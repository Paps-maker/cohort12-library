package app;

import app.LoginRequired;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/fines")
@LoginRequired
public class FineHistoryServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
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
            activeLoans = libraryService.getAdminBorrowedRecords();
            history = libraryService.getAdminFineHistory();
            displayTotal = libraryService.getTotalSystemRiskDebt();
            dashboardTitle = "System Administration";
        } else {
            activeLoans = libraryService.getMemberActiveLoans(username);
            history = libraryService.getMemberFineHistory(username);
            double recorded = libraryService.getUnpaidFines(username);
            double projected = libraryService.getProjectedLateFees(username);
            displayTotal = recorded + projected;
            dashboardTitle = "Member Dashboard";
        }

        writer.println("<!DOCTYPE html><html><head><title>" + dashboardTitle + "</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px 20px; color: #1e293b; }");
        writer.println(".container { max-width: 1100px; margin: auto; }");

        // Header & Search
        writer.println(".card-header { background: " + ("ADMIN".equals(role) ? "#0f172a" : "#2563eb") + "; color: white; padding: 40px; border-radius: 24px; text-align: center; margin-bottom: 20px; }");
        writer.println(".card-header h1 { margin: 0; font-size: 32px; font-weight: 800; letter-spacing: -1px; }");

        writer.println(".search-container { position: relative; max-width: 600px; margin: -30px auto 40px auto; padding: 0 20px; }");
        writer.println("#searchBar { width: 100%; padding: 18px 25px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); font-size: 16px; font-family: inherit; outline: none; transition: 0.2s; }");
        writer.println("#searchBar:focus { border-color: #6366f1; box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1); }");

        // Section Cards
        writer.println(".user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; transition: transform 0.2s; }");
        writer.println(".user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }");
        writer.println(".user-info { display: flex; align-items: center; gap: 12px; }");
        writer.println(".user-avatar { background: #6366f1; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; }");
        writer.println(".user-balance { background: #fee2e2; color: #dc2626; padding: 6px 16px; border-radius: 12px; font-weight: 800; font-size: 14px; }");

        writer.println("table { width: 100%; border-collapse: collapse; }");
        writer.println("th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid #f1f5f9; }");
        writer.println("td { padding: 15px 30px; border-bottom: 1px solid #f8fafc; font-size: 14px; }");

        writer.println(".status-pill { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; display: inline-flex; align-items: center; gap: 4px; }");
        writer.println(".pill-red { background: #fee2e2; color: #dc2626; }");
        writer.println(".pill-green { background: #dcfce7; color: #166534; }");
        writer.println(".btn-del { color: #dc2626; text-decoration: none; font-weight: 700; font-size: 12px; border: 1px solid #fecaca; padding: 5px 10px; border-radius: 8px; background: #fff; cursor:pointer; }");
        writer.println(".btn-del:hover { background: #dc2626; color: white; }");
        writer.println(".nav-bar { display: flex; gap: 12px; justify-content: center; margin-top: 20px; }");
        writer.println("</style>");

        // Search Script
        writer.println("<script>");
        writer.println("function filterDashboard() {");
        writer.println("  const input = document.getElementById('searchBar').value.toLowerCase();");
        writer.println("  const cards = document.getElementsByClassName('user-section');");
        writer.println("  for (let card of cards) {");
        writer.println("    const text = card.textContent.toLowerCase();");
        writer.println("    card.style.display = text.includes(input) ? '' : 'none';");
        writer.println("  }");
        writer.println("}");
        writer.println("</script>");
        writer.println("</head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='card-header'><h1>" + dashboardTitle + "</h1>");
        writer.println("<p style='opacity:0.8; font-weight:600;'>System Total Risk: KSH " + String.format("%.2f", displayTotal) + "</p></div>");

        writer.println("<div class='search-container'>");
        writer.println("  <input type='text' id='searchBar' onkeyup='filterDashboard()' placeholder='Search by user, book title, or transaction ID...'>");
        writer.println("</div>");

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

        Set<String> allUsers = new TreeSet<>(userHistory.keySet());
        allUsers.addAll(userLoans.keySet());

        for (String targetUser : allUsers) {
            double recorded = libraryService.getUnpaidFines(targetUser);
            double projected = libraryService.getProjectedLateFees(targetUser);
            double userTotal = recorded + projected;
            String displayName = targetUser.substring(0,1).toUpperCase() + targetUser.substring(1);

            writer.println("<div class='user-section'>");
            writer.println("  <div class='user-header'>");
            writer.println("    <div class='user-info'>");
            writer.println("      <div class='user-avatar'>" + displayName.substring(0,1) + "</div>");
            writer.println("      <span style='font-weight:800; font-size:18px;'>" + displayName + "</span>");
            writer.println("    </div>");
            writer.println("    <div class='user-balance'>Outstanding: KSH " + String.format("%.2f", userTotal) + "</div>");
            writer.println("  </div>");

            writer.println("  <table><thead><tr><th>Loan Details</th><th>Status</th></tr></thead><tbody>");
            List<String> loans = userLoans.getOrDefault(targetUser, new ArrayList<>());
            if (loans.isEmpty()) {
                writer.println("<tr><td colspan='2' style='color:#94a3b8;'>No active loans.</td></tr>");
            } else {
                for (String loan : loans) {
                    String[] lp = loan.split("\\|");
                    String t = ("ADMIN".equals(role)) ? lp[2].trim() : lp[1].trim();
                    String s = lp[lp.length-1].trim();
                    boolean urgent = libraryService.isLoanUrgent(s);
                    writer.println("<tr><td><b>" + t + "</b></td>");
                    writer.println("<td><span class='status-pill " + (urgent ? "pill-red" : "pill-green") + "'>" + s + "</span></td></tr>");
                }
            }
            writer.println("</tbody></table>");

            // --- PAYMENT AUDIT TABLE WITH CREATED_AT ---
            writer.println("<div style='padding:15px 30px; background:#f8fafc; font-weight:700; font-size:12px; color:#64748b; border-top:1px solid #f1f5f9;'>PAYMENT AUDIT</div>");
            writer.println("<table><thead><tr><th>Audit ID</th><th>Date & Time</th><th>Amount</th><th>Status</th><th>Action</th></tr></thead><tbody>");

            List<String> fines = userHistory.getOrDefault(targetUser, new ArrayList<>());
            if (fines.isEmpty()) {
                writer.println("<tr><td colspan='5' style='color:#94a3b8; text-align:center;'>No transaction history.</td></tr>");
            } else {
                for (String fine : fines) {
                    String[] fParts = fine.split("\\|");
                    String fId = fParts[0].replaceAll("[^0-9]", "").trim();

                    // --- ALIGNMENT FIX FOR USER VIEW ---
                    String timestamp;
                    String amt;

                    if ("ADMIN".equals(role)) {
                        // Admin logic preserved as requested
                        timestamp = (fParts.length > 4) ? fParts[4].trim() : "N/A";
                        amt = (fParts.length > 2) ? fParts[2].trim() : "0.00";
                    } else {
                        // User side fix: align indices to DAO string format
                        // DAO Format: ID | Timestamp | Amount | Status | BookTitle
                        timestamp = (fParts.length > 1) ? fParts[1].trim() : "N/A";
                        amt = (fParts.length > 2) ? fParts[2].trim() : "0.00";
                    }

                    boolean isUnpaid = fine.toUpperCase().contains("UNPAID");

                    writer.println("<tr>");
                    writer.println("  <td><span style='color:#64748b; font-size:11px;'>ID: "+fId+"</span></td>");
                    writer.println("  <td><span style='color:#1e293b; font-size:13px; font-weight:600;'>" + timestamp + "</span></td>");
                    writer.println("  <td><b>" + amt + "</b></td>");
                    writer.println("  <td><span class='status-pill " + (isUnpaid ? "pill-red" : "pill-green") + "'>" + (isUnpaid ? "UNPAID" : "PAID") + "</span></td>");
                    writer.println("  <td style='text-align:right;'>");

                    if ("ADMIN".equals(role)) {
                        writer.println("    <form action='delete-fine' method='POST' style='margin:0;'>");
                        writer.println("      <input type='hidden' name='fineId' value='"+fId+"'>");
                        writer.println("      <button type='submit' class='btn-del'>Delete</button></form>");
                    } else if (isUnpaid) {
                        writer.println("    <form action='pay-fine' method='POST' style='margin:0;'>");
                        writer.println("      <input type='hidden' name='fineId' value='"+fId+"'>");
                        writer.println("      <button type='submit' style='background:#2563eb; color:white; border:none; padding:5px 12px; border-radius:8px; font-weight:700; cursor:pointer;'>Pay</button></form>");
                    }
                    writer.println("  </td></tr>");
                }
            }
            writer.println("</tbody></table></div>");
        }

        writer.println("<div class='nav-bar'><a href='books' style='color:#64748b; text-decoration:none; font-weight:700;'>← Return to Catalog</a></div>");
        writer.println("</div></body></html>");
    }
}