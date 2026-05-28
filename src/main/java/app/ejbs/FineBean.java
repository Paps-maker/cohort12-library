package app.ejbs;

import app.dao.FineDAO;
import app.dao.BorrowDAO;
import app.model.Fine;
import app.validation.FineValidator;
import app.websocket.SystemActivityServer; // 🌟 IMPORT YOUR WEBSOCKET SERVER
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Stateless
public class FineBean {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private FineDAO fineDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineValidator fineValidator;

    private static final double RATE_PER_DAY = 50.0; // 50 KSH Per Day

    public FineBean() {}

    // CONSTRUCTOR INJECTION FOR FINE VALIDATOR
    @Inject
    public FineBean(FineValidator fineValidator) {
        this.fineValidator = fineValidator;
    }

    // =========================================================================
    // 📊 BASIC READ OPERATIONS
    // =========================================================================

    public double getUnpaidFines(String username) {
        if (username == null || username.trim().isEmpty()) return 0.0;
        return fineDao.getTotalUnpaid(username.trim());
    }

    public List<String> getMemberFineHistory(String username) {
        if (username == null || username.trim().isEmpty()) return List.of();
        return fineDao.getUserFines(username.trim());
    }

    public List<String> getAdminFineHistory() {
        return fineDao.getAllFines();
    }

    // =========================================================================
    // 📊 ANALYTICS DATA PROVIDERS & MAP MAPPERS
    // =========================================================================

    /**
     * Maps active fines grouped by user.
     */
    public Map<String, List<Fine>> getAllUserFinesMap() {
        try {
            List<Fine> allFines = em.createQuery("SELECT f FROM Fine f JOIN FETCH f.user", Fine.class)
                    .getResultList();

            return allFines.stream()
                    .collect(Collectors.groupingBy(fine -> fine.getUser().getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    /**
     * Retrieves total fines incurred for each of the last 7 days.
     */
    public List<Double> getLastSevenDaysDebt() {
        List<Double> sevenDayTrend = new ArrayList<>();
        try {
            // 🌟 FIX: Changed 'f.dateCreated' to match your Fine.java property field 'f.createdAt'
            String query = "SELECT SUM(f.amount) FROM Fine f " +
                    "WHERE f.createdAt >= :startDate " +
                    "GROUP BY FUNCTION('DATE', f.createdAt) " +
                    "ORDER BY FUNCTION('DATE', f.createdAt) ASC";

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            List<Double> results = em.createQuery(query, Double.class)
                    .setParameter("startDate", sevenDaysAgo)
                    .getResultList();

            for (int i = 0; i < 7; i++) {
                sevenDayTrend.add(i < results.size() ? results.get(i) : 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            for (int i = 0; i < 7; i++) sevenDayTrend.add(0.0);
        }
        return sevenDayTrend;
    }

    // =========================================================================
    // 💸 TRANSACTIONAL MUTATIONS & PAYMENT FLOWS
    // =========================================================================

    public boolean processFinePayment(String username, String fineIdParam) {
        if (fineValidator.validatePayment(username, fineIdParam) != null) return false;

        try {
            int fineId = Integer.parseInt(fineIdParam.trim());

            // 🌟 TRANSACTION REDUCTION ARCHITECTURE: Update entity via EntityManager directly to drop the balance state
            Fine fine = em.find(Fine.class, fineId);
            if (fine != null && "UNPAID".equalsIgnoreCase(fine.getStatus())) {
                fine.setStatus("PAID");
                fine.setAmount(0.0); // 🌟 Drops outstanding fee amounts to 0 instantly on payment interaction
                em.merge(fine);

                // Keep DAO sync fallback layer if fineDao has external state logic
                fineDao.payFine(fineId);

                // 🌟 WEBSOCKET BROADCAST: Broadcast fine clearing transactions
                SystemActivityServer.broadcastActivity("💸 FINE SETTLED: Member [" + username +
                        "] has successfully paid and cleared fine record ID: #" + fineId);
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean deleteFineRecord(int fineId) {
        try {
            fineDao.delete(fineId);

            // 🌟 WEBSOCKET BROADCAST: Log direct admin intervention updates
            SystemActivityServer.broadcastActivity("🚨 FINE WAIVED: Fine entry state ID: #" +
                    fineId + " was manually deleted/waived by an Administrator.");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // 📉 FINANCIAL PROJECTION COMPUTATION ENGINE
    // =========================================================================

    public double calculateFineForRecord(int borrowId) {
        int lateDays = borrowDao.getOverdueDays(borrowId);
        return (lateDays > 0) ? (lateDays * RATE_PER_DAY) : 0.0;
    }

    public double getProjectedLateFees(String username) {
        if (username == null || username.trim().isEmpty()) return 0.0;
        double currentFines = fineDao.getTotalUnpaid(username.trim());
        List<String> activeLoans = borrowDao.getUserBorrowed(username.trim());
        return currentFines + calculateOngoingLateFees(activeLoans);
    }

    public double getTotalSystemRiskDebt() {
        double totalUnpaidFines = fineDao.getSystemTotalUnpaid();
        List<String> allActiveLoans = borrowDao.getAllBorrowed();
        return totalUnpaidFines + calculateOngoingLateFees(allActiveLoans);
    }

    // =========================================================================
    // 🛠️ INTERNAL PARSING ENGINE UTILITIES
    // =========================================================================

    private double calculateOngoingLateFees(List<String> records) {
        double accumulatedFees = 0.0;
        if (records == null) return accumulatedFees;

        for (String record : records) {
            try {
                String[] parts = record.split("\\|");
                int borrowId = Integer.parseInt(parts[0].replace("ID:", "").trim());
                accumulatedFees += calculateFineForRecord(borrowId);
            } catch (Exception ignored) {
                // skip malformed records
            }
        }
        return accumulatedFees;
    }
}