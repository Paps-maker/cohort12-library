package app.services;

import app.dao.FineDAO;
import app.ejbs.FineBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class FineService {

    @Inject
    private FineDAO fineDao;

    @Inject
    private FineBean fineBean;

    // =========================================================================
    // BASIC FINE DATA (DAO-level reads)
    // =========================================================================

    public double getUnpaidFines(String username) {
        return fineDao.getTotalUnpaid(username);
    }

    public List<String> getMemberFineHistory(String username) {
        return fineDao.getUserFines(username);
    }

    public List<String> getAdminFineHistory() {
        return fineDao.getAllFines();
    }

    // =========================================================================
    // BUSINESS LOGIC (DELEGATED TO FINEBEAN)
    // =========================================================================

    public double getProjectedLateFees(String username) {
        return fineBean.getProjectedDebt(username);
    }

    public double getTotalSystemRiskDebt() {
        return fineBean.getSystemTotalRisk();
    }

    public boolean processFinePayment(String username, String fineIdParam) {
        return fineBean.payFine(username, fineIdParam);
    }

    public boolean deleteFineRecord(int fineId) {
        return fineBean.deleteFine(fineId);
    }
}