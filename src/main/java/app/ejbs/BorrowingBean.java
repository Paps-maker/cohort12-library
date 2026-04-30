package app.ejbs;

import app.dao.BorrowDAO;
import app.dao.FineDAO;
import app.validation.BorrowValidator;
import app.validation.ReturnValidator;
import app.validation.ValidatorQualifier;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Stateless
public class BorrowingBean {

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineDAO fineDao;

    @Inject
    @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BORROW)
    private BorrowValidator borrowValidator;

    @Inject
    @Named("returnValidator")
    private ReturnValidator returnValidator;

    public String validateAndBorrow(String username, int bookId, int days) {
        int currentLoanCount = borrowDao.getMemberLoanCount(username);
        double unpaidFines = fineDao.getTotalUnpaid(username);
        boolean available = !borrowDao.isBookBorrowed(bookId);

        String error = borrowValidator.canBorrow("ACTIVE", currentLoanCount, available, unpaidFines);
        if (error != null) return error;

        return borrowDao.borrowBook(username, bookId, days) ? null : "Transaction failed.";
    }

    public String processReturn(String role, String borrowIdParam) {
        String error = returnValidator.validateReturn(role, borrowIdParam);
        if (error != null) return error;

        try {
            int borrowId = Integer.parseInt(borrowIdParam);
            String member = borrowDao.getMemberByBorrowId(borrowId);
            int daysLate = borrowDao.getOverdueDays(borrowId);

            if (daysLate > 0 && member != null) {
                fineDao.insertFine(member, daysLate * 50.0, daysLate, borrowId);
            }

            return borrowDao.returnBook(borrowId) ? null : "Database Error.";
        } catch (Exception e) {
            return "Critical Return Error.";
        }
    }
}