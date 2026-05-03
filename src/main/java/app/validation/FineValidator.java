package app.validation;

import app.dao.FineDAO;
import app.model.Fine;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

/**
 * ✅ BUSINESS RULE ENGINE: Fine Payment Validation
 * This class ensures that payments are only processed for valid, unpaid fines
 * belonging to the authenticated user.
 */
@Named("fineValidator")
@RequestScoped
public class FineValidator {

    @Inject
    private FineDAO fineDAO;

    /**
     * ✅ FULL VALIDATION: Checks existence, ownership, and status.
     * Synchronized with FineDAO.getFineById(int).
     *
     * @param username The user attempting the payment.
     * @param fineIdStr The raw ID string from the web form.
     * @return String error message if invalid, or null if validation passes.
     */
    public String validatePayment(String username, String fineIdStr) {

        // 1. Basic Format Check
        if (fineIdStr == null || fineIdStr.trim().isEmpty()) {
            return "Error: Fine ID is missing.";
        }

        int fineId;
        try {
            fineId = Integer.parseInt(fineIdStr);
        } catch (NumberFormatException e) {
            return "Error: Invalid Fine ID format.";
        }

        // 2. Existence Check
        // Resolves compilation error by calling the validated method in FineDAO
        Fine existingFine = fineDAO.getFineById(fineId);
        if (existingFine == null) {
            return "Error: Fine record not found in the system.";
        }

        // 3. Ownership Security Check
        // Prevents users from manipulating URL parameters to pay others' fines
        if (!existingFine.getUsername().equalsIgnoreCase(username)) {
            return "Error: Security Violation. This fine does not belong to your account.";
        }

        // 4. Status Check
        if ("PAID".equalsIgnoreCase(existingFine.getStatus())) {
            return "Error: This fine has already been settled.";
        }

        // 5. Amount Validation
        if (existingFine.getAmount() <= 0) {
            return "Error: This fine has no outstanding balance.";
        }

        return null; // All checks passed!
    }
}