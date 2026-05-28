package app.validation;

import app.dao.BorrowDAO;
import app.model.BorrowedBook;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

@Named("returnValidator")
@RequestScoped
public class ReturnValidator {

    @Inject
    private BorrowDAO borrowDao;

    /**
     * Validates if a book can be returned.
     * Updated to use GenericDao patterns to resolve compilation errors.
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
            // Clean the input to ensure it is purely numeric
            int borrowId = Integer.parseInt(borrowIdStr.replaceAll("[^0-9]", ""));

            // 3. Existence & State Check
            // Replaced borrowDao.exists(borrowId) with findById check
            BorrowedBook record = borrowDao.findById(borrowId);

            if (record == null) {
                return "Status Error: Transaction #" + borrowId + " not found or already returned.";
            }

        } catch (NumberFormatException e) {
            return "Format Error: Transaction ID [" + borrowIdStr + "] must be numeric.";
        }

        return null; // Validation passed
    }
}