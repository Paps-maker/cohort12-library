package app.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DATA ACCESS OBJECT: Analytics
 * Optimized for aggregate reporting in the Assessment-1-Livingstone Project.
 */
@ApplicationScoped
public class AnalyticsDAO {

    @PersistenceContext(unitName = "TrainingAppPU")
    private EntityManager em;

    // =========================================================================
    // SECTION 1: KEY PERFORMANCE INDICATORS (KPIs)
    // =========================================================================

    public double getTotalRevenue() {
        Double result = em.createQuery(
                        "SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'PAID'", Double.class)
                .getSingleResult();
        return (result != null) ? result : 0.0;
    }

    public int getUnpaidFineCount() {
        Long result = em.createQuery(
                        "SELECT COUNT(DISTINCT f.username) FROM Fine f WHERE f.status = 'UNPAID'", Long.class)
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

    public int getTotalBookVolume() {
        // Correctly targets the totalQuantity field from your Book entity
        Long result = em.createQuery("SELECT SUM(b.totalQuantity) FROM Book b", Long.class)
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

    public int getActiveOverdueCount() {
        // Uses CURRENT_TIMESTAMP to match your LocalDateTime fields in BorrowedBook
        Long result = em.createQuery(
                        "SELECT COUNT(b) FROM BorrowedBook b WHERE b.dueDate < CURRENT_TIMESTAMP", Long.class)
                .getSingleResult();
        return (result != null) ? result.intValue() : 0;
    }

    // =========================================================================
    // SECTION 2: TRENDS & DISTRIBUTIONS
    // =========================================================================

    public Map<String, Integer> getTopBooks() {
        Map<String, Integer> data = new LinkedHashMap<>();
        // Performs a cross-join between BorrowedBook and Book to retrieve titles
        String jpql = "SELECT bk.title, COUNT(b.id) FROM BorrowedBook b, Book bk " +
                "WHERE b.bookId = bk.id GROUP BY bk.title ORDER BY COUNT(b.id) DESC";

        List<Object[]> results = em.createQuery(jpql, Object[].class)
                .setMaxResults(5)
                .getResultList();

        results.forEach(row -> data.put((String) row[0], ((Long) row[1]).intValue()));
        return data;
    }

    public Map<String, Integer> getMostActiveUsers() {
        Map<String, Integer> data = new LinkedHashMap<>();
        List<Object[]> results = em.createQuery(
                        "SELECT b.username, COUNT(b.id) FROM BorrowedBook b " +
                                "GROUP BY b.username ORDER BY COUNT(b.id) DESC", Object[].class)
                .setMaxResults(5)
                .getResultList();

        results.forEach(row -> data.put((String) row[0], ((Long) row[1]).intValue()));
        return data;
    }

    /**
     * Native Query remains the most reliable way to handle MySQL-specific DATE functions.
     */
    public Map<String, Double> getDailyDebtTrend() {
        Map<String, Double> data = new LinkedHashMap<>();

        String sql = "SELECT DATE(created_at) as trend_date, SUM(amount) as daily_sum " +
                "FROM fine " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY trend_date ASC";

        @SuppressWarnings("unchecked")
        List<Object[]> results = em.createNativeQuery(sql).getResultList();

        double runningTotal = 0.0;
        for (Object[] row : results) {
            // Number handling ensures compatibility across different JDBC driver return types
            runningTotal += ((Number) row[1]).doubleValue();
            data.put(row[0].toString(), runningTotal);
        }

        if (data.isEmpty()) {
            data.put(java.time.LocalDate.now().toString(), 0.0);
        }

        return data;
    }
}