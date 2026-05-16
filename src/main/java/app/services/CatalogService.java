package app.services;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.ejbs.CatalogBean;
import app.model.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class CatalogService {

    @Inject
    private CatalogBean catalogBean;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private BookDAO bookDao;

    // =========================================================================
    // BOOK LISTING
    // =========================================================================

    public List<Book> getAllBooks() {
        return catalogBean.getAvailableBooks();
    }

    // =========================================================================
    // INVENTORY COUNTS
    // =========================================================================

    public int getAvailableCount() {
        return catalogBean.getTotalAvailableCopies();
    }

    public int getSystemBorrowedCount() {
        return catalogBean.getSystemBorrowedCount();
    }

    public int getMemberBorrowedCount(String username) {
        return catalogBean.getMemberBorrowedCount(username);
    }

    // =========================================================================
    // DASHBOARD LABEL HELPERS
    // =========================================================================

    public String getBorrowedLabel(String role) {
        return "ADMIN".equalsIgnoreCase(role)
                ? "Total Copies Borrowed"
                : "My Borrowed Books";
    }

    // =========================================================================
    // BOOK STATUS HELPERS
    // =========================================================================

    public int getDaysUntilAvailable(String bookTitle) {
        return borrowDao.getDaysLeft(bookTitle);
    }

    /**
     * Updated to use GenericDao findById lookup.
     */
    public boolean isBookAvailable(int bookId) {
        Book book = bookDao.findById(bookId); // Uses inherited findById
        return book != null && book.getAvailableCopies() > 0;
    }

    public boolean isLoanUrgent(String statusText) {
        return catalogBean.isLoanUrgent(statusText);
    }
}