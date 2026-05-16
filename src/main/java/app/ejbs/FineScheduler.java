package app.ejbs;

import app.dao.BorrowDAO;
import app.dao.UserDAO;
import app.events.LibraryEvent;
import app.model.BorrowedBook;
import app.model.User;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Scheduled tasks for the library system.
 * Updated to align with GenericDao patterns for the Assessment-1-Livingstone project.
 */
@Singleton
@Startup
public class FineScheduler {

    @Inject
    private BorrowDAO borrowDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private FineBean fineBean;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void processOverdueFines() {
        System.out.println(" Running daily overdue check [MIDNIGHT]...");

        List<Integer> overdueIds = borrowDAO.getAllOverdueBorrowIds();

        if (overdueIds != null && !overdueIds.isEmpty()) {
            for (Integer borrowId : overdueIds) {
                sendOverdueNotification(borrowId);
            }
        }
    }

    @Schedule(hour = "8", minute = "0", second = "0", persistent = false)
    public void sendDueTomorrowReminders() {
        System.out.println(" Running 'Due Tomorrow' reminder check [08:00 AM]...");

        List<Integer> upcomingIds = borrowDAO.getBooksDueIn24Hours();

        if (upcomingIds != null && !upcomingIds.isEmpty()) {
            for (Integer borrowId : upcomingIds) {
                // FIX: Use findById to get the BorrowedBook entity
                BorrowedBook record = borrowDAO.findById(borrowId);

                if (record != null) {
                    String username = record.getUsername();
                    // FIX: Resolve email via userDAO lookup
                    User user = userDAO.findUserByUsername(username);
                    String email = (user != null) ? user.getEmail() : null;

                    // We can still use the custom title helper if it exists in BorrowDAO,
                    // or fetch via BookDAO if needed.
                    String bookTitle = borrowDAO.getBookTitleByBorrowId(borrowId);

                    if (email != null) {
                        eventPublisher.fire(new LibraryEvent(
                                "REMINDER",
                                email,
                                "Friendly Reminder: '" + bookTitle + "' is due tomorrow. Please return it to avoid fines.",
                                "Upcoming"
                        ));
                    }
                }
            }
        }
    }

    private void sendOverdueNotification(int borrowId) {
        double totalFine = fineBean.calculateFineForRecord(borrowId);
        int daysLate = borrowDAO.getOverdueDays(borrowId);

        if (totalFine > 0) {
            // FIX: Use findById to get the record details
            BorrowedBook record = borrowDAO.findById(borrowId);

            if (record != null) {
                String username = record.getUsername();
                // FIX: Resolve email via findUserByUsername
                User user = userDAO.findUserByUsername(username);
                String email = (user != null) ? user.getEmail() : null;

                if (email != null) {
                    eventPublisher.fire(new LibraryEvent(
                            "OVERDUE_UPDATE",
                            email,
                            "Fine Update Notice: KSH " + String.format("%.2f", totalFine) +
                                    " (Days Late: " + daysLate + ")",
                            "Overdue"
                    ));

                    System.out.println("📧 Fine update email sent to: " + email + " for Record ID: " + borrowId);
                }
            }
        }
    }
}