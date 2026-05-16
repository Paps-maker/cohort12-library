package app.dao;

import app.model.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * DATA ACCESS OBJECT: BOOKS
 * Optimized to inherit standard CRUD from GenericDao while maintaining
 * specialized inventory management logic.
 */
@ApplicationScoped
@Transactional
public class BookDAO extends GenericDao<Book, Integer> {

    // =========================================================================
    // SECTION 1: INVENTORY & STOCK MANAGEMENT (Specialized Logic)
    // =========================================================================

    /**
     * Efficient lookup for notification generation.
     */
    public String getBookTitleById(int bookId) {
        try {
            return getEm().createQuery("SELECT b.title FROM Book b WHERE b.id = :id", String.class)
                    .setParameter("id", bookId)
                    .getSingleResult();
        } catch (Exception e) {
            return "Unknown Book";
        }
    }

    /**
     * Specialized inventory update logic using JPQL.
     * Kept because this handles complex conditional updates that standard CRUD doesn't.
     */
    public boolean updateInventory(int bookId, int change, boolean isAdminUpdate) {
        try {
            String ql = isAdminUpdate
                    ? "UPDATE Book b SET b.totalQuantity = b.totalQuantity + :ch, b.availableCopies = b.availableCopies + :ch WHERE b.id = :id"
                    : "UPDATE Book b SET b.availableCopies = b.availableCopies + :ch WHERE b.id = :id";

            int updated = getEm().createQuery(ql)
                    .setParameter("ch", change)
                    .setParameter("id", bookId)
                    .executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCopiesToExistingBook(int bookId, int amount) {
        // Logic specific to administrative inventory adjustments
        boolean isAdmin = (amount > 1 || amount < -1);
        return updateInventory(bookId, amount, isAdmin);
    }

    // =========================================================================
    // SECTION 2: CUSTOMIZED LOOKUPS
    // =========================================================================

    public List<Book> getAllBooks() {
        // Overrides standard findAll to ensure descending order for the UI
        return getEm().createQuery("SELECT b FROM Book b ORDER BY b.id DESC", Book.class)
                .getResultList();
    }
}