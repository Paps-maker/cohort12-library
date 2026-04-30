package app.validation;

import app.dao.BorrowDAO;
import jakarta.inject.Inject;           // ✅ Use this
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

/**
 * ✅ BUSINESS RULE ENGINE: Return Validation
 */
@Named("returnValidator")
@RequestScoped
public class ReturnValidator {

    // ✅ FIXED: Inject the DAO so WildFly can provide the Database Connection
    @Inject
    private BorrowDAO borrowDao;

    /**
     * Validates if a book can be returned.
     */
    public String validateReturn(String role, String borrowIdStr) {

        // 1. Authorization
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return "Unauthorized: Only administrative staff can process returns.";
        }

        // 2. Format Check
        if (borrowIdStr == null || borrowIdStr.trim().isEmpty()) {
            return "Input Error: Transaction ID is missing.";
        }

        try {
            // Clean the ID just in case the Servlet missed a character
            int borrowId = Integer.parseInt(borrowIdStr.replaceAll("[^0-9]", ""));

            // 3. Existence Check
            // This will now work because borrowDao is no longer null!
            if (!borrowDao.exists(borrowId)) {
                return "Record Error: Transaction #" + borrowId + " not found in database.";
            }

            // 4. State Check
            if (borrowDao.isAlreadyReturned(borrowId)) {
                return "Status Error: This book has already been marked as returned.";
            }

        } catch (NumberFormatException e) {
            return "Format Error: Transaction ID [" + borrowIdStr + "] must be numeric.";
        }

        return null;
    }
}