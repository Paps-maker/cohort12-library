package app.ejbs;

import app.dao.FineDAO;
import app.dao.BorrowDAO;
import app.validation.FineValidator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

/**
 * SERVICE EJB: FINE MANAGEMENT
 * Handles payment processing and financial risk projections.
 */
@Stateless
public class FineBean {

    @Inject
    private FineDAO fineDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineValidator fineValidator;

    /**
     * Processes a fine payment after validation.
     */
    public boolean payFine(String username, String fineIdParam) {
        // Validation ensures the fine belongs to the user and is actually unpaid
        if (fineValidator.validatePayment(username, fineIdParam) != null) return false;

        try {
            int fineId = Integer.parseInt(fineIdParam);
            return fineDao.payFine(fineId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Admin-only operation to remove a fine record.
     */
    public boolean deleteFine(int fineId) {
        return fineDao.deleteFine(fineId);
    }

    /**
     * ✅ UPDATED: Debt Projection
     * Calculates current unpaid fines PLUS estimated fees for books currently overdue.
     */
    public double getProjectedDebt(String username) {
        double currentFines = fineDao.getTotalUnpaid(username);

        // We pass the raw string list from BorrowDAO and parse the IDs
        List<String> activeLoans = borrowDao.getUserBorrowed(username);
        return currentFines + calculateOngoingLateFees(activeLoans);
    }

    /**
     * ✅ UPDATED: System-wide Financial Risk
     * Calculates all unpaid fines in the system plus all accumulating late fees.
     */
    public double getSystemTotalRisk() {
        double totalUnpaidFines = fineDao.getSystemTotalUnpaid();

        // getAllBorrowed now correctly joins physical copies but still returns IDs
        List<String> allActiveLoans = borrowDao.getAllBorrowed();
        return totalUnpaidFines + calculateOngoingLateFees(allActiveLoans);
    }

    /**
     * Internal helper to calculate potential fines for books not yet returned.
     * Uses the KSH 50.00 per day standard.
     */
    private double calculateOngoingLateFees(List<String> records) {
        double accumulatedFees = 0.0;
        for (String record : records) {
            try {
                // Extracts the "ID: X" part of the formatted string from BorrowDAO
                int borrowId = Integer.parseInt(record.split("\\|")[0].replace("ID:", "").trim());

                // Uses the updated BorrowDAO method that checks due_date vs now
                int lateDays = borrowDao.getOverdueDays(borrowId);

                if (lateDays > 0) {
                    accumulatedFees += (lateDays * 50.0);
                }
            } catch (Exception e) {
                // Skip malformed strings to prevent calculation crashes
            }
        }
        return accumulatedFees;
    }
}