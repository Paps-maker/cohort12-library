package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.dao.UserDAO;
import app.events.LibraryEvent;
import app.model.Book;
import app.model.BorrowedBook;
import app.model.Fine;
import app.validation.BorrowValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDateTime;

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
     * Handles the borrowing process by utilizing GenericDao save methods.
     */
    public String validateAndBorrow(String username, int bookId, int days) {
        int currentLoanCount = borrowDao.getMemberLoanCount(username);
        double unpaidFines = fineDao.getTotalUnpaid(username);

        // FIX: Replaced getAvailableCopiesCount(bookId) with findById
        Book book = bookDao.findById(bookId);
        int availableCount = (book != null) ? book.getAvailableCopies() : 0;
        boolean available = (availableCount > 0);

        String error = borrowValidator.canBorrow("ACTIVE", currentLoanCount, available, unpaidFines);
        if (error != null) return error;

        try {
            // FIX: Replaced borrowBook() with object instantiation and save()
            BorrowedBook record = new BorrowedBook(username, bookId, days);
            borrowDao.save(record);
            int generatedBorrowId = record.getId();

            // Update inventory
            bookDao.addCopiesToExistingBook(bookId, -1);

            String bookTitle = (book != null) ? book.getTitle() : "Unknown Title";

            // Resolve actual email address via UserDAO
            String recipientEmail = username;
            var userRecord = userDao.findUserByUsername(username);
            if (userRecord != null && userRecord.getEmail() != null) {
                recipientEmail = userRecord.getEmail();
            }

            String message = String.format("REC #%d | Book: %s (ID: %d)",
                    generatedBorrowId, bookTitle, bookId);

            eventPublisher.fire(new LibraryEvent("BORROW", recipientEmail, message, "Active"));

            return "Success: Checked out " + bookTitle + "! Return within " + days + " days.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Transaction failed during database write.";
        }
    }

    /**
     * Handles the return process, issuing fines if the record is overdue.
     */
    public String processReturn(String role, String borrowIdParam) {
        String error = returnValidator.validateReturn(role, borrowIdParam);
        if (error != null) return error;

        try {
            int borrowId = Integer.parseInt(borrowIdParam);

            // FIX: Replaced getBookIdByBorrowId and getMemberByBorrowId with findById lookups
            BorrowedBook borrowRecord = borrowDao.findById(borrowId);
            if (borrowRecord == null) return "Error: Borrow record not found.";

            int bookId = borrowRecord.getBookId();
            String memberUsername = borrowRecord.getUsername();
            int daysLate = borrowDao.getOverdueDays(borrowId);

            String bookTitle = bookDao.getBookTitleById(bookId);
            String fineStatus = "No Fine";

            if (daysLate > 0 && memberUsername != null) {
                // FIX: Replaced insertFine() with manual Fine creation and save()
                Fine fine = new Fine();
                fine.setUsername(memberUsername);
                fine.setAmount(daysLate * 50.0);
                fine.setDaysOverdue(daysLate);
                fine.setBorrowId(borrowId);
                fine.setBookId(bookId);
                fine.setStatus("UNPAID");
                fine.setCreatedAt(LocalDateTime.now());
                fineDao.save(fine);

                fineStatus = "Fine Issued: KSH " + fine.getAmount();
            }

            // FIX: Replaced returnBook() with delete()
            borrowDao.delete(borrowId);

            if (bookId != -1) {
                bookDao.addCopiesToExistingBook(bookId, 1);
            }

            String recipientEmail = memberUsername;
            var user = userDao.findUserByUsername(memberUsername);
            if (user != null) recipientEmail = user.getEmail();

            String message = String.format("Return Processed for REC #%d | Book: %s",
                    borrowId, bookTitle);

            eventPublisher.fire(new LibraryEvent("RETURN", recipientEmail, message, fineStatus));

            return "Success: " + bookTitle + " returned. " + fineStatus;

        } catch (NumberFormatException e) {
            return "Invalid Borrow ID format.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Critical Return Error.";
        }
    }
}