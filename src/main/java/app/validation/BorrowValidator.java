package app.validation;

import jakarta.enterprise.context.ApplicationScoped; //  Added

/**
 * Validator for the Circulation/Borrowing Module.
 */
@ApplicationScoped // This is mandatory for CDI to manage the lifecycle
@ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW)
public class BorrowValidator implements Validate {

    @Override
    public boolean name(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public String canBorrow(String memberStatus, int currentLoans, boolean isBookAvailable, double unpaidFines) {

        if (!"ACTIVE".equalsIgnoreCase(memberStatus)) {
            return "Member account is not active or is suspended.";
        }

        if (currentLoans >= 5) {
            return "Maximum borrowed limit reached (5 books). Please return a book before borrowing more.";
        }

        if (!isBookAvailable) {
            return "This book is currently unavailable (checked out by someone else).";
        }

        if (unpaidFines > 0) {
            return "Action Denied: You have an unpaid fine balance of KSH " + String.format("%.2f", unpaidFines) + ". Please pay your fines first.";
        }

        return null;
    }
}