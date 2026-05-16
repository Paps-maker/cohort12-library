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



    public List<Book> getAvailableBooks() {
        return bookDao.getAllBooks();
    }

    /**

     * Sums up available_copies across all titles.
     */
    public int getTotalAvailableCopies() {
        return bookDao.getAllBooks().stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();
    }

    /**
    getBorrowedCountForUser(..., "ADMIN") call.
     * Calculates: Total System Capacity - Current Available Stock.
     */
    public int getSystemBorrowedCount() {
        List<Book> books = bookDao.getAllBooks();
        int totalPhysicalInventory = books.stream().mapToInt(Book::getTotalQuantity).sum();
        int currentAvailable = books.stream().mapToInt(Book::getAvailableCopies).sum();


        return Math.max(0, totalPhysicalInventory - currentAvailable);
    }


    public int getMemberBorrowedCount(String username) {
        return borrowDao.getMemberLoanCount(username);
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