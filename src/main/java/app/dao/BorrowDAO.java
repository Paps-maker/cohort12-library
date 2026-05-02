package app.dao;

import app.db.DBConnection;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BorrowDAO {

    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource mysqlDataSource;

    private Connection getMySQLCon() {
        try {
            return mysqlDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("❌ BorrowDAO: MySQL Pool Error");
            return null;
        }
    }

    private Connection getPostgresCon() {
        try {
            return DBConnection.getPostgresConnection();
        } catch (Exception e) {
            System.err.println("⚠️ BorrowDAO: PostgreSQL Connection Failed (Backup DB)");
            return null;
        }
    }

    // =========================================================================
    // SECTION 1: VALIDATION, CALCULATIONS & SERVLET HELPERS
    // =========================================================================

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

    /**
     * ✅ FIX: Resolves "Cannot resolve method 'getDaysLeft'" in BookServlet.
     * Provides an estimation of when a book might be returned based on the closest due date.
     */
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
     * Retrieves the book ID for a borrow record.
     * Essential for restocking inventory via BorrowingBean.
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
    // SECTION 2: CORE LOGIC & LISTING
    // =========================================================================

    public boolean borrowBook(String username, int bookId, int daysRequested) {
        String sql = "INSERT INTO borrowedbook(username, bookId, borrow_date, due_date) VALUES(?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        Timestamp borrowDate = Timestamp.valueOf(now);
        Timestamp dueDate = Timestamp.valueOf(now.plusDays(daysRequested));

        boolean mysqlResult = executeWriteWithDue(getMySQLCon(), sql, username, bookId, borrowDate, dueDate);

        // Mirror to Backup
        try (Connection pgCon = getPostgresCon()) {
            if (pgCon != null) executeWriteWithDue(pgCon, sql, username, bookId, borrowDate, dueDate);
        } catch (Exception e) { System.err.println("❌ Backup Log Failed"); }

        return mysqlResult;
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

    public List<String> getAllBorrowed() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.id, b.username, bk.title, b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id ORDER BY b.due_date ASC";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return list;
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp dDate = rs.getTimestamp("due_date");
                list.add("ID: " + id + " | User: " + rs.getString("username") +
                        " | Title: " + rs.getString("title") + " | " + formatStatus(dDate));
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
                    int id = rs.getInt("id");
                    Timestamp dDate = rs.getTimestamp("due_date");
                    list.add("ID: " + id + " | Book: " + rs.getString("title") + " | " + formatStatus(dDate));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
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

    // =========================================================================
    // UTILITIES
    // =========================================================================

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

    private boolean executeWriteWithDue(Connection con, String sql, String user, int bId, Timestamp bDate, Timestamp dDate) {
        if (con == null) return false;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setInt(2, bId);
            ps.setTimestamp(3, bDate);
            ps.setTimestamp(4, dDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}