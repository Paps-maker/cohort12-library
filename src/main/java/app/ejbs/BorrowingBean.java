package app.ejbs;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.dao.UserDAO;
import app.events.LibraryEvent;
import app.model.Book;
import app.model.BorrowedBook;
import app.model.Fine;
import app.model.User;
import app.validation.BorrowValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import app.websocket.SystemActivityServer;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Stateless
public class BorrowingBean {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private BookDAO bookDao;

    @Inject
    private FineDAO fineDao;

    @Inject
    private UserDAO userDao;

    @Inject
    private Event<LibraryEvent> eventPublisher;

    @Inject
    @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW)
    private BorrowValidator borrowValidator;

    @Inject
    @Named("returnValidator")
    private ReturnValidator returnValidator;

    public BorrowingBean() {}


    //  ACTIVE CHECKS


    public List<String> getAdminBorrowedRecords() {
        return borrowDao.getAllBorrowed();
    }

    public List<String> getMemberActiveLoans(String username) {
        return borrowDao.getUserBorrowed(username);
    }

    public int getMemberBorrowedCount(String username) {
        if (username == null || username.isEmpty()) {
            return 0;
        }
        return borrowDao.getMemberLoanCount(username);
    }

    public boolean isUserRegistered(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        return userDao.checkUserExists(identifier.trim());
    }


    //  BORROW OPERATION


    public String attemptBorrow(String username, String bookIdParam, String daysParam) {
        int bookId = parseInt(bookIdParam);
        int days = parseInt(daysParam);

        if (bookId <= 0 || days <= 0) {
            return "Blocked: Invalid book selection or borrow duration values.";
        }

        int currentLoanCount = borrowDao.getMemberLoanCount(username);
        double unpaidFines = fineDao.getTotalUnpaid(username);

        Book book = bookDao.findById(bookId);
        int availableCount = (book != null) ? book.getAvailableCopies() : 0;
        boolean available = (availableCount > 0);

        String error = borrowValidator.canBorrow("ACTIVE", currentLoanCount, available, unpaidFines);
        if (error != null) return error;

        User userRecord = userDao.findUserByUsername(username);
        if (userRecord == null) {
            return "Blocked: Member registration entity record could not be found.";
        }
        if (book == null) {
            return "Blocked: Target cataloged book does not exist.";
        }

        try {
            BorrowedBook record = new BorrowedBook(userRecord, book, days);
            borrowDao.save(record);
            int generatedBorrowId = record.getId();

            bookDao.addCopiesToExistingBook(bookId, -1);

            String bookTitle = book.getTitle();
            String recipientEmail = (userRecord.getEmail() != null) ? userRecord.getEmail() : username;

            String message = String.format("REC #%d | Book: %s (ID: %d)",
                    generatedBorrowId, bookTitle, bookId);

            eventPublisher.fire(new LibraryEvent("BORROW", recipientEmail, message, "Active"));

            SystemActivityServer.broadcastActivity(" BOOK CHECKOUT: Member [" + username + "] has borrowed '" +
                    bookTitle + "' (ID: " + bookId + ") for " + days + " days. Loan Transaction ID: #" + generatedBorrowId);

            return "Success: Checked out " + bookTitle + "! Return within " + days + " days.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Transaction failed during database write execution.";
        }
    }


    //  RETURN OPERATION (Fixed Transaction Failure Vulnerabilities)


    public String processReturnRequest(String role, String borrowIdParam) {
        if (borrowIdParam == null || borrowIdParam.trim().isEmpty()) {
            return "Error: Missing borrowing transaction identifier.";
        }

        try {
            // Clean dynamic string parameters down to pure numeric layout elements
            String cleanIdStr = borrowIdParam.replaceAll("[^0-9]", "").trim();
            if (cleanIdStr.isEmpty()) {
                return "Error: Invalid transaction identity layout.";
            }

            int borrowId = Integer.parseInt(cleanIdStr);

            // 1. Fire Validation rules via ReturnValidator component
            String error = returnValidator.validateReturn(role, cleanIdStr);
            if (error != null) return error;

            // 2. Safely extract target entity matching the transaction criteria
            BorrowedBook borrowRecord = borrowDao.findById(borrowId);
            if (borrowRecord == null) {
                return "Error: Borrow record not found inside database storage.";
            }

            Book book = borrowRecord.getBook();
            User user = borrowRecord.getUser();

            int bookId = (book != null) ? book.getId() : -1;
            String memberUsername = (user != null) ? user.getUsername() : "Unknown Member";
            String bookTitle = (book != null) ? book.getTitle() : "Unknown Book";

            int daysLate = borrowDao.getOverdueDays(borrowId);
            String fineStatus = "No Fine";
            String wsFineDetails = "None";

            // 3. Process Fine Allocation if thresholds are crossed
            if (daysLate > 0 && user != null && book != null) {
                Fine fine = new Fine(user, daysLate * 50.0, daysLate, borrowRecord, book);
                fineDao.save(fine);
                fineStatus = "Fine Issued: KSH " + fine.getAmount();
                wsFineDetails = "⚠️ KSH " + fine.getAmount() + " due to " + daysLate + " days delay";
            }

            // 4. Mutation phase - Drop relation records and re-increment inventory stock counters
            borrowDao.delete(borrowId);

            if (bookId != -1) {
                bookDao.addCopiesToExistingBook(bookId, 1);
            }

            // 5. Fire external systems notification logs
            String recipientEmail = (user != null && user.getEmail() != null) ? user.getEmail() : memberUsername;
            String message = String.format("Return Processed for REC #%d | Book: %s", borrowId, bookTitle);

            eventPublisher.fire(new LibraryEvent("RETURN", recipientEmail, message, fineStatus));

            // 6. Broadcast structural processing success across open WebSockets
            SystemActivityServer.broadcastActivity(" BOOK RETURNED: '" + bookTitle + "' has been handed back by Member [" +
                    memberUsername + "]. Inventory restored (+1). Fine Status: " + wsFineDetails);

            return "Success: " + bookTitle + " returned successfully. " + fineStatus;

        } catch (NumberFormatException e) {
            return "Error: Invalid Borrow ID layout format.";
        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE WITHIN RETURN TRANSACTION BLOCK:");
            e.printStackTrace(); // This logs to the GlassFish/Payara console for trace evaluation
            return "Critical Return Error inside transactional processing vault: " + e.getMessage();
        }
    }


    //  ANALYTICS & DASHBOARD DATA PROVIDERS
    public Map<String, List<BorrowedBook>> getAllUserLoansMap() {
        try {
            List<BorrowedBook> allLoans = em.createQuery("SELECT b FROM BorrowedBook b JOIN FETCH b.user JOIN FETCH b.book", BorrowedBook.class)
                    .getResultList();

            return allLoans.stream()
                    .collect(Collectors.groupingBy(loan -> loan.getUser().getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    public Map<String, Integer> getTopBorrowedTitles(int limit) {
        Map<String, Integer> topTitles = new LinkedHashMap<>();
        try {
            String query = "SELECT b.book.title, COUNT(b) FROM BorrowedBook b GROUP BY b.book.title ORDER BY COUNT(b) DESC";
            List<Object[]> results = em.createQuery(query, Object[].class)
                    .setMaxResults(limit)
                    .getResultList();

            for (Object[] result : results) {
                topTitles.put((String) result[0], ((Long) result[1]).intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topTitles;
    }

    public Map<String, Integer> getUserActivityDistribution() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        try {
            long totalBorrowed = em.createQuery("SELECT COUNT(b) FROM BorrowedBook b", Long.class).getSingleResult();
            long totalUsers = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();

            stats.put("ActiveLoans", (int) totalBorrowed);
            stats.put("RegisteredUsers", (int) totalUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }


    // ESTIMATION & SCHEDULING LOGIC
    public LocalDateTime getEarliestReturnDateByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        try {
            String query = "SELECT MIN(b.dueDate) FROM BorrowedBook b WHERE b.book.id IN " +
                    "(SELECT bk.id FROM Book bk WHERE LOWER(bk.title) = LOWER(:title))";

            return em.createQuery(query, LocalDateTime.class)
                    .setParameter("title", title.trim())
                    .getSingleResult();
        } catch (Exception e) {
            return LocalDateTime.now().plusDays(7);
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return -1;
        }
    }
}