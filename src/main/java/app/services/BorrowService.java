package app.services;

import app.dao.BorrowDAO;
import app.ejbs.BorrowingBean;
import app.events.LibraryEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BorrowService {

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    // =========================================================================
    // LISTING
    // =========================================================================

    public List<String> getAdminBorrowedRecords() {
        return borrowDao.getAllBorrowed();
    }

    public List<String> getMemberActiveLoans(String username) {
        return borrowDao.getUserBorrowed(username);
    }

    // =========================================================================
    // BORROW (DELEGATED TO BEAN)
    // =========================================================================

    public String attemptBorrow(String username, String role, String bookIdParam, String daysParam) {
        return borrowingBean.validateAndBorrow(username, parseInt(bookIdParam), parseInt(daysParam));
    }

    // =========================================================================
    // RETURN (DELEGATED TO BEAN)
    // =========================================================================

    public String processReturnRequest(String role, String borrowIdParam) {
        return borrowingBean.processReturn(role, borrowIdParam);
    }
// =========================================================================
// COUNTS & STATS
// =========================================================================

    /**
     * ✅ Added to resolve Servlet error.
     * Returns the number of active loans a specific member currently has.
     */
    public int getMemberBorrowedCount(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        // Assuming borrowDao has a method to return the count
        // If your DAO only has getMemberLoanCount, use that name instead
        return borrowDao.getMemberLoanCount(username);
    }
    // =========================================================================
    // SAFE PARSER (prevents duplication of try/catch everywhere)
    // =========================================================================

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }
}