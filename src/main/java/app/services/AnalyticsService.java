package app.services;

import app.dao.AnalyticsDAO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class AnalyticsService {

    @Inject
    private AnalyticsDAO analyticsDao;

    public Map<String, Integer> getTopBorrowedBooks() {
        return analyticsDao.getTopBooks();
    }

    public double getSystemTotalRevenue() {
        return analyticsDao.getTotalRevenue();
    }

    public Map<String, Integer> getMostActiveMembers() {
        return analyticsDao.getMostActiveUsers();
    }

    public Map<String, Double> getWeeklyDebtTrend() {
        return analyticsDao.getDailyDebtTrend();
    }

    public double getUnpaidFinesCount() {
        return analyticsDao.getUnpaidFineCount();
    }

    public int getLibraryTotalInventory() {
        return analyticsDao.getTotalBookVolume();
    }

    public int getSystemOverdueCount() {
        return analyticsDao.getActiveOverdueCount();
    }
}