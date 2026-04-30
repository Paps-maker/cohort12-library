package app.ejbs;

import app.dao.FineDAO;
import app.dao.BorrowDAO;
import app.validation.FineValidator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class FineBean {

    @Inject
    private FineDAO fineDao;

    @Inject
    private BorrowDAO borrowDao;

    @Inject
    private FineValidator fineValidator;

    public boolean payFine(String username, String fineIdParam) {
        if (fineValidator.validatePayment(username, fineIdParam) != null) return false;
        try {
            return fineDao.payFine(Integer.parseInt(fineIdParam));
        } catch (Exception e) { return false; }
    }

    public boolean deleteFine(int fineId) {
        return fineDao.deleteFine(fineId);
    }

    public double getProjectedDebt(String username) {
        double total = fineDao.getTotalUnpaid(username);
        List<String> active = borrowDao.getUserBorrowed(username);
        return total + calculateOngoingLateFees(active);
    }

    public double getSystemTotalRisk() {
        double total = fineDao.getSystemTotalUnpaid();
        List<String> allActive = borrowDao.getAllBorrowed();
        return total + calculateOngoingLateFees(allActive);
    }

    private double calculateOngoingLateFees(List<String> records) {
        double fees = 0.0;
        for (String record : records) {
            try {
                int id = Integer.parseInt(record.split("\\|")[0].replace("ID:", "").trim());
                int late = borrowDao.getOverdueDays(id);
                if (late > 0) fees += (late * 50.0);
            } catch (Exception e) {}
        }
        return fees;
    }
}