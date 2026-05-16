package app.services;

import app.dao.BookDAO;
import app.model.Book;
import app.ejbs.CatalogBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class BookService {

    @Inject
    private BookDAO bookDAO;

    @Inject
    private CatalogBean catalogBean;

    // =========================================================
    // 📚 CREATE OPERATIONS
    // =========================================================

    /**
     * Persists a new book record into the MySQL database.
     * Updated to use GenericDao.save().
     */
    public boolean addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return false;
        }
        try {
            // save() handles both persist and merge
            bookDAO.save(book);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // 📖 READ OPERATIONS
    // =========================================================

    /**
     * Fetches a single book by ID via the DAO.
     * Updated to findById() to match GenericDao.
     */
    public Book getBookById(int id) {
        if (id <= 0) return null;
        return bookDAO.findById(id);
    }

    /**
     * Retrieves all books currently in the catalog.
     */
    public List<Book> getAllBooks() {
        return catalogBean.getAvailableBooks();
    }

    /**
     * Sums up total available copies across the library.
     */
    public int getAvailableCount() {
        return catalogBean.getTotalAvailableCopies();
    }

    /**
     * Retrieves system-wide borrowed counts via CatalogBean.
     */
    public int getGlobalBorrowedCount() {
        return catalogBean.getSystemBorrowedCount();
    }

    // =========================================================
    // ✏️ UPDATE/INVENTORY OPERATIONS
    // =========================================================

    /**
     * Updates book metadata and optionally increases physical stock.
     * Logic remains the same, but uses GenericDao methods.
     */
    public String updateBook(Book book, int addCopies) {
        if (book == null || book.getId() <= 0) return "invalid_book";

        try {
            // Using save() for updates as it wraps em.merge()
            bookDAO.save(book);

            // Handle inventory adjustment
            if (addCopies > 0) {
                bookDAO.addCopiesToExistingBook(book.getId(), addCopies);
            }
            return "success";
        } catch (Exception e) {
            return "update_failed";
        }
    }

    /**
     * Direct method to increase stock for a specific book.
     */
    public boolean addCopies(int bookId, int copies) {
        return bookDAO.addCopiesToExistingBook(bookId, copies);
    }

    // =========================================================
    // 🗑 DELETE OPERATIONS
    // =========================================================

    /**
     * Securely deletes a book record. Role check included.
     * Updated to delete() to match GenericDao.
     */
    public String deleteBook(int id, String role) {
        if (!"ADMIN".equals(role)) {
            return "unauthorized";
        }

        if (id <= 0) return "invalid_id";

        try {
            bookDAO.delete(id);
            return "success";
        } catch (Exception e) {
            return "delete_failed";
        }
    }

    // =========================================================
    // 📦 BUSINESS LOGIC / HELPERS
    // =========================================================

    /**
     * Logic for wait-times when a book is out of stock.
     */
    public int getDaysUntilAvailable(String title) {
        return 0; // Placeholder for future BorrowDAO logic
    }

    /**
     * Determines if a loan status requires urgent attention.
     */
    public boolean isUrgent(String daysStr) {
        return catalogBean.isLoanUrgent(daysStr);
    }

    /**
     * Helper to retrieve a book title by ID.
     */
    public String getBookTitle(int id) {
        Book book = bookDAO.findById(id); // Use standard findById
        return (book != null) ? book.getTitle() : "Unknown Book";
    }
}