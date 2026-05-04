package app;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.ejbs.CatalogBean;
import app.ejbs.BorrowingBean;
import app.ejbs.FineBean;
import app.model.Book;
import app.validation.BookValidator;
import app.validation.BorrowValidator;
import app.validation.FineValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import app.events.LibraryEvent; // ✅ Corrected Import
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event; // ✅ Added for Event firing
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE LAYER (Business Logic Facade)
 * Fully synchronized with the updated BookDAO inventory logic.
 */
@ApplicationScoped
public class LibraryService {

    @Inject
    private BookDAO bookDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineDAO fineDao;

    @Inject
    private CatalogBean catalogBean;

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private FineBean fineBean;

    @Inject
    private Event<LibraryEvent> eventPublisher; // ✅ Added Event Publisher

    private final FineValidator fineValidator;
    private final BorrowValidator borrowValidator;
    private BookValidator bookValidator;
    private ReturnValidator returnValidator;

    /**
     * Default constructor for CDI proxying.
     */
    protected LibraryService() {
        this.fineValidator = null;
        this.borrowValidator = null;
    }

    /**
     * Constructor injection for required validators.
     */
    @Inject
    public LibraryService(
            @Named("fineValidator") FineValidator fineValidator,
            @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW) BorrowValidator borrowValidator) {
        this.fineValidator = fineValidator;
        this.borrowValidator = borrowValidator;
    }

    @Inject
    public void setBookValidator(@ValidatorQualifier(ValidatorQualifier.ValidationChoice.BOOK) BookValidator bookValidator) {
        this.bookValidator = bookValidator;
    }

    @Inject
    public void setReturnValidator(@Named("returnValidator") ReturnValidator returnValidator) {
        this.returnValidator = returnValidator;
    }

    // =========================================================================
    // SECTION 1: CATALOG & DASHBOARD (Updated to use CatalogBean)
    // =========================================================================

    public List<Book> getAllBooks() {
        return catalogBean.getAvailableBooks();
    }

    public int getAvailableCount() {
        return catalogBean.getTotalAvailableCopies();
    }

    public int getBorrowedCountForUser(String username, String role) {
        if ("ADMIN".equals(role)) {
            return catalogBean.getSystemBorrowedCount();
        }
        return borrowDao.getMemberLoanCount(username);
    }

    public String getBorrowedLabel(String role) {
        return "ADMIN".equals(role) ? "Total Copies Borrowed" : "My Borrowed Books";
    }

    public int getDaysUntilAvailable(String bookTitle) {
        return borrowDao.getDaysLeft(bookTitle);
    }

    public boolean isBookAvailable(int bookId) {
        return bookDao.getAvailableCopiesCount(bookId) > 0;
    }

    public boolean isLoanUrgent(String statusText) {
        if (statusText == null) return false;
        String upper = statusText.toUpperCase();
        return upper.contains("OVERDUE") || upper.contains("DUE TODAY") || upper.contains("1 DAY LEFT");
    }
    // =========================================================================
    // SECTION 2: FINES & DEBT
    // =========================================================================

    public double getUnpaidFines(String username) {
        return fineDao.getTotalUnpaid(username);
    }

    public double getProjectedLateFees(String username) {
        double currentDebt = fineDao.getTotalUnpaid(username);
        double projected = fineBean.getProjectedDebt(username);
        return Math.max(0, projected - currentDebt);
    }

    public double getTotalSystemRiskDebt() {
        return fineBean.getSystemTotalRisk();
    }

    // =========================================================================
    // SECTION 3: INVENTORY & OPERATIONS
    // =========================================================================

    public List<String> getAdminBorrowedRecords() {
        return borrowDao.getAllBorrowed();
    }

    public List<String> getMemberActiveLoans(String username) {
        return borrowDao.getUserBorrowed(username);
    }

    public String attemptBorrow(String username, String role, String bookIdParam, String daysParam) {
        if ("ADMIN".equals(role)) return "Administrative accounts cannot borrow.";
        if (bookIdParam == null || bookIdParam.trim().isEmpty()) return "Please choose a book.";

        try {
            int bookId = Integer.parseInt(bookIdParam);
            int days = Integer.parseInt(daysParam);

            if (!isBookAvailable(bookId)) {
                return "Checkout Blocked: This book is currently out of stock.";
            }

            // 1. Perform the business logic
            String result = borrowingBean.validateAndBorrow(username, bookId, days);

            // 2. ✅ Fire Event if successful
            if (result != null && result.contains("Success")) {
                // Matches constructor: LibraryEvent(type, email, bookTitle, status)
                eventPublisher.fire(new LibraryEvent("BORROW", username, "Book ID: " + bookId, result));
                System.out.println(">>> LibraryService: Borrow Event Fired for " + username);
            }

            return result;
        } catch (NumberFormatException e) {
            return "Invalid format for book selection or duration.";
        }
    }

    public String processReturnRequest(String role, String borrowIdParam) {
        return borrowingBean.processReturn(role, borrowIdParam);
    }

    public boolean addCopies(int bookId, int amount) {
        boolean isAdminAction = (amount > 1 || amount < -1);
        return bookDao.updateInventory(bookId, amount, isAdminAction);
    }

    public boolean processFinePayment(String username, String fineIdParam) {
        return fineBean.payFine(username, fineIdParam);
    }

    public boolean deleteFineRecord(int fineId) {
        return fineBean.deleteFine(fineId);
    }

    public List<String> getMemberFineHistory(String username) {
        return fineDao.getUserFines(username);
    }

    public List<String> getAdminFineHistory() {
        return fineDao.getAllFines().stream().map(fine -> {
            if (fine.contains("Status: UNPAID")) {
                return fine.replace("Status: UNPAID", " UNPAID FINES");
            }
            return fine;
        }).collect(Collectors.toList());
    }
}