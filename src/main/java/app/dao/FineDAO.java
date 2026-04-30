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
 * Manages database interactions for recorded debts.
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
     * ✅ MEMBER VIEW: Returns formatted list for history display.
     */
    public List<String> getUserFines(String username) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT id, amount, status, created_at FROM fine WHERE username = ? ORDER BY created_at DESC";

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String status = rs.getString("status");
                    double amt = rs.getDouble("amount");
                    Timestamp ts = rs.getTimestamp("created_at");

                    String row = "ID: " + id + " | ";
                    row += (amt == 0) ? "Returned on time" : "Fine: KSH " + String.format("%.2f", amt);
                    row += " | Status: " + status + " | Date: " + (ts != null ? ts.toString() : "N/A");

                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * ✅ ADMIN VIEW: Returns overview of all fines in the system.
     */
    public List<String> getAllFines() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT id, username, amount, status, created_at FROM fine ORDER BY created_at DESC";

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return list;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add("ID: " + rs.getInt("id") + " | User: " + rs.getString("username").toUpperCase() +
                            " | KSH " + String.format("%.2f", rs.getDouble("amount")) +
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
                    double total = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : total;
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
                double total = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : total;
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
            System.err.println("❌ FineDAO: Error inserting fine record for " + username);
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
            System.err.println("❌ FineDAO: Error processing payment for ID: " + fineId);
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // SECTION 4: DELETE OPERATIONS
    // =========================================================================

    /**
     * ✅ NEW: Supports the Admin delete button.
     */
    public boolean deleteFine(int fineId) {
        String sql = "DELETE FROM fine WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("❌ FineDAO: Error deleting fine record ID: " + fineId);
            e.printStackTrace();
            return false;
        }
    }
}