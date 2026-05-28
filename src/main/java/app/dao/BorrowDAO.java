package app.dao;

import app.model.BorrowedBook;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
@Transactional
public class BorrowDAO extends GenericDao<BorrowedBook, Integer> {


    // SECTION 1: VALIDATION & ANALYTICS


    public List<Integer> getAllOverdueBorrowIds() {
        return getEm().createQuery("SELECT b.id FROM BorrowedBook b WHERE b.dueDate < :now", Integer.class)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    public List<Integer> getBooksDueIn24Hours() {
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusSeconds(1);

        return getEm().createQuery("SELECT b.id FROM BorrowedBook b WHERE b.dueDate BETWEEN :start AND :end", Integer.class)
                .setParameter("start", tomorrowStart)
                .setParameter("end", tomorrowEnd)
                .getResultList();
    }

    /**
     * Dual-purpose counter logic:
     * - If username is provided, counts loans for that member.
     * - If username is null/empty, returns the total system-wide active loans (for Admin Dashboard).
     */
    public int getMemberLoanCount(String username) {
        // Handshake check for admin global stat cards
        if (username == null || username.trim().isEmpty()) {
            Long globalCount = getEm().createQuery("SELECT COUNT(b) FROM BorrowedBook b", Long.class)
                    .getSingleResult();
            return globalCount.intValue();
        }

        // Standard track path mapping for individual user dashboards
        Long count = getEm().createQuery("SELECT COUNT(b) FROM BorrowedBook b WHERE b.user.username = :user", Long.class)
                .setParameter("user", username.trim())
                .getSingleResult();
        return count.intValue();
    }

    public int getOverdueDays(int borrowId) {
        BorrowedBook record = findById(borrowId); // Inherited from GenericDao
        if (record == null || record.getDueDate() == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(record.getDueDate())) {
            long hoursLate = Duration.between(record.getDueDate(), now).toHours();
            int days = (int) (hoursLate / 24);
            return (hoursLate % 24 > 0 || days == 0) ? days + 1 : days;
        }
        return 0;
    }


    // SECTION 2: SPECIALIZED LOOKUPS (Leveraging Object Graph Navigation)


    public String getBookTitleByBorrowId(int borrowId) {
        try {
            return getEm().createQuery(
                            "SELECT b.book.title FROM BorrowedBook b WHERE b.id = :id", String.class)
                    .setParameter("id", borrowId)
                    .getSingleResult();
        } catch (Exception e) {
            return "Unknown Book";
        }
    }

    public int getDaysLeft(String bookTitle) {
        try {
            LocalDateTime dDate = getEm().createQuery(
                            "SELECT b.dueDate FROM BorrowedBook b WHERE LOWER(b.book.title) = LOWER(:title) ORDER BY b.dueDate ASC", LocalDateTime.class)
                    .setParameter("title", bookTitle.trim())
                    .setMaxResults(1)
                    .getSingleResult();

            long daysRemaining = Duration.between(LocalDateTime.now(), dDate).toDays();
            return (daysRemaining < 0) ? 0 : (int) daysRemaining;
        } catch (Exception e) {
            return 0;
        }
    }

    // SECTION 3: LISTING & STATUS FORMATTING


    public List<String> getAllBorrowed() {
        List<Object[]> results = getEm().createQuery(
                        "SELECT b.id, b.user.username, b.book.title, b.dueDate FROM BorrowedBook b ORDER BY b.dueDate ASC", Object[].class)
                .getResultList();

        return results.stream()
                .map(res -> "ID: " + res[0] + " | User: " + res[1] +
                        " | Title: " + res[2] + " | " + formatStatus((LocalDateTime) res[3]))
                .collect(Collectors.toList());
    }

    public List<String> getUserBorrowed(String username) {
        List<Object[]> results = getEm().createQuery(
                        "SELECT b.id, b.book.title, b.dueDate FROM BorrowedBook b WHERE b.user.username = :user ORDER BY b.dueDate ASC", Object[].class)
                .setParameter("user", username)
                .getResultList();

        return results.stream()
                .map(res -> "ID: " + res[0] + " | Book: " + res[1] + " | " + formatStatus((LocalDateTime) res[2]))
                .collect(Collectors.toList());
    }

    private String formatStatus(LocalDateTime dueDate) {
        if (dueDate == null) return "No Due Date";
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate)) {
            int days = getOverdueDaysLogic(dueDate, now);
            return "OVERDUE (" + days + " days)";
        } else {
            long diff = Duration.between(now, dueDate).toDays();
            return (diff <= 0) ? "Due Today" : diff + " days left";
        }
    }

    private int getOverdueDaysLogic(LocalDateTime dueDate, LocalDateTime now) {
        long hoursLate = Duration.between(dueDate, now).toHours();
        int days = (int) (hoursLate / 24);
        return (hoursLate % 24 > 0 || days == 0) ? days + 1 : days;
    }
}