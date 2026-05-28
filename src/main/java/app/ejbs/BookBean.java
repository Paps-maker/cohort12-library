package app.ejbs;

import app.dao.BookDAO;
import app.model.Book;
import app.validation.BookValidator;
import app.validation.ValidatorQualifier;
import app.websocket.SystemActivityServer; // 🌟 IMPORT YOUR WEBSOCKET SERVER
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class BookBean {

    @Inject
    private BookDAO bookDAO;

    @Inject
    private CatalogBean catalogBean;

    @Inject
    private BorrowingBean borrowingBean;

    private BookValidator bookValidator;

    @Inject
    public void setBookValidator(@ValidatorQualifier(ValidatorQualifier.ValidationChoice.BOOK) BookValidator bookValidator) {
        this.bookValidator = bookValidator;
    }

    // ==========================================
    // CRUD OPERATIONS
    // ==========================================

    /**
     * Adds a book and returns null on success, or an error message string on failure.
     */
    public String addBook(Book book) {
        if (book == null) return "Book record is null.";

        String validationError = bookValidator.validate(book.getTitle(), book.getImageUrl(), book.getDescription());
        if (validationError != null) {
            return validationError;
        }

        try {
            bookDAO.save(book);

            // 🌟 WEBSOCKET BROADCAST: Notify admins when a new book enters the library catalog
            SystemActivityServer.broadcastActivity("📚 NEW BOOK ADDED: '" + book.getTitle() +
                    "' by " + book.getAuthor() + " (ISBN: " + book.getIsbn() + ") has been added to the catalog inventory.");

            return null; // Null signifies success
        } catch (Exception e) {
            return "Internal server error: Unable to save book to the database.";
        }
    }

    public Book getBookById(int id) {
        return (id <= 0) ? null : bookDAO.findById(id);
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    /**
     * Updates book details and inventory. Returns "success" or a descriptive error.
     */
    public String updateBook(Book book, int addCopies) {
        if (book == null || book.getId() <= 0) return "Invalid book record provided.";

        String validationError = bookValidator.validate(book.getTitle(), book.getImageUrl(), book.getDescription());
        if (validationError != null) return "Validation failed: " + validationError;

        try {
            boolean isUpdated = bookDAO.updateBookDetailsAndInventory(book, addCopies);

            if (isUpdated) {
                // 🌟 WEBSOCKET BROADCAST: Broadcast detailed catalog metadata modifications
                SystemActivityServer.broadcastActivity("🔄 BOOK MODIFIED: Details for '" + book.getTitle() +
                        "' (ID: " + book.getId() + ") have been updated. Added copies: " + addCopies);
                return "success";
            }
            return "Update failed: Database constraints not met.";
        } catch (Exception e) {
            return "Update failed: System error during persistence.";
        }
    }

    public void deleteBook(int id, String role) throws SecurityException, IllegalArgumentException {
        if (!"ADMIN".equals(role)) throw new SecurityException("Unauthorized access.");
        if (id <= 0) throw new IllegalArgumentException("Invalid book identifier.");

        try {
            // Fetch title before deleting for cleaner logging contexts
            String bookTitle = getBookTitle(id);

            bookDAO.delete(id);

            // 🌟 WEBSOCKET BROADCAST: Broadcast absolute item deletions from the platform records
            SystemActivityServer.broadcastActivity("🚨 CATALOG PURGE: Book '" + bookTitle +
                    "' (ID: " + id + ") was permanently deleted from the system repository by an Admin.");

        } catch (Exception e) {
            throw new RuntimeException("Database constraint failure during deletion of ID: " + id, e);
        }
    }

    // ==========================================
    // INVENTORY LOGIC
    // ==========================================

    public boolean addCopies(int bookId, int amount) {
        boolean isAdminAction = (amount > 1 || amount < -1);
        boolean isSuccess = bookDAO.updateInventory(bookId, amount, isAdminAction);

        if (isSuccess) {
            String bookTitle = getBookTitle(bookId);
            // 🌟 WEBSOCKET BROADCAST: Track real-time inventory adjustments
            SystemActivityServer.broadcastActivity("📦 INVENTORY ADJUSTMENT: Book '" + bookTitle +
                    "' (ID: " + bookId + ") stock levels updated by " + (amount > 0 ? "+" : "") + amount + " unit(s).");
        }
        return isSuccess;
    }

    public boolean isBookAvailable(int bookId) {
        Book book = bookDAO.findById(bookId);
        return book != null && book.getAvailableCopies() > 0;
    }

    // ==========================================
    // ANALYTICS & METRICS
    // ==========================================

    public int getAvailableCount() {
        return catalogBean.calculateTotalAvailableCopies();
    }

    public int getGlobalBorrowedCount() {
        return catalogBean.calculateSystemBorrowedCount();
    }

    public String getBookTitle(int id) {
        Book book = bookDAO.findById(id);
        return (book != null) ? book.getTitle() : "Unknown Book";
    }

    public int getDaysUntilAvailable(String bookTitle) {
        if (bookTitle == null || bookTitle.trim().isEmpty()) return 0;
        try {
            java.time.LocalDateTime earliestDueDate = borrowingBean.getEarliestReturnDateByTitle(bookTitle);
            if (earliestDueDate == null || earliestDueDate.isBefore(java.time.LocalDateTime.now())) return 1;

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDateTime.now(), earliestDueDate);
            return daysBetween <= 0 ? 1 : (int) daysBetween;
        } catch (Exception e) {
            return 1;
        }
    }
}