package app.dao;

import app.model.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;


@ApplicationScoped
@Transactional
public class BookDAO extends GenericDao<Book, Integer> {


    // SECTION 0: STANDARD CRUD OVERRIDES

    public void update(Book book) {
        getEm().merge(book);
    }


    // SECTION 1: INVENTORY & STOCK MANAGEMENT

    public String getBookTitleById(int bookId) {
        try {
            return getEm().createQuery("SELECT b.title FROM Book b WHERE b.id = :id", String.class)
                    .setParameter("id", bookId)
                    .getSingleResult();
        } catch (Exception e) {
            return "Unknown Book";
        }
    }

    public boolean updateInventory(int bookId, int change, boolean isAdminUpdate) {
        try {
            Book book = findById(bookId);
            if (book == null) return false;

            if (isAdminUpdate) {
                book.setQuantity(book.getQuantity() + change);
            }
            book.setAvailableCopies(book.getAvailableCopies() + change);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBookDetailsAndInventory(Book webBook, int addCopies) {
        try {
            Book managedBook = findById(webBook.getId());
            if (managedBook == null) return false;

            managedBook.setTitle(webBook.getTitle());
            managedBook.setImageUrl(webBook.getImageUrl());
            managedBook.setDescription(webBook.getDescription());

            if (addCopies != 0) {
                boolean isAdmin = (addCopies > 1 || addCopies < -1);
                if (isAdmin) {
                    managedBook.setQuantity(managedBook.getQuantity() + addCopies);
                }
                managedBook.setAvailableCopies(managedBook.getAvailableCopies() + addCopies);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCopiesToExistingBook(int bookId, int amount) {
        boolean isAdmin = (amount > 1 || amount < -1);
        return updateInventory(bookId, amount, isAdmin);
    }

    // SECTION 2: CUSTOMIZED sorting

    public List<Book> getAllBooks() {
        return getEm().createQuery("SELECT b FROM Book b ORDER BY b.id DESC", Book.class)
                .getResultList();
    }
}