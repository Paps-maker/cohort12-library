package app.dao;

import app.model.Fine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DATA ACCESS OBJECT: FINES
 * Optimized to inherit CRUD from GenericDao for the Assessment-1-Livingstone Project.
 */
@ApplicationScoped
@Transactional
public class FineDAO extends GenericDao<Fine, Integer> {

    // =========================================================================
    // SECTION 1: SPECIALIZED RETRIEVAL (Reporting)
    // =========================================================================

    /**
     * MEMBER VIEW: Optimized JPQL Join to fetch book titles directly.
     */
    public List<String> getUserFines(String username) {
        List<Object[]> results = getEm().createQuery(
                        "SELECT f.id, f.createdAt, f.amount, f.status, bk.title " +
                                "FROM Fine f LEFT JOIN Book bk ON f.bookId = bk.id " +
                                "WHERE f.username = :user ORDER BY f.createdAt DESC", Object[].class)
                .setParameter("user", username)
                .getResultList();

        return results.stream()
                .map(res -> res[0] + " | " +
                        (res[1] != null ? res[1].toString() : "N/A") + " | " +
                        String.format("%.2f", (Double) res[2]) + " | " +
                        res[3] + " | " +
                        "Book: " + (res[4] != null ? res[4] : "Unknown Book"))
                .collect(Collectors.toList());
    }

    /**
     * ADMIN VIEW: Full system audit trail.
     */
    public List<String> getAllFines() {
        List<Object[]> results = getEm().createQuery(
                        "SELECT f.id, f.username, f.amount, f.status, f.createdAt, bk.title " +
                                "FROM Fine f LEFT JOIN Book bk ON f.bookId = bk.id " +
                                "ORDER BY f.createdAt DESC", Object[].class)
                .getResultList();

        return results.stream()
                .map(res -> res[0] + " | User: " + res[1].toString().toUpperCase() + " | " +
                        String.format("%.2f", (Double) res[2]) + " | " +
                        res[3] + " | " +
                        (res[4] != null ? res[4].toString() : "N/A") + " | " +
                        "Book: " + (res[5] != null ? res[5] : "[Deleted]"))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // SECTION 2: AGGREGATE CALCULATIONS
    // =========================================================================

    public double getTotalUnpaid(String username) {
        Double total = getEm().createQuery(
                        "SELECT SUM(f.amount) FROM Fine f WHERE f.username = :user AND f.status = 'UNPAID'", Double.class)
                .setParameter("user", username)
                .getSingleResult();
        return (total != null) ? total : 0.0;
    }

    public double getSystemTotalUnpaid() {
        Double total = getEm().createQuery(
                        "SELECT SUM(f.amount) FROM Fine f WHERE f.status = 'UNPAID'", Double.class)
                .getSingleResult();
        return (total != null) ? total : 0.0;
    }

    // =========================================================================
    // SECTION 3: BUSINESS LOGIC
    // =========================================================================

    /**
     * Standard update logic for payments.
     * Uses inherited findById and save (merge).
     */
    public boolean payFine(int fineId) {
        try {
            Fine fine = findById(fineId);
            if (fine != null) {
                fine.setStatus("PAID");
                save(fine);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}