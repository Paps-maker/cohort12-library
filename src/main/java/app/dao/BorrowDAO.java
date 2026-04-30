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
            System.err.println("❌ BorrowDAO: MySQL Pool Error - Check WildFly DataSources");
            return null;
        }
    }

    private Connection getPostgresCon() {
        try {
            return DBConnection.getPostgresConnection();
        } catch (Exception e) {
            // Log but don't crash; we want the system to work even if the backup DB is down
            System.err.println("⚠️ BorrowDAO: PostgreSQL Connection Failed (Backup DB)");
            return null;
        }
    }

    // =========================================================================
    // SECTION 1: VALIDATION & CALCULATIONS
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

    public boolean isAlreadyReturned(int borrowId) {
        return !exists(borrowId);
    }

    public int getDaysLeft(String bookTitle) {
        String sql = "SELECT b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id WHERE bk.title = ?";
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

    public List<String> getAllBorrowed() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.id, b.username, bk.title, b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return list;
            while (rs.next()) {
                int id = rs.getInt("id");
                Timestamp dDate = rs.getTimestamp("due_date");
                String status = formatStatus(dDate);
                list.add("ID: " + id + " | User: " + rs.getString("username") +
                        " | Title: " + rs.getString("title") + " | " + status);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getUserBorrowed(String username) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT b.id, bk.title, b.due_date FROM borrowedbook b " +
                "JOIN book bk ON b.bookId = bk.id WHERE b.username=?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Timestamp dDate = rs.getTimestamp("due_date");
                    String status = formatStatus(dDate);
                    list.add("ID: " + id + " | Book: " + rs.getString("title") + " | " + status);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private String formatStatus(Timestamp dDate) {
        if (dDate == null) return "No Due Date";
        LocalDateTime dueDate = dDate.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate)) {
            long hours = Duration.between(dueDate, now).toHours();
            long days = (hours / 24) + (hours % 24 > 0 ? 1 : 0);
            return "OVERDUE (" + (days == 0 ? 1 : days) + " days)";
        } else {
            long diff = Duration.between(now, dueDate).toDays();
            return diff + " days left";
        }
    }

    public boolean borrowBook(String username, int bookId, int daysRequested) {
        String sql = "INSERT INTO borrowedbook(username, bookId, borrow_date, due_date) VALUES(?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        Timestamp borrowDate = Timestamp.valueOf(now);
        Timestamp dueDate = Timestamp.valueOf(now.plusDays(daysRequested));

        // 1. Attempt MySQL (Primary)
        boolean mysqlResult = executeWriteWithDue(getMySQLCon(), sql, username, bookId, borrowDate, dueDate, true);

        // 2. Attempt Postgres (Backup) - encapsulated in try/catch to prevent crashing the whole app
        boolean postgresResult = false;
        try (Connection pgCon = getPostgresCon()) {
            if (pgCon != null) {
                postgresResult = executeWriteWithDue(pgCon, sql, username, bookId, borrowDate, dueDate, false);
            }
        } catch (Exception e) {
            System.err.println("❌ Postgres Borrow Log Failed: " + e.getMessage());
        }

        return mysqlResult || postgresResult;
    }

    public boolean returnBook(int borrowId) {
        String sql = "DELETE FROM borrowedbook WHERE id = ?";
        boolean m = false, p = false;

        // MySQL Delete
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) {
                ps.setInt(1, borrowId);
                m = ps.executeUpdate() > 0;
            }
        } catch (Exception e) { System.err.println("❌ MySQL Delete Failed"); }

        // Postgres Delete
        try (Connection con = getPostgresCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con != null) {
                ps.setInt(1, borrowId);
                p = ps.executeUpdate() > 0;
            }
        } catch (Exception e) { System.err.println("❌ Postgres Delete Failed"); }

        return m || p;
    }

    // =========================================================================
    // SECTION 3: UTILITIES
    // =========================================================================

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

    public boolean isBookBorrowed(int bookId) {
        String sql = "SELECT COUNT(*) FROM borrowedbook WHERE bookId=?";
        try (Connection con = getMySQLCon(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean executeWriteWithDue(Connection con, String sql, String user, int bId, Timestamp bDate, Timestamp dDate, boolean isPooled) {
        if (con == null) return false;
        // If it's pooled (MySQL), the try-with-resources in the caller handles it.
        // If it's not pooled (Postgres), we use try-with-resources here.
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setInt(2, bId);
            ps.setTimestamp(3, bDate);
            ps.setTimestamp(4, dDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ SQL Write Error: " + e.getMessage());
            return false;
        } finally {
            // Only close manually if it's pooled and we are specifically told to
            // Note: Modern best practice is to let the calling method handle connection closing via try-with-resources.
        }
    }
}