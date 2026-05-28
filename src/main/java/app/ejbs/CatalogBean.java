package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.model.Book;
import app.websocket.SystemActivityServer; // 🌟 IMPORT YOUR WEBSOCKET SERVER
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class CatalogBean {

    @Inject
    private BookDAO bookDao;

    @Inject
    private BorrowDAO borrowDao;

    // ==========================================
    // BOOK CRUD OPERATIONS
    // ==========================================

    public List<Book> getAllBooks() {
        return bookDao.getAllBooks();
    }

    public Book getBookById(int id) {
        return bookDao.findById(id);
    }

    public void deleteBook(int bookId, String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Access Denied: Only administrators can delete records.");
        }

        try {
            // Capture target name before removing record state
            Book targetBook = bookDao.findById(bookId);
            String bookTitle = (targetBook != null) ? targetBook.getTitle() : "Unknown Book";

            bookDao.delete(bookId);

            // 🌟 WEBSOCKET BROADCAST: Log core catalog deletions
            SystemActivityServer.broadcastActivity("🚨 CATALOG REMOVAL: '" + bookTitle +
                    "' (ID: " + bookId + ") has been dropped from the repository systems by Admin.");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Updates book details.
     * We explicitly call bookDao.update(book) to ensure persistence
     * and avoid type mismatch errors during compilation.
     */
    public void updateBook(Book book, int addCopies) {
        if (addCopies != 0) {
            book.setQuantity(book.getQuantity() + addCopies);
            book.setAvailableCopies(book.getAvailableCopies() + addCopies);
        }

        // Explicitly updating to ensure the object state is synchronized
        bookDao.update(book);

        // 🌟 WEBSOCKET BROADCAST: Broadcast detailed catalog configuration updates
        SystemActivityServer.broadcastActivity("📋 CATALOG UPDATED: Core records for '" + book.getTitle() +
                "' (ID: " + book.getId() + ") updated. Inventory adjustment: " + (addCopies >= 0 ? "+" : "") + addCopies);
    }

    // ==========================================
    // 📊 SYSTEM-WIDE AGGREGATIONS & METRICS
    // ==========================================

    public int calculateTotalAvailableCopies() {
        return bookDao.getAllBooks().stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();
    }

    public int calculateSystemBorrowedCount() {
        return borrowDao.getMemberLoanCount(null);
    }

    public int getMemberBorrowedCount(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        return borrowDao.getMemberLoanCount(username.trim());
    }

    // ==========================================
    // DASHBOARD LABEL & STATUS HELPERS
    // ==========================================

    public String getBorrowedLabel(String role) {
        return "ADMIN".equalsIgnoreCase(role)
                ? "Total Copies Borrowed"
                : "My Borrowed Books";
    }

    public boolean isLoanUrgent(String daysStr) {
        if (daysStr == null || daysStr.trim().isEmpty()) return false;
        String normalized = daysStr.toUpperCase();
        return normalized.contains("OVERDUE") ||
                normalized.contains("0 DAYS") ||
                normalized.contains("1 DAY") ||
                normalized.startsWith("0") ||
                normalized.startsWith("1 ");
    }
}