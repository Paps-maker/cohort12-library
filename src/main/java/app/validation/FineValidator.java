package app.validation;

import app.dao.FineDAO;
import app.model.Fine;
import jakarta.inject.Inject;            // ✅ Added for Managed Injection
import jakarta.inject.Named;             // ✅ The Built-in Qualifier
import jakarta.enterprise.context.RequestScoped; // ✅ Lifecycle Management

/**
 * ✅ BUSINESS RULE ENGINE: Fine Payment Validation
 * Marked with @Named to allow WildFly to inject it into the Service Layer.
 */
@Named("fineValidator") // Built-in Qualifier name
@RequestScoped          // This validator lives only for the duration of one request
public class FineValidator {

    @Inject
    private FineDAO fineDAO; // ✅ UPDATED: Now injected, not manually instantiated

    /**
     * ✅ FULL VALIDATION: Checks existence, ownership, and status.
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
        Fine existingFine = fineDAO.getFineById(fineId);
        if (existingFine == null) {
            return "Error: Fine record not found in the system.";
        }

        // 3. Ownership Security Check
        // Ensures users can't pay for someone else's fines by guessing IDs
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