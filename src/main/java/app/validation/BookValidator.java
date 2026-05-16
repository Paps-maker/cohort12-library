package app.validation;

import jakarta.enterprise.context.RequestScoped; //  Required for CDI management
import jakarta.inject.Named;


@RequestScoped
@ValidatorQualifier(ValidatorQualifier.ValidationChoice.BOOK)
public class BookValidator implements Validate {


    @Override
    public boolean name(String name) {
        return name != null && name.trim().length() >= 3;
    }

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

        return null; //  All checks passed
    }
}