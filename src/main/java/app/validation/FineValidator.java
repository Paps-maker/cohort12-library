package app.validation;

import app.dao.FineDAO;
import app.model.Fine;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;

/**
 * VALIDATION LAYER: FINES
 * Refactored to align with GenericDao for the Assessment-1-Livingstone Project.
 */
@Named("fineValidator")
@RequestScoped
public class FineValidator {

    @Inject
    private FineDAO fineDAO;

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
        // ✅ Standardized method from GenericDao (replaces getFineById)
        Fine existingFine = fineDAO.findById(fineId);
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

        return null; // Validation successful
    }
}