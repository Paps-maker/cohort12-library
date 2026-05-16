package app.services;

import app.dao.BookDAO;
import app.model.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InventoryService {

    @Inject
    private BookDAO bookDao;

    /**
     * Updates the physical count of books in the library.
     */
    public boolean addCopies(int bookId, int amount) {
        // Logic remains to distinguish between automated system returns (+1)
        // and manual Admin inventory adjustments.
        boolean isAdminAction = (amount > 1 || amount < -1);
        return bookDao.updateInventory(bookId, amount, isAdminAction);
    }

    /**
     * Updated to use GenericDao findById lookup to resolve compilation errors.
     */
    public boolean isBookAvailable(int bookId) {
        Book book = bookDao.findById(bookId);
        return book != null && book.getAvailableCopies() > 0;
    }
}