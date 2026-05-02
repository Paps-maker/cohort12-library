package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.model.Book;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class CatalogBean {

    @Inject
    private BookDAO bookDao;

    @Inject
    private BorrowDAO borrowDao;

    /**
     * Fetches all books from the database.
     * The BookDAO now populates quantity fields automatically.
     */
    public List<Book> getAllBooks() {
        return bookDao.getAllBooks();
    }

    /**
     * ✅ OPTIMIZED: Sums up available units.
     * Uses the field already mapped in the Book model to avoid
     * repetitive "N+1" database queries.
     */
    public int calculateAvailableCount() {
        return bookDao.getAllBooks().stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();
    }

    /**
     * Calculates borrowing statistics for the dashboard cards.
     */
    public int getBorrowedCount(String username, String role) {
        if ("ADMIN".equals(role)) {
            // Returns total active loans in the system
            return borrowDao.getMemberLoanCount(null);
        } else {
            // Returns only loans for the specific user
            return borrowDao.getMemberLoanCount(username);
        }
    }

    /**
     * Helper to determine if a return date is dangerously close (0-1 days or late).
     */
    public boolean isLoanUrgent(String daysStr) {
        if (daysStr == null || daysStr.trim().isEmpty()) return false;

        String normalized = daysStr.toUpperCase();

        // Mark as urgent if overdue or returning in 0 or 1 days
        return normalized.contains("OVERDUE") ||
                normalized.contains("0 DAYS") ||
                normalized.contains("1 DAY") ||
                normalized.startsWith("0") ||
                normalized.startsWith("1 ");
    }
}