package app;

import app.LoginRequired;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * FINES & DASHBOARD SERVLET
 * Manages the financial overview for both Admins (System Risk) and Members (Personal Debt).
 */
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
        String contextPath = req.getContextPath();

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        // --- DATA AGGREGATION ---
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

            // Total = Unpaid (recorded in DB) + Projected (accruing daily for late books)
            double recorded = libraryService.getUnpaidFines(username);
            double projected = libraryService.getProjectedLateFees(username);
            displayTotal = recorded + projected;
            dashboardTitle = "Member Dashboard";
        }

        writer.println("<!DOCTYPE html><html><head><title>" + dashboardTitle + "</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; margin: 0; padding: 40px 20px; color: #1e293b; }");
        writer.println(".container { max-width: 900px; margin: auto; background: white; border-radius: 24px; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); overflow: hidden; border: 1px solid #e2e8f0; }");
        writer.println(".card-header { background: " + ("ADMIN".equals(role) ? "#0f172a" : "#2563eb") + "; color: white; padding: 50px 30px; text-align: center; }");
        writer.println(".card-header h1 { margin: 0; font-size: 32px; font-weight: 800; letter-spacing: -1px; }");
        writer.println(".content { padding: 40px; }");
        writer.println("h2 { font-size: 14px; color: #94a3b8; margin-bottom: 25px; text-transform: uppercase; letter-spacing: 2px; font-weight: 800; display: flex; align-items: center; gap: 10px; }");
        writer.println("h2::after { content: ''; flex-grow: 1; height: 1px; background: #f1f5f9; }");
        writer.println(".total-owed { background: " + ("ADMIN".equals(role) ? "#334155" : "#fff1f2") + "; color: " + ("ADMIN".equals(role) ? "#fff" : "#e11d48") + "; padding: 25px; border-radius: 16px; text-align: center; margin-bottom: 40px; font-weight: 800; font-size: 22px; border: 1px solid " + ("ADMIN".equals(role) ? "transparent" : "#ffe4e6") + "; }");
        writer.println(".item { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #ffffff; border: 1px solid #f1f5f9; border-radius: 12px; margin-bottom: 12px; transition: 0.2s; }");
        writer.println(".item:hover { border-color: #cbd5e1; transform: translateX(5px); }");
        writer.println(".book-title { font-weight: 700; color: #0f172a; font-size: 16px; }");
        writer.println(".badge { padding: 6px 12px; border-radius: 8px; font-size: 11px; font-weight: 800; text-transform: uppercase; }");
        writer.println(".badge-urgent { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }");
        writer.println(".badge-safe { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }");
        writer.println(".fine-row { display: flex; justify-content: space-between; align-items: center; padding: 18px; background: #f8fafc; border-radius: 12px; margin-bottom: 10px; border: 1px solid #e2e8f0; }");
        writer.println(".id-badge { background: #1e293b; color: #fbbf24; padding: 3px 8px; border-radius: 6px; font-family: 'JetBrains Mono', monospace; font-size: 11px; font-weight: 700; }");
        writer.println(".user-name { color: #64748b; font-weight: 700; font-size: 12px; text-transform: uppercase; margin: 0 10px; }");
        writer.println(".fine-amt { font-weight: 800; font-size: 15px; }");
        writer.println(".actions { display: flex; gap: 12px; margin-top: 40px; justify-content: center; }");
        writer.println(".btn { text-decoration: none; padding: 14px 28px; border-radius: 12px; font-weight: 700; font-size: 14px; transition: 0.2s; cursor: pointer; border: none; }");
        writer.println(".btn-blue { background: #2563eb; color: white; }");
        writer.println(".btn-slate { background: #f1f5f9; color: #475569; }");
        writer.println(".btn-delete { background: #fee2e2; color: #dc2626; padding: 8px 16px; font-size: 12px; border: 1px solid #fecaca; }");
        writer.println(".btn-delete:hover { background: #dc2626; color: white; }");
        writer.println("</style></head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='card-header'>");
        writer.println("<h1>" + dashboardTitle + "</h1>");
        writer.println("<p style='opacity: 0.9; margin-top: 10px; font-weight: 600;'>" + ("ADMIN".equals(role) ? "Financial Risk Oversight" : "Welcome, " + username) + "</p>");
        writer.println("</div>");

        writer.println("<div class='content'>");

        // 1. BALANCE SECTION
        if (displayTotal > 0) {
            writer.println("<div class='total-owed'>");
            writer.println(role.equals("ADMIN") ? "🚨 FINES OUTSTANDING: " : "💰 CURRENT BALANCE: ");
            writer.println("KSH " + String.format("%.2f", displayTotal));
            writer.println("</div>");
        }

        // 2. ACTIVE LOANS SECTION
        writer.println("<h2>" + ("ADMIN".equals(role) ? "Current System Loans" : "Books in your possession") + "</h2>");
        if (activeLoans.isEmpty()) {
            writer.println("<p style='text-align:center; color:#94a3b8; padding: 20px;'>No active books detected.</p>");
        } else {
            for (String record : activeLoans) {
                String[] parts = record.split("\\|");
                // Admin format: ID | User | Title | Days
                // Member format: ID | Title | Days
                String title = parts.length > 2 && "ADMIN".equals(role) ? parts[2].trim() : (parts.length > 1 ? parts[1].trim() : parts[0]);
                String statusText = parts[parts.length - 1].trim();
                boolean isLate = libraryService.isLoanUrgent(statusText);

                writer.println("<div class='item'>");
                writer.println("<div><span class='book-title'>" + title + "</span>");
                if ("ADMIN".equals(role)) {
                    String idStr = parts[0].trim().replace("ID:", "").trim();
                    String userStr = parts[1].trim().replace("User:", "").trim();
                    writer.println("<div style='margin-top:5px;'><span class='id-badge'>#" + idStr + "</span><span class='user-name'>" + userStr + "</span></div>");
                }
                writer.println("</div><span class='badge " + (isLate ? "badge-urgent" : "badge-safe") + "'>" + statusText + "</span></div>");
            }
        }

        // 3. FINE HISTORY / GLOBAL LOGS
        writer.println("<h2 style='margin-top:50px;'>" + ("ADMIN".equals(role) ? "System Payment Audit" : "Your Payment History") + "</h2>");
        if (history.isEmpty()) {
            writer.println("<p style='text-align:center; color:#94a3b8; padding: 20px;'>No payment records found.</p>");
        } else {
            for (String fine : history) {
                String[] fParts = fine.split("\\|");
                String idPart = fParts[0].replaceAll("[^0-9]", "").trim();
                boolean isUnpaid = fine.toUpperCase().contains("UNPAID");
                String statusLabel = isUnpaid ? "UNPAID: " : "PAID: ";
                String statusColor = isUnpaid ? "#e11d48" : "#10b981";

                writer.println("<div class='fine-row'>");

                if (fParts.length >= 3) {
                    String user = fParts[1].replace("User:", "").trim();
                    String amt = fParts[2].trim();

                    writer.println("<div>");
                    writer.println("<span class='id-badge'>ID: " + idPart + "</span>");
                    if ("ADMIN".equals(role)) writer.println("<span class='user-name'>" + user + "</span>");
                    writer.println("<span class='fine-amt' style='color:"+statusColor+";'>" + statusLabel + amt + "</span>");
                    writer.println("</div>");
                } else {
                    writer.println("<span>" + fine + "</span>");
                }

                // Actions: Admin deletes records, Member pays active fines
                if (!idPart.isEmpty()) {
                    if ("ADMIN".equals(role)) {
                        writer.println("<form action='" + contextPath + "/delete-fine' method='POST' style='margin:0;'>");
                        writer.println("<input type='hidden' name='fineId' value='" + idPart + "'>");
                        writer.println("<button type='submit' class='btn btn-delete' onclick='return confirm(\"Delete this financial record permanently?\")'>Delete Record</button>");
                        writer.println("</form>");
                    } else if (isUnpaid) {
                        writer.println("<form action='" + contextPath + "/pay-fine' method='POST' style='margin:0;'>");
                        writer.println("<input type='hidden' name='fineId' value='" + idPart + "'>");
                        writer.println("<button type='submit' class='btn btn-blue' style='padding:8px 16px; font-size:12px;'>Clear Debt</button>");
                        writer.println("</form>");
                    }
                }
                writer.println("</div>");
            }
        }

        // 4. NAVIGATION
        writer.println("<div class='actions'>");
        writer.println("<a href='" + contextPath + "/books' class='btn btn-slate'>Return to Catalog</a>");
        if (!"ADMIN".equals(role)) {
            writer.println("<a href='" + contextPath + "/books' class='btn btn-blue'>Borrow Books</a>");
        }
        writer.println("</div>");

        writer.println("</div></div></body></html>");
    }
}