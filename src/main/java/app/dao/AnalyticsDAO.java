package app.dao;

import app.util.DataSourceHelper; // Imported your helper
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Used for dependency injection
import java.sql.*;
import java.util.*;

/**
 * DATA ACCESS OBJECT: Analytics
 * Optimized for dynamic cumulative trend visualization.
 */
@ApplicationScoped
public class AnalyticsDAO {

    @Inject
    private DataSourceHelper dbHelper;

    private Connection getConnection() throws SQLException {
        return dbHelper.getConnection();
    }

    // =========================================================================
    // SECTION 1: KEY PERFORMANCE INDICATORS (KPIs)
    // =========================================================================

    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM fine WHERE status = 'PAID'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public int getUnpaidFineCount() {
        String sql = "SELECT COUNT(DISTINCT username) FROM fine WHERE status = 'UNPAID'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalBookVolume() {
        String sql = "SELECT SUM(total_quantity) FROM book";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getActiveOverdueCount() {
        String sql = "SELECT COUNT(*) FROM borrowedbook WHERE due_date < NOW() AND return_date IS NULL";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // =========================================================================
    // SECTION 2: TRENDS & DISTRIBUTIONS
    // =========================================================================

    public Map<String, Integer> getTopBooks() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT bk.title, COUNT(b.id) as count " +
                "FROM borrowedbook b JOIN book bk ON b.bookId = bk.id " +
                "GROUP BY bk.title ORDER BY count DESC LIMIT 5";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("title"), rs.getInt("count"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    public Map<String, Integer> getMostActiveUsers() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT username, COUNT(*) as total FROM borrowedbook GROUP BY username ORDER BY total DESC LIMIT 5";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("username"), rs.getInt("total"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return data;
    }

    /**
     * ✅ UPDATED: Dynamic Cumulative Trend.
     * Only shows dates with data. Each point is the sum of itself plus all previous days.
     */
    public Map<String, Double> getDailyDebtTrend() {
        Map<String, Double> data = new LinkedHashMap<>();

        // We fetch only active days from the last 7 days
        String sql = "SELECT DATE(created_at) as trend_date, SUM(amount) as daily_sum " +
                "FROM fine " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY trend_date ASC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            double runningTotal = 0.0;
            while (rs.next()) {
                // This creates the rising curve by accumulating the daily totals
                runningTotal += rs.getDouble("daily_sum");
                data.put(rs.getString("trend_date"), runningTotal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If table is empty, show today at 0
        if (data.isEmpty()) {
            data.put(java.time.LocalDate.now().toString(), 0.0);
        }

        return data;
    }
}