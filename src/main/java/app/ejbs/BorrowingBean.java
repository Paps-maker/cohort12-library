package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.dao.UserDAO;
import app.events.LibraryEvent;
import app.validation.BorrowValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
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
    private UserDAO userDao;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    @Inject
    @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW)
    private BorrowValidator borrowValidator;

    @Inject
    @Named("returnValidator")
    private ReturnValidator returnValidator;

    /**
     * Handles the borrowing process, fetching both Record ID and Book Title for the email.
     */
    public String validateAndBorrow(String username, int bookId, int days) {
        int currentLoanCount = borrowDao.getMemberLoanCount(username);
        double unpaidFines = fineDao.getTotalUnpaid(username);

        int availableCount = bookDao.getAvailableCopiesCount(bookId);
        boolean available = (availableCount > 0);

        String error = borrowValidator.canBorrow("ACTIVE", currentLoanCount, available, unpaidFines);
        if (error != null) return error;

        // ✅ 1. Get the newly generated Record ID from the DAO
        int generatedBorrowId = borrowDao.borrowBook(username, bookId, days);

        if (generatedBorrowId != -1) {
            bookDao.addCopiesToExistingBook(bookId, -1);

            // ✅ 2. Fetch the Book Title so the email isn't just numbers
            String bookTitle = bookDao.getBookTitleById(bookId); // Ensure this exists in your BookDAO
            if (bookTitle == null) bookTitle = "Unknown Title";

            // ✅ 3. Resolve the actual email address
            String recipientEmail = username;
            try {
                var user = userDao.findUserByUsername(username);
                if (user != null && user.getEmail() != null) {
                    recipientEmail = user.getEmail();
                }
            } catch (Exception e) {
                System.err.println("Email resolution failed for: " + username);
            }

            // ✅ 4. Fire event with detailed description
            String message = String.format("REC #%d | Book: %s (ID: %d)",
                    generatedBorrowId, bookTitle, bookId);

            eventPublisher.fire(new LibraryEvent("BORROW", recipientEmail, message, "Active"));

            return "Success: Checked out " + bookTitle + "! Return within " + days + " days.";
        }

        return "Transaction failed during database write.";
    }

    /**
     * Handles the return process with detailed book info in the notification.
     */
    public String processReturn(String role, String borrowIdParam) {
        String error = returnValidator.validateReturn(role, borrowIdParam);
        if (error != null) return error;

        try {
            int borrowId = Integer.parseInt(borrowIdParam);
            int bookId = borrowDao.getBookIdByBorrowId(borrowId);
            String memberUsername = borrowDao.getMemberByBorrowId(borrowId);
            int daysLate = borrowDao.getOverdueDays(borrowId);

            // Fetch book title for the return email
            String bookTitle = bookDao.getBookTitleById(bookId);
            if (bookTitle == null) bookTitle = "Unknown Title";

            String fineStatus = "No Fine";
            if (daysLate > 0 && memberUsername != null) {
                fineDao.insertFine(memberUsername, daysLate * 50.0, daysLate, borrowId, bookId);
                fineStatus = "Fine Issued: KSH " + (daysLate * 50.0);
            }

            if (borrowDao.returnBook(borrowId)) {
                if (bookId != -1) {
                    bookDao.addCopiesToExistingBook(bookId, 1);
                }

                String recipientEmail = memberUsername;
                try {
                    var user = userDao.findUserByUsername(memberUsername);
                    if (user != null) recipientEmail = user.getEmail();
                } catch (Exception e) {}

                // ✅ Clearer Return message
                String message = String.format("Return Processed for REC #%d | Book: %s",
                        borrowId, bookTitle);

                eventPublisher.fire(new LibraryEvent("RETURN", recipientEmail, message, fineStatus));

                return "Success: " + bookTitle + " returned. " + fineStatus;
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