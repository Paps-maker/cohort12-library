package app.validation;

import app.dao.BorrowDAO;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

/**
 * ✅ BUSINESS RULE ENGINE: Return Validation
 */
@Named("returnValidator")
@RequestScoped
public class ReturnValidator {

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
            // Clean the ID (removes # or spaces if present)
            int borrowId = Integer.parseInt(borrowIdStr.replaceAll("[^0-9]", ""));

            // 3. Existence & State Check (Merged)
            // In a relational system, if the record is gone from the 'borrowed' table,
            // it means the book is already back in the 'inventory'.
            if (!borrowDao.exists(borrowId)) {
                return "Status Error: Transaction #" + borrowId + " not found or already returned.";
            }

        } catch (NumberFormatException e) {
            return "Format Error: Transaction ID [" + borrowIdStr + "] must be numeric.";
        }

        return null; // Validation passed
    }
}