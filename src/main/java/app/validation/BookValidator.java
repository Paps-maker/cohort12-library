package app.validation;

import jakarta.enterprise.context.RequestScoped; //  Required for CDI management
import jakarta.inject.Named;

/**
 *  BUSINESS RULE ENGINE: Book Validation
 * 1. Labelled with the Qualifier for Strategy-based injection.
 * 2. RequestScoped ensures a fresh instance per form submission.
 */
@RequestScoped
@ValidatorQualifier(ValidatorQualifier.ValidationChoice.BOOK)
public class BookValidator implements Validate {

    /**
     * Implementation of the Validate interface method.
     * Core logic for Title length.
     */
    @Override
    public boolean name(String name) {
        return name != null && name.trim().length() >= 3;
    }

    /**
     * Comprehensive validation for the Book creation form.
     * This ensures data integrity before hits your MySQL/Postgres DAOs.
     * * @return String error message if invalid, null if validation passes.
     */
    public String validate(String title, String imageUrl, String description) {

        // 1. Title Validation (Logic reused from interface)
        if (!name(title)) {
            if (title == null || title.trim().isEmpty()) {
                return "Validation Error: Book title is required.";
            }
            return "Validation Error: '" + title + "' is too short (Minimum 3 characters).";
        }

        // 2. Image URL Validation
        // If provided, it must be a formatted URL to prevent broken images in the catalog.
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            String urlLower = imageUrl.toLowerCase().trim();
            if (!urlLower.startsWith("http://") && !urlLower.startsWith("https://")) {
                return "Link Error: Image URL must start with http:// or https://";
            }

            // Optional: Check for common image extensions
            if (!(urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg") ||
                    urlLower.endsWith(".png") || urlLower.endsWith(".webp"))) {
                // We'll allow it, but a warning could be logged here.
            }
        }

        // 3. Description Validation
        // Prevents database 'Data Too Long' exceptions in MySQL.
        if (description != null && description.length() > 500) {
            return "Content Error: Description exceeds the 500-character limit (Current: " + description.length() + ").";
        }

        return null; // ✅ All checks passed
    }
}