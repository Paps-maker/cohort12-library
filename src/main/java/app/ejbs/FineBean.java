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
 * Updated to align with GenericDao for the Assessment-1-Livingstone Project.
 */
@Stateless
public class FineBean {

    @Inject
    private FineDAO fineDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineValidator fineValidator;

    // Central system rule
    private static final double RATE_PER_DAY = 50.0;

    public double calculateFineForRecord(int borrowId) {
        int lateDays = borrowDao.getOverdueDays(borrowId);
        return (lateDays > 0) ? (lateDays * RATE_PER_DAY) : 0.0;
    }

    public boolean payFine(String username, String fineIdParam) {
        if (fineValidator.validatePayment(username, fineIdParam) != null) {
            return false;
        }

        try {
            int fineId = Integer.parseInt(fineIdParam);
            return fineDao.payFine(fineId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Deletes a fine record using the standardized GenericDao method.
     */
    public boolean deleteFine(int fineId) {
        try {
            // ✅ Standardized method inherited from GenericDao
            fineDao.delete(fineId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public double getProjectedDebt(String username) {
        double currentFines = fineDao.getTotalUnpaid(username);
        List<String> activeLoans = borrowDao.getUserBorrowed(username);

        return currentFines + calculateOngoingLateFees(activeLoans);
    }

    public double getSystemTotalRisk() {
        double totalUnpaidFines = fineDao.getSystemTotalUnpaid();
        List<String> allActiveLoans = borrowDao.getAllBorrowed();

        return totalUnpaidFines + calculateOngoingLateFees(allActiveLoans);
    }

    // =========================================================================
    // INTERNAL CALCULATION ENGINE
    // =========================================================================

    private double calculateOngoingLateFees(List<String> records) {
        double accumulatedFees = 0.0;

        for (String record : records) {
            try {
                String[] parts = record.split("\\|");

                int borrowId = Integer.parseInt(
                        parts[0].replace("ID:", "").trim()
                );

                accumulatedFees += calculateFineForRecord(borrowId);

            } catch (Exception ignored) {
                // skip malformed records safely
            }
        }

        return accumulatedFees;
    }
}