package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.services.BorrowService;
import app.services.FineService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FineController {

    @Inject
    private BorrowService borrowService;

    @Inject
    private FineService fineService;

    /**
     * Replaces FineHistoryServlet: Main Dashboard with Search and Grouping logic
     */
    @ActionGetMethod("/fines")
    public void viewFines(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();

        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        List<String> activeLoans;
        List<String> history;
        double displayTotal;
        String dashboardTitle;

        if ("ADMIN".equals(role)) {
            activeLoans = borrowService.getAdminBorrowedRecords(); //
            history = fineService.getAdminFineHistory(); //
            displayTotal = fineService.getTotalSystemRiskDebt(); //
            dashboardTitle = "System Administration";
        } else {
            activeLoans = borrowService.getMemberActiveLoans(username); //
            history = fineService.getMemberFineHistory(username); //
            double recorded = fineService.getUnpaidFines(username); //
            double projected = fineService.getProjectedLateFees(username); //
            displayTotal = recorded + projected;
            dashboardTitle = "Member Dashboard";
        }

        writer.println("<!DOCTYPE html><html><head><title>" + dashboardTitle + "</title>");

        // --- CSS FROM ORIGINAL SERVLET ---
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px 20px; color: #1e293b; }");
        writer.println(".container { max-width: 1100px; margin: auto; }");
        writer.println(".card-header { background: " + ("ADMIN".equals(role) ? "#0f172a" : "#2563eb") + "; color: white; padding: 40px; border-radius: 24px; text-align: center; margin-bottom: 20px; }");
        writer.println(".card-header h1 { margin: 0; font-size: 32px; font-weight: 800; letter-spacing: -1px; }");
        writer.println(".search-container { position: relative; max-width: 600px; margin: -30px auto 40px auto; padding: 0 20px; }");
        writer.println("#searchBar { width: 100%; padding: 18px 25px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); font-size: 16px; outline: none; transition: 0.2s; }");
        writer.println(".user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; }");
        writer.println(".user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }");
        writer.println(".user-avatar { background: #6366f1; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; }");
        writer.println(".user-balance { background: #fee2e2; color: #dc2626; padding: 6px 16px; border-radius: 12px; font-weight: 800; font-size: 14px; }");
        writer.println("table { width: 100%; border-collapse: collapse; }");
        writer.println("th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 11px; text-transform: uppercase; border-bottom: 1px solid #f1f5f9; }");
        writer.println("td { padding: 15px 30px; border-bottom: 1px solid #f8fafc; font-size: 14px; }");
        writer.println(".status-pill { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; }");
        writer.println(".pill-red { background: #fee2e2; color: #dc2626; }");
        writer.println(".pill-green { background: #dcfce7; color: #166534; }");
        writer.println(".btn-del { color: #dc2626; text-decoration: none; font-weight: 700; font-size: 12px; border: 1px solid #fecaca; padding: 5px 10px; border-radius: 8px; background: #fff; cursor:pointer; }");
        writer.println(".nav-bar { display: flex; justify-content: center; margin-top: 40px; padding-bottom: 20px; }");
        writer.println(".nav-bar a { color: #64748b; text-decoration: none; font-weight: 700; font-size: 14px; padding: 10px 20px; border-radius: 12px; background: #ffffff; border: 1px solid #e2e8f0; transition: all 0.2s ease-in-out; display: flex; align-items: center; gap: 8px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); }");
        writer.println(".nav-bar a:hover { color: #0f172a; background: #f8fafc; border-color: #cbd5e1; transform: translateX(-4px); box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }");

        writer.println("</style>");

        // --- SEARCH SCRIPT ---
        writer.println("<script>function filterDashboard() { const input = document.getElementById('searchBar').value.toLowerCase(); const cards = document.getElementsByClassName('user-section'); for (let card of cards) { const text = card.textContent.toLowerCase(); card.style.display = text.includes(input) ? '' : 'none'; } }</script>");
        writer.println("</head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='card-header'><h1>" + dashboardTitle + "</h1>");
        writer.println("<p style='opacity:0.8; font-weight:600;'>System Total Risk: KSH " + String.format("%.2f", displayTotal) + "</p></div>");

        writer.println("<div class='search-container'><input type='text' id='searchBar' onkeyup='filterDashboard()' placeholder='Search...'></div>");

        // --- GROUPING LOGIC ---
        Map<String, List<String>> userHistory = new LinkedHashMap<>();
        Map<String, List<String>> userLoans = new HashMap<>();

        if ("ADMIN".equals(role)) {
            for (String h : history) {
                String[] p = h.split("\\|");
                String u = (p.length > 1) ? p[1].replace("User:", "").trim().toLowerCase() : "system";
                userHistory.computeIfAbsent(u, k -> new ArrayList<>()).add(h);
            }
            for (String l : activeLoans) {
                String[] p = l.split("\\|");
                String u = (p.length > 1) ? p[1].replace("User:", "").trim().toLowerCase() : "system";
                userLoans.computeIfAbsent(u, k -> new ArrayList<>()).add(l);
            }
        } else {
            userHistory.put(username.toLowerCase(), history);
            userLoans.put(username.toLowerCase(), activeLoans);
        }

        for (String targetUser : new TreeSet<>(userHistory.keySet())) {
            double recorded = fineService.getUnpaidFines(targetUser); //
            double projected = fineService.getProjectedLateFees(targetUser); //
            double userTotal = recorded + projected;
            String displayName = targetUser.substring(0,1).toUpperCase() + targetUser.substring(1);

            writer.println("<div class='user-section'><div class='user-header'><div class='user-info'>");
            writer.println("<div class='user-avatar'>" + displayName.substring(0,1) + "</div>");
            writer.println("<span style='font-weight:800; font-size:18px;'>" + displayName + "</span></div>");
            writer.println("<div class='user-balance'>Outstanding: KSH " + String.format("%.2f", userTotal) + "</div></div>");

            // --- LOAN TABLE ---
            writer.println("<table><thead><tr><th>Loan Details</th><th>Status</th></tr></thead><tbody>");
            List<String> loans = userLoans.getOrDefault(targetUser, new ArrayList<>());
            if (loans.isEmpty()) {
                writer.println("<tr><td colspan='2' style='color:#94a3b8;'>No active loans.</td></tr>");
            } else {
                for (String loan : loans) {
                    String[] lp = loan.split("\\|");
                    String t = ("ADMIN".equals(role)) ? lp[2].trim() : lp[1].trim();
                    String s = lp[lp.length-1].trim();
                    boolean urgent = s.toUpperCase().contains("OVERDUE") || s.toUpperCase().contains("DUE");
                    writer.println("<tr><td><b>" + t + "</b></td><td><span class='status-pill " + (urgent ? "pill-red" : "pill-green") + "'>" + s + "</span></td></tr>");
                }
            }
            writer.println("</tbody></table>");

            // --- AUDIT TABLE ---
            writer.println("<div style='padding:15px 30px; background:#f8fafc; font-weight:700; font-size:12px; color:#64748b; border-top:1px solid #f1f5f9;'>PAYMENT AUDIT</div>");
            writer.println("<table><thead><tr><th>Audit ID</th><th>Date</th><th>Amount</th><th>Status</th><th>Action</th></tr></thead><tbody>");

            for (String fine : userHistory.getOrDefault(targetUser, new ArrayList<>())) {
                String[] fParts = fine.split("\\|");
                String fId = fParts[0].replaceAll("[^0-9]", "").trim();
                String timestamp = ("ADMIN".equals(role)) ? (fParts.length > 4 ? fParts[4].trim() : "N/A") : (fParts.length > 1 ? fParts[1].trim() : "N/A");
                String amt = (fParts.length > 2) ? fParts[2].trim() : "0.00";
                boolean isUnpaid = fine.toUpperCase().contains("UNPAID");

                writer.println("<tr><td>ID: "+fId+"</td><td>" + timestamp + "</td><td><b>" + amt + "</b></td>");
                writer.println("<td><span class='status-pill " + (isUnpaid ? "pill-red" : "pill-green") + "'>" + (isUnpaid ? "UNPAID" : "PAID") + "</span></td>");
                writer.println("<td style='text-align:right;'>");

                if ("ADMIN".equals(role)) {
                    writer.println("<form action='"+contextPath+"/delete-fine' method='POST' style='margin:0;'>");
                    writer.println("<input type='hidden' name='fineId' value='"+fId+"'>");
                    writer.println("<button type='submit' class='btn-del'>Delete</button></form>");
                } else if (isUnpaid) {
                    writer.println("<form action='"+contextPath+"/pay-fine' method='POST' style='margin:0;'>");
                    writer.println("<input type='hidden' name='fineId' value='"+fId+"'>");
                    writer.println("<button type='submit' style='background:#2563eb; color:white; border:none; padding:5px 12px; border-radius:8px; cursor:pointer;'>Pay</button></form>");
                }
                writer.println("</td></tr>");
            }
            writer.println("</tbody></table></div>");
        }
        writer.println("<div class='nav-bar'><a href='books'>← Catalog</a></div></div></body></html>");
    }

    /**
     * Replaces FinePaymentServlet logic
     */
    @ActionPostMethod("/pay-fine")
    public void payFine(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }
        String username = (String) session.getAttribute("username");
        String fineIdParam = req.getParameter("fineId");

        // Matches Service: processFinePayment(String, String)
        boolean success = fineService.processFinePayment(username, fineIdParam);
        resp.sendRedirect(contextPath + "/fines?status=" + (success ? "success" : "failed"));
    }

    /**
     * Replaces DeleteFineServlet logic
     */
    @ActionPostMethod("/delete-fine")
    public void deleteFine(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();
        if (session != null && "ADMIN".equals(session.getAttribute("role"))) {
            String fineId = req.getParameter("fineId");
            if (fineId != null) {
                // Matches Service: deleteFineRecord(int)
                fineService.deleteFineRecord(Integer.parseInt(fineId));
            }
        }
        resp.sendRedirect(contextPath + "/fines");
    }
}