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

    public List<Book> getAllBooks() {
        return bookDao.getAllBooks();
    }

    public int calculateAvailableCount() {
        return bookDao.getAllBooks().size() - borrowDao.getAllBorrowed().size();
    }

    public int getBorrowedCount(String username, String role) {
        return "ADMIN".equals(role) ?
                borrowDao.getAllBorrowed().size() :
                borrowDao.getUserBorrowed(username).size();
    }

    public boolean isLoanUrgent(String daysStr) {
        if (daysStr == null) return false;
        String normalized = daysStr.toUpperCase();
        return normalized.contains("OVERDUE") || normalized.startsWith("0") || normalized.startsWith("1 ");
    }
}