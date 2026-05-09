package app.dao;

import app.db.DBConnection;
import app.util.DataSourceHelper; // Imported your helper
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Used for dependency injection
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BorrowDAO {

    @Inject
    private DataSourceHelper dbHelper;

    private Connection getMySQLCon() {
        try {
            return dbHelper.getConnection();
        } catch (SQLException e) {
            System.err.println(" BorrowDAO: MySQL Pool Error via Helper");
            return null;
        }
    }

    private Connection getPostgresCon() {
        try {
            return DBConnection.getPostgresConnection();
        } catch (Exception e) {
            System.err.println(" BorrowDAO: PostgreSQL Connection Failed (Backup DB)");
            return null;
        }
    }

    // =========================================================================
    // SECTION 1: VALIDATION & HELPERS
    // =========================================================================

    /**
     * ✅ NEW: Required for FineScheduler (Midnight Overdue Check).
     */
    public List<Integer> getAllOverdueBorrowIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM borrowedbook WHERE due_date < NOW()";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return ids;
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ids;
    }

    /**
     * ✅ NEW: Required for FineScheduler (8:00 AM Reminder Check).
     * Finds loans where the due_date is exactly tomorrow.
     */
    public List<Integer> getBooksDueIn24Hours() {
        List<Integer> ids = new ArrayList<>();
        // Finds records due within the next calendar day
        String sql = "SELECT id FROM borrowedbook WHERE DATE(due_date) = CURDATE() + INTERVAL 1 DAY";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return ids;
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ids;
    }

    public boolean exists(int borrowId) {
        String sql = "SELECT COUNT(*) FROM borrowedbook WHERE id = ?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) { return false; }
    }

    public int getDaysLeft(String bookTitle) {
        String sql = "SELECT b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id WHERE bk.title = ? ORDER BY b.due_date ASC LIMIT 1";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return 0;
            ps.setString(1, bookTitle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp dDate = rs.getTimestamp("due_date");
                    if (dDate == null) return 0;
                    long daysRemaining = Duration.between(LocalDateTime.now(), dDate.toLocalDateTime()).toDays();
                    return (daysRemaining < 0) ? 0 : (int) daysRemaining;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    /**
     * ✅ FIXED: Required by BorrowingBean to link returns to inventory.
     */
    public int getBookIdByBorrowId(int borrowId) {
        String sql = "SELECT bookId FROM borrowedbook WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return -1;
            ps.setInt(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("bookId");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * ✅ NEW: Required for FineScheduler email content.
     */
    public String getBookTitleByBorrowId(int borrowId) {
        String sql = "SELECT bk.title FROM borrowedbook b JOIN book bk ON b.bookId = bk.id WHERE b.id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return "Unknown Book";
            ps.setInt(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("title");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "Unknown Book";
    }

    public String getMemberByBorrowId(int borrowId) {
        String sql = "SELECT username FROM borrowedbook WHERE id = ?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setInt(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int getMemberLoanCount(String username) {
        String sql = "SELECT COUNT(*) FROM borrowedbook WHERE username = ?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return 0;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int getOverdueDays(int borrowId) {
        String sql = "SELECT due_date FROM borrowedbook WHERE id = ?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return 0;
            ps.setInt(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp dDate = rs.getTimestamp("due_date");
                    if (dDate == null) return 0;
                    LocalDateTime dueDate = dDate.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(dueDate)) {
                        long hoursLate = Duration.between(dueDate, now).toHours();
                        int days = (int) (hoursLate / 24);
                        return (hoursLate % 24 > 0 || days == 0) ? days + 1 : days;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // =========================================================================
    // SECTION 2: CORE LOGIC
    // =========================================================================

    public int borrowBook(String username, int bookId, int daysRequested) {
        String sql = "INSERT INTO borrowedbook(username, bookId, borrow_date, due_date) VALUES(?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        Timestamp borrowDate = Timestamp.valueOf(now);
        Timestamp dueDate = Timestamp.valueOf(now.plusDays(daysRequested));

        int newId = executeWriteAndGetId(getMySQLCon(), sql, username, bookId, borrowDate, dueDate);

        if (newId != -1) {
            try (Connection pgCon = getPostgresCon()) {
                if (pgCon != null) executeWriteAndGetId(pgCon, sql, username, bookId, borrowDate, dueDate);
            } catch (Exception e) { }
        }

        return newId;
    }

    public boolean returnBook(int borrowId) {
        String sql = "DELETE FROM borrowedbook WHERE id = ?";
        boolean m = false;
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) {
                ps.setInt(1, borrowId);
                m = ps.executeUpdate() > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }

        try (Connection con = getPostgresCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) {
                ps.setInt(1, borrowId);
                ps.executeUpdate();
            }
        } catch (Exception e) { }
        return m;
    }

    // =========================================================================
    // SECTION 3: LISTING & UTILITIES
    // =========================================================================

    public List<String> getAllBorrowed() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.id, b.username, bk.title, b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id ORDER BY b.due_date ASC";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return list;
            while (rs.next()) {
                list.add("ID: " + rs.getInt("id") + " | User: " + rs.getString("username") +
                        " | Title: " + rs.getString("title") + " | " + formatStatus(rs.getTimestamp("due_date")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getUserBorrowed(String username) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.id, bk.title, b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id WHERE b.username=? ORDER BY b.due_date ASC";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add("ID: " + rs.getInt("id") + " | Book: " + rs.getString("title") + " | " + formatStatus(rs.getTimestamp("due_date")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private int executeWriteAndGetId(Connection con, String sql, String user, int bId, Timestamp bDate, Timestamp dDate) {
        if (con == null) return -1;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user);
            ps.setInt(2, bId);
            ps.setTimestamp(3, bDate);
            ps.setTimestamp(4, dDate);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private String formatStatus(Timestamp dDate) {
        if (dDate == null) return "No Due Date";
        LocalDateTime dueDate = dDate.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate)) {
            long hours = Duration.between(dueDate, now).toHours();
            int days = (int) (hours / 24);
            return "OVERDUE (" + ((hours % 24 > 0 || days == 0) ? days + 1 : days) + " days)";
        } else {
            long diff = Duration.between(now, dueDate).toDays();
            return (diff <= 0) ? "Due Today" : diff + " days left";
        }
    }
}