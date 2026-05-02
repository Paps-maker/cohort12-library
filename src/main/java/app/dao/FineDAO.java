package app.dao;

import app.model.Fine;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * DATA ACCESS OBJECT: FINES
 * Synchronized with the multi-copy inventory system to show book titles in fine history.
 */
@ApplicationScoped
public class FineDAO {

    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource dataSource;

    private Connection getMySQLCon() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("❌ FineDAO: Connection failed from WildFly Pool");
            return null;
        }
    }

    // =========================================================================
    // SECTION 1: RETRIEVAL
    // =========================================================================

    public Fine getFineById(int fineId) {
        String sql = "SELECT * FROM fine WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setInt(1, fineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Fine fine = new Fine();
                    fine.setId(rs.getInt("id"));
                    fine.setUsername(rs.getString("username"));
                    fine.setAmount(rs.getDouble("amount"));
                    fine.setStatus(rs.getString("status"));
                    fine.setDaysOverdue(rs.getInt("daysOverdue"));
                    fine.setBorrowId(rs.getInt("borrowId"));
                    return fine;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ✅ MEMBER VIEW: Returns formatted list for history display with Book Titles.
     */
    public List<String> getUserFines(String username) {
        List<String> list = new ArrayList<>();
        // Relational Join: fine -> borrowedbook -> book
        String sql = "SELECT f.id, f.amount, f.status, f.created_at, bk.title " +
                "FROM fine f " +
                "LEFT JOIN borrowedbook b ON f.borrowId = b.id " +
                "LEFT JOIN book bk ON b.bookId = bk.id " +
                "WHERE f.username = ? ORDER BY f.created_at DESC";

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String status = rs.getString("status");
                    double amt = rs.getDouble("amount");
                    String title = rs.getString("title");
                    Timestamp ts = rs.getTimestamp("created_at");

                    String row = "ID: " + id + " | ";
                    row += (title != null) ? "Book: " + title + " | " : "Book: [Removed] | ";
                    row += (amt == 0) ? "Returned on time" : "Fine: KSH " + String.format("%.2f", amt);
                    row += " | Status: " + status + " | Date: " + (ts != null ? ts.toLocalDateTime().toLocalDate().toString() : "N/A");

                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * ✅ ADMIN VIEW: Returns overview of all fines with book context and user info.
     */
    public List<String> getAllFines() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT f.id, f.username, f.amount, f.status, bk.title " +
                "FROM fine f " +
                "LEFT JOIN borrowedbook b ON f.borrowId = b.id " +
                "LEFT JOIN book bk ON b.bookId = bk.id " +
                "ORDER BY f.created_at DESC";

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    String bookInfo = (title != null) ? " | Book: " + title : " | Book: [N/A]";

                    list.add("ID: " + rs.getInt("id") + " | User: " + rs.getString("username").toUpperCase() +
                            bookInfo + " | KSH " + String.format("%.2f", rs.getDouble("amount")) +
                            " | Status: " + rs.getString("status"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // SECTION 2: CALCULATIONS
    // =========================================================================

    public double getTotalUnpaid(String username) {
        String sql = "SELECT SUM(amount) FROM fine WHERE username = ? AND status = 'UNPAID'";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return 0.0;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getSystemTotalUnpaid() {
        String sql = "SELECT SUM(amount) FROM fine WHERE status = 'UNPAID'";
        try (Connection con = getMySQLCon();
             Statement s = con.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // =========================================================================
    // SECTION 3: WRITE OPERATIONS
    // =========================================================================

    public boolean insertFine(String username, double amount, int daysOverdue, int borrowId) {
        String sql = "INSERT INTO fine (username, amount, daysOverdue, borrowId, status, created_at) VALUES (?, ?, ?, ?, 'UNPAID', ?)";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setString(1, username);
            ps.setDouble(2, amount);
            ps.setInt(3, daysOverdue);
            ps.setInt(4, borrowId);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean payFine(int fineId) {
        String sql = "UPDATE fine SET status = 'PAID' WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // SECTION 4: DELETE OPERATIONS
    // =========================================================================

    public boolean deleteFine(int fineId) {
        String sql = "DELETE FROM fine WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}