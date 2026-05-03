package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.validation.BorrowValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Stateless
public class BorrowingBean {

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private BookDAO bookDao;

    @Inject
    private FineDAO fineDao;

    @Inject
    @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW)
    private BorrowValidator borrowValidator;

    @Inject
    @Named("returnValidator")
    private ReturnValidator returnValidator;

    /**
     * Handles the borrowing process by validating user status and updating inventory.
     */
    public String validateAndBorrow(String username, int bookId, int days) {
        int currentLoanCount = borrowDao.getMemberLoanCount(username);
        double unpaidFines = fineDao.getTotalUnpaid(username);

        // ✅ Check physical stock levels from the database column
        int availableCount = bookDao.getAvailableCopiesCount(bookId);
        boolean available = (availableCount > 0);

        // Run validation logic (checks loan limits, stock availability, and existing debt)
        String error = borrowValidator.canBorrow("ACTIVE", currentLoanCount, available, unpaidFines);
        if (error != null) return error;

        // ✅ Register the loan and decrement physical inventory count
        boolean success = borrowDao.borrowBook(username, bookId, days);
        if (success) {
            // Subtract 1 from available_copies
            bookDao.addCopiesToExistingBook(bookId, -1);
            return null; // Success
        }

        return "Transaction failed during database write.";
    }

    /**
     * Handles the return process, calculates fines, and restores inventory stock.
     */
    public String processReturn(String role, String borrowIdParam) {
        // Basic validation for role and parameter presence
        String error = returnValidator.validateReturn(role, borrowIdParam);
        if (error != null) return error;

        try {
            int borrowId = Integer.parseInt(borrowIdParam);

            // ✅ Identify book and member before deleting the borrow record
            int bookId = borrowDao.getBookIdByBorrowId(borrowId);
            String member = borrowDao.getMemberByBorrowId(borrowId);
            int daysLate = borrowDao.getOverdueDays(borrowId);

            // Create fine record if returned late (Example: KSH 50.0 per day)
            if (daysLate > 0 && member != null) {
                // ✅ UPDATED: Passing bookId as the 5th argument to match updated FineDAO
                fineDao.insertFine(member, daysLate * 50.0, daysLate, borrowId, bookId);
            }

            // ✅ Delete the borrow record and increment physical inventory count
            if (borrowDao.returnBook(borrowId)) {
                if (bookId != -1) {
                    // Add 1 back to available_copies
                    bookDao.addCopiesToExistingBook(bookId, 1);
                }
                return null; // Success
            }
            return "Database Error: Could not remove loan record.";
        } catch (NumberFormatException e) {
            return "Invalid Borrow ID format.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Critical Return Error.";
        }
    }
}