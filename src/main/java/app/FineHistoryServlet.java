package app;

import app.LoginRequired;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
        writer.println("body { font-family: 'Inter', sans-serif; background: linear-gradient(135deg, #f0f2f5 0%, #e2e8f0 100%); margin: 0; min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 20px; }");
        writer.println(".container { width: 100%; max-width: 850px; background: white; border-radius: 16px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); overflow: hidden; }");
        writer.println(".card-header { background: " + ("ADMIN".equals(role) ? "#0f172a" : "#1e293b") + "; color: white; padding: 40px 20px; text-align: center; }");
        writer.println(".card-header h1 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }");
        writer.println(".content { padding: 35px; }");
        writer.println("h2 { font-size: 14px; color: #64748b; margin-bottom: 20px; text-transform: uppercase; letter-spacing: 1.5px; border-bottom: 2px solid #f1f5f9; padding-bottom: 10px; }");
        writer.println(".item { display: flex; justify-content: space-between; align-items: center; padding: 18px 0; border-bottom: 1px solid #f1f5f9; }");
        writer.println(".book-title { font-weight: 600; color: #1e293b; font-size: 15px; }");
        writer.println(".badge { padding: 6px 14px; border-radius: 9999px; font-size: 11px; font-weight: 800; }");
        writer.println(".badge-urgent { background: #fee2e2; color: #991b1b; }");
        writer.println(".badge-safe { background: #dcfce7; color: #166534; }");
        writer.println(".fine-row { background: #f8fafc; padding: 15px; border-radius: 10px; margin-bottom: 12px; border-left: 5px solid #cbd5e1; font-size: 14px; color: #475569; display: flex; justify-content: space-between; align-items: center; transition: 0.2s; }");
        writer.println(".fine-row:hover { background: #f1f5f9; }");
        writer.println(".total-owed { background: " + ("ADMIN".equals(role) ? "#334155" : "#ef4444") + "; color: white; padding: 18px; border-radius: 12px; text-align: center; margin-bottom: 30px; font-weight: 800; font-size: 18px; }");
        writer.println(".actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 35px; justify-content: center; }");
        writer.println(".btn { text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 700; font-size: 13px; transition: 0.2s; border: none; cursor: pointer; display: inline-flex; align-items: center; }");
        writer.println(".btn-blue { background: #3b82f6; color: white; }");
        writer.println(".btn-slate { background: #64748b; color: white; }");
        writer.println(".btn-delete { background: #fee2e2; color: #dc2626; padding: 8px 12px; font-size: 11px; border: 1px solid #fecaca; }");
        writer.println(".btn-delete:hover { background: #ef4444; color: white; }");

        writer.println(".record-meta { display: flex; align-items: center; gap: 8px; }");
        writer.println(".id-badge { background: #1e293b; color: #fef3c7; padding: 1px 6px; border-radius: 4px; font-family: monospace; font-size: 11px; font-weight: 700; }");
        writer.println(".user-name { color: #475569; font-weight: 700; font-size: 12px; text-transform: uppercase; }");
        writer.println(".fine-amt { font-weight: 800; color: #1e293b; margin-left: 5px; }");

        writer.println("</style></head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='card-header'>");
        writer.println("<h1>" + dashboardTitle + "</h1>");
        writer.println("<p style='opacity: 0.8; margin: 5px 0 0 0;'>" + ("ADMIN".equals(role) ? "System Overview" : "Member: " + username.toUpperCase()) + "</p>");
        writer.println("</div>");

        writer.println("<div class='content'>");

        if (displayTotal > 0) {
            writer.println("<div class='total-owed'>");
            writer.println("💰 " + ("ADMIN".equals(role) ? "TOTAL SYSTEM RISK: " : "ACCOUNT BALANCE: ") + "KSH " + String.format("%.2f", displayTotal));
            writer.println("</div>");
        }

        // Active Loans Section
        writer.println("<h2>" + ("ADMIN".equals(role) ? "All Active System Loans" : "My Active Loans") + "</h2>");
        if (activeLoans.isEmpty()) {
            writer.println("<p style='text-align:center; color:#94a3b8;'>No active loans found.</p>");
        } else {
            for (String record : activeLoans) {
                String[] parts = record.split("\\|");
                String title = parts.length > 2 && "ADMIN".equals(role) ? parts[2].trim() : (parts.length > 1 ? parts[1].trim() : parts[0]);
                String statusText = parts[parts.length - 1].trim();
                boolean isLate = libraryService.isLoanUrgent(statusText);

                writer.println("<div class='item'>");
                writer.println("<div><span class='book-title'>" + title + "</span>");
                if ("ADMIN".equals(role)) {
                    String idStr = parts[0].trim().replace("ID:", "").trim();
                    String userStr = parts[1].trim().replace("User:", "").trim();
                    writer.println("<div class='record-meta' style='margin-top:4px;'>");
                    writer.println("<span class='id-badge'>#" + idStr + "</span>");
                    writer.println("<span class='user-name'>" + userStr + "</span>");
                    writer.println("</div>");
                }
                writer.println("</div><span class='badge " + (isLate ? "badge-urgent" : "badge-safe") + "'>" + statusText + "</span></div>");
            }
        }

        // Fine History Section
        writer.println("<h2 style='margin-top:40px;'>" + ("ADMIN".equals(role) ? "Global Fine Records" : "Payment History") + "</h2>");
        if (history.isEmpty()) {
            writer.println("<p style='text-align:center; color:#94a3b8;'>No fine history recorded.</p>");
        } else {
            for (String fine : history) {
                String[] fParts = fine.split("\\|");
                // Extracting ID from "ID: 4" or similar
                String idPart = fParts[0].replaceAll("[^0-9]", "").trim();

                // Detect status for styling
                boolean isUnpaid = fine.toUpperCase().contains("UNPAID");
                String statusLabel = isUnpaid ? "UNPAID FINES: " : "PAID: ";
                String statusColor = isUnpaid ? "#ef4444" : "#10b981";

                writer.println("<div class='fine-row'>");

                if (fParts.length >= 3) {
                    String user = fParts[1].replace("User:", "").trim();
                    String amt = fParts[2].trim();

                    writer.println("<div class='record-meta'>");
                    writer.println("<span class='id-badge'>ID: " + idPart + "</span>");
                    writer.println("<span class='user-name'>" + user + "</span>");
                    writer.println("<span class='fine-amt' style='color:"+statusColor+";'>" + statusLabel + amt + "</span>");
                    writer.println("</div>");
                } else {
                    writer.println("<span>" + fine + "</span>");
                }

                if (!idPart.isEmpty()) {
                    if ("ADMIN".equals(role)) {
                        // Action for Admin: DELETE
                        writer.println("<form action='" + contextPath + "/delete-fine' method='POST' style='margin:0;' onsubmit='return confirm(\"Are you sure you want to delete this fine record?\");'>");
                        writer.println("<input type='hidden' name='fineId' value='" + idPart + "'>");
                        writer.println("<button type='submit' class='btn btn-delete'>Delete</button>");
                        writer.println("</form>");
                    } else if (isUnpaid) {
                        // Action for Member: PAY
                        writer.println("<form action='" + contextPath + "/pay-fine' method='POST' style='margin:0;'>");
                        writer.println("<input type='hidden' name='fineId' value='" + idPart + "'>");
                        writer.println("<button type='submit' class='btn btn-blue' style='padding:8px 16px; font-size:11px;'>Pay Now</button>");
                        writer.println("</form>");
                    }
                }
                writer.println("</div>");
            }
        }

        writer.println("<div class='actions'>");
        if (!"ADMIN".equals(role)) writer.println("<a href='" + contextPath + "/borrow' class='btn btn-blue'>Borrow New Book</a>");
        writer.println("<a href='" + contextPath + "/books' class='btn btn-slate'>Return to Catalog</a>");
        writer.println("</div>");

        writer.println("</div></div></body></html>");
    }
}