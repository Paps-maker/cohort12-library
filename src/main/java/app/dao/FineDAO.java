package app.dao;

import app.model.Fine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DATA ACCESS OBJECT: FINES
 * Optimized to inherit CRUD from GenericDao
 */
@ApplicationScoped
@Transactional
public class FineDAO extends GenericDao<Fine, Integer> {

    //  REUSE DATE FORMATTER: Avoid creating new formatter instances inside loops
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // SECTION 1: SPECIALIZED RETRIEVAL (Reporting)



     // MEMBER VIEW: Optimized JPQL path navigation to fetch book titles directly.

    public List<String> getUserFines(String username) {
        List<Object[]> results = getEm().createQuery(
                        "SELECT f.id, f.createdAt, f.amount, f.status, f.book.title " +
                                "FROM Fine f " +
                                "WHERE LOWER(f.user.username) = LOWER(:user) ORDER BY f.createdAt DESC", Object[].class)
                .setParameter("user", username.trim())
                .getResultList();

        return results.stream()
                .map(res -> {
                    //  : Parse LocalDateTime
                    String formattedDate = "N/A";
                    if (res[1] != null) {
                        formattedDate = ((LocalDateTime) res[1]).format(DATE_FORMATTER);
                    }

                    return res[0] + " | " +
                            formattedDate + " | " +
                            String.format("%.2f", (Double) res[2]) + " | " +
                            res[3] + " | " +
                            "Book: " + (res[4] != null ? res[4] : "Unknown Book");
                })
                .collect(Collectors.toList());
    }


     //ADMIN VIEW: Full system audit trail.

    public List<String> getAllFines() {
        List<Object[]> results = getEm().createQuery(
                        "SELECT f.id, f.user.username, f.amount, f.status, f.createdAt, f.book.title " +
                                "FROM Fine f " +
                                "ORDER BY f.createdAt DESC", Object[].class)
                .getResultList();

        return results.stream()
                .map(res -> {
                    //  FIX: Parse LocalDateTime safely for Admin overview
                    String formattedDate = "N/A";
                    if (res[4] != null) {
                        formattedDate = ((LocalDateTime) res[4]).format(DATE_FORMATTER);
                    }

                    return res[0] + " | User: " + res[1].toString().toUpperCase() + " | " +
                            String.format("%.2f", (Double) res[2]) + " | " +
                            res[3] + " | " +
                            formattedDate + " | " +
                            "Book: " + (res[5] != null ? res[5] : "[Deleted]");
                })
                .collect(Collectors.toList());
    }


    // SECTION 2: AGGREGATE CALCULATIONS


    public double getTotalUnpaid(String username) {
        Double total = getEm().createQuery(
                        "SELECT SUM(f.amount) FROM Fine f WHERE LOWER(f.user.username) = LOWER(:user) AND f.status = 'UNPAID'", Double.class)
                .setParameter("user", username.trim())
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
                fine.setAmount(0.0); // 🌟 EQUILIBRIUM ARCHITECTURE: Synchronize payment clearance down to table level
                save(fine);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}