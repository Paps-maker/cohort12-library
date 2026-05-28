package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.ModelAndView;
import app.ejbs.FineBean;
import app.ejbs.BorrowingBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import app.framework.Controller;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class FineController {

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private FineBean fineBean;

    /**
     * Main Dashboard with Search, Grouping logic, and Interactive Payments
     */
    @ActionGetMethod("/fines")
    public ModelAndView viewFines(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return new ModelAndView("redirect:/login.jsp");
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String infoMessage = req.getParameter("message");

        List<String> activeLoans;
        List<String> history;
        double displayTotal;
        String dashboardTitle;

        // --- BUSINESS & DATA ORCHESTRATION LAYER ---
        if ("ADMIN".equals(role)) {
            activeLoans = borrowingBean.getAdminBorrowedRecords();
            history = fineBean.getAdminFineHistory();
            displayTotal = fineBean.getTotalSystemRiskDebt();
            dashboardTitle = "System Administration";
        } else {
            activeLoans = borrowingBean.getMemberActiveLoans(username);
            history = fineBean.getMemberFineHistory(username);
            double recorded = fineBean.getUnpaidFines(username);
            double projected = fineBean.getProjectedLateFees(username);
            displayTotal = recorded + projected;
            dashboardTitle = "Member Dashboard";
        }

        // --- SHIP RAW DATA AND CONFIGURATION TO THE TARGET VIEW ---
        return new ModelAndView("/views/fines.jsp")
                .addObject("title", dashboardTitle)
                .addObject("role", role)
                .addObject("username", username)
                .addObject("displayTotal", displayTotal)
                .addObject("infoMessage", infoMessage)
                .addObject("activeLoans", activeLoans)
                .addObject("history", history);
    }

    @ActionPostMethod("/pay-fine")
    public ModelAndView payFine(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return new ModelAndView("redirect:/login.jsp");
        }
        String username = (String) session.getAttribute("username");
        String fineIdParam = req.getParameter("fineId");

        boolean success = fineBean.processFinePayment(username, fineIdParam);
        String feedback = success ? "Payment Successful! Fine cleared safely." : "Payment processing failed. Please verify records.";

        return new ModelAndView("redirect:/fines?message=" + URLEncoder.encode(feedback, StandardCharsets.UTF_8));
    }

    @ActionPostMethod("/delete-fine")
    public ModelAndView deleteFine(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null && "ADMIN".equals(session.getAttribute("role"))) {
            String fineId = req.getParameter("fineId");
            if (fineId != null) {
                try {
                    fineBean.deleteFineRecord(Integer.parseInt(fineId.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return new ModelAndView("redirect:/fines");
    }
}