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

@Singleton
@Startup
public class FineScheduler {

    @Inject
    private BorrowDAO borrowDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void processOverdueFines() {
        System.out.println("⏳ Running daily overdue check...");

        List<Integer> overdueIds = borrowDAO.getAllOverdueBorrowIds();

        if (overdueIds != null && !overdueIds.isEmpty()) {
            for (Integer borrowId : overdueIds) {
                int daysLate = borrowDAO.getOverdueDays(borrowId);

                if (daysLate > 0) {
                    double totalFine = daysLate * 50.0;
                    String username = borrowDAO.getMemberByBorrowId(borrowId);
                    String email = userDAO.getEmailByUsername(username);

                    eventPublisher.fire(new LibraryEvent(
                            "OVERDUE_UPDATE",
                            email,
                            "Daily Fine Update: KSH " + totalFine + " (Days Late: " + daysLate + ")",
                            "Overdue"
                    ));

                    System.out.println("📧 Fine update sent to: " + email + " for Record ID: " + borrowId);
                }
            }
        }
    }
}