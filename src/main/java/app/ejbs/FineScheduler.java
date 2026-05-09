package app.ejbs;

import app.dao.BorrowDAO;
import app.dao.UserDAO;
import app.events.LibraryEvent;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.util.List;

/**
 * PROACTIVE FINE SCHEDULER
 * Transitioned from post-return notifications to pre-overdue reminders.
 */
@Singleton
@Startup
public class FineScheduler {

    @Inject
    private BorrowDAO borrowDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    /**
     * ✅ OVERDUE CHECK: Runs daily at midnight.
     * Identifies items that have officially passed their due date to update fine status.
     */
    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void processOverdueFines() {
        System.out.println("⏳ Running daily overdue check [MIDNIGHT]...");

        List<Integer> overdueIds = borrowDAO.getAllOverdueBorrowIds();

        if (overdueIds != null && !overdueIds.isEmpty()) {
            for (Integer borrowId : overdueIds) {
                sendOverdueNotification(borrowId);
            }
        }
    }

    /**
     * ✅ PROACTIVE REMINDER: Runs daily at 8:00 AM.
     * Notifies users who have books due in the next 24 hours to help them avoid penalties.
     */
    @Schedule(hour = "8", minute = "0", second = "0", persistent = false)
    public void sendDueTomorrowReminders() {
        System.out.println(" Running 'Due Tomorrow' reminder check [08:00 AM]...");

        // Fetches borrow IDs where DATEDIFF(due_date, CURDATE()) = 1
        List<Integer> upcomingIds = borrowDAO.getBooksDueIn24Hours();

        if (upcomingIds != null && !upcomingIds.isEmpty()) {
            for (Integer borrowId : upcomingIds) {
                String username = borrowDAO.getMemberByBorrowId(borrowId);
                String email = userDAO.getEmailByUsername(username);
                String bookTitle = borrowDAO.getBookTitleByBorrowId(borrowId);

                if (email != null) {
                    eventPublisher.fire(new LibraryEvent(
                            "REMINDER",
                            email,
                            "Friendly Reminder: '" + bookTitle + "' is due tomorrow. Please return it to avoid fines.",
                            "Upcoming"
                    ));
                    System.out.println("📧 Pre-emptive reminder sent to: " + email + " for '" + bookTitle + "'");
                }
            }
        }
    }

    /**
     * Internal logic to handle actual overdue penalty notifications.
     */
    private void sendOverdueNotification(int borrowId) {
        int daysLate = borrowDAO.getOverdueDays(borrowId);

        if (daysLate > 0) {
            double totalFine = daysLate * 50.0; // Based on KSH 50.00 per day
            String username = borrowDAO.getMemberByBorrowId(borrowId);
            String email = userDAO.getEmailByUsername(username);

            if (email != null) {
                eventPublisher.fire(new LibraryEvent(
                        "OVERDUE_UPDATE",
                        email,
                        "Fine Update Notice: KSH " + totalFine + " (Days Late: " + daysLate + ")",
                        "Overdue"
                ));

                System.out.println("📧 Fine update email sent to: " + email + " for Record ID: " + borrowId);
            }
        }
    }

    /*
     * 🗑️ CLEANUP: Removed scheduleDelayedNotification and handleTimeout.
     * The system no longer uses one-time 2-minute timers after a book return,
     * favoring scheduled morning reminders instead.
     */
}