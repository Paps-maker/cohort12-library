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
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE LAYER (Business Logic Facade)
 * Now coordinates access via specialized Stateless EJBs.
 */
@ApplicationScoped
public class LibraryService {

    @Inject
    private BookDAO bookDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineDAO fineDao;

    // EJB Injections
    @Inject
    private CatalogBean catalogBean;

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private FineBean fineBean;

    private final FineValidator fineValidator;
    private final BorrowValidator borrowValidator;
    private BookValidator bookValidator;
    private ReturnValidator returnValidator;

    /**
     * NO-ARGS CONSTRUCTOR (CDI requirement)
     */
    protected LibraryService() {
        this.fineValidator = null;
        this.borrowValidator = null;
    }

    /**
     * CONSTRUCTOR INJECTION
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
    // SECTION 1: CATALOG & DASHBOARD (Delegated to CatalogBean)
    // =========================================================================

    public List<Book> getAllBooks() {
        return catalogBean.getAllBooks();
    }

    public int getAvailableCount() {
        return catalogBean.calculateAvailableCount();
    }

    public int getBorrowedCountForUser(String username, String role) {
        return catalogBean.getBorrowedCount(username, role);
    }

    public String getBorrowedLabel(String role) {
        return "ADMIN".equals(role) ? "Total Borrowed" : "My Borrowed";
    }

    public int getDaysUntilAvailable(String bookTitle) {
        return borrowDao.getDaysLeft(bookTitle);
    }

    // =========================================================================
    // SECTION 2: BORROWING (CLEAN ATOMIC TRANSACTION)
    // =========================================================================

    public double getUnpaidFines(String username) {
        return fineDao.getTotalUnpaid(username);
    }

    public boolean isBookAvailable(int bookId) {
        return !borrowDao.isBookBorrowed(bookId);
    }

    /**
     * The primary entry point for borrowing.
     * Handles validation and DB insertion via BorrowingBean in one step.
     * @return null on success, or an error message on failure.
     */
    public String attemptBorrow(String username, String role, String bookIdParam, String daysParam) {
        if ("ADMIN".equals(role)) return "Administrative accounts cannot borrow.";
        if (bookIdParam == null || bookIdParam.isEmpty()) return "Please choose a book.";

        try {
            int bookId = Integer.parseInt(bookIdParam);
            int days = Integer.parseInt(daysParam);

            // The EJB ensures the transaction is atomic and checks availability/fines/limits
            return borrowingBean.validateAndBorrow(username, bookId, days);
        } catch (NumberFormatException e) {
            return "Invalid format for book selection or duration.";
        }
    }
    // =========================================================================
    // SECTION 3: RETURNS (Delegated to BorrowingBean)
    // =========================================================================

    public String processReturnRequest(String role, String borrowIdParam) {
        return borrowingBean.processReturn(role, borrowIdParam);
    }

    // =========================================================================
    // SECTION 4: FINES & ROLE-BASED HISTORY (Delegated to FineBean)
    // =========================================================================

    public boolean processFinePayment(String username, String fineIdParam) {
        return fineBean.payFine(username, fineIdParam);
    }

    public boolean deleteFineRecord(int fineId) {
        return fineBean.deleteFine(fineId);
    }

    public List<String> getAdminBorrowedRecords() { return borrowDao.getAllBorrowed(); }

    public List<String> getMemberActiveLoans(String username) { return borrowDao.getUserBorrowed(username); }

    public List<String> getMemberFineHistory(String username) { return fineDao.getUserFines(username); }

    public List<String> getAdminFineHistory() {
        return fineDao.getAllFines().stream().map(fine -> {
            if (fine.contains("Status: UNPAID")) {
                return fine.replace("Status: UNPAID", "UNPAID FINES");
            }
            return fine;
        }).collect(Collectors.toList());
    }

    public boolean isLoanUrgent(String daysStr) {
        return catalogBean.isLoanUrgent(daysStr);
    }

    // =========================================================================
    // SECTION 5: REAL-TIME DEBT CALCULATIONS (Delegated to FineBean)
    // =========================================================================

    public double getProjectedLateFees(String username) {
        return fineBean.getProjectedDebt(username) - fineDao.getTotalUnpaid(username);
    }

    public double getTotalSystemRiskDebt() {
        return fineBean.getSystemTotalRisk();
    }
}