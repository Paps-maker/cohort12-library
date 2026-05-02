package app;

// ✅ CORRECTED IMPORT: LibraryService is in package 'app', not 'app.ejbs'
import app.LibraryService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/borrowed")
public class BorrowedBooks extends HttpServlet {

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

        // ✅ DATA SOURCE: Admin sees the master log; Members see their personal active loans
        List<String> borrowedList;
        if ("ADMIN".equals(role)) {
            // These methods now match LibraryService.java exactly
            borrowedList = libraryService.getAdminBorrowedRecords();
        } else {
            borrowedList = libraryService.getMemberActiveLoans(username);
        }

        writer.println("<!DOCTYPE html><html><head><title>Library | Dashboard</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; margin: 0; padding: 40px 20px; color: #1e293b; line-height: 1.6; }");
        writer.println(".container { max-width: 1000px; margin: auto; }");
        writer.println(".header-bar { background: #0f172a; color: white; padding: 40px; border-radius: 24px 24px 0 0; }");
        writer.println(".header-bar h2 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -1px; }");
        writer.println(".card { background: white; padding: 30px; border-radius: 0 0 24px 24px; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); border: 1px solid #f1f5f9; border-top: none; }");
        writer.println(".alert { padding: 15px; border-radius: 12px; margin-bottom: 25px; font-size: 14px; font-weight: 600; border: 1px solid; }");
        writer.println(".alert-success { background: #dcfce7; color: #166534; border-color: #bbf7d0; }");
        writer.println(".alert-error { background: #fee2e2; color: #991b1b; border-color: #fecaca; }");
        writer.println("ul { list-style: none; padding: 0; margin: 0; }");
        writer.println("li { display: flex; justify-content: space-between; align-items: center; padding: 20px; margin-bottom: 16px; background: #ffffff; border: 1px solid #f1f5f9; border-radius: 16px; transition: all 0.3s ease; }");
        writer.println("li:hover { border-color: #3b82f6; transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(59, 130, 246, 0.1); }");
        writer.println(".record-content { display: flex; align-items: center; gap: 20px; flex-grow: 1; }");
        writer.println(".id-badge { background: #eff6ff; color: #2563eb; padding: 4px 12px; border-radius: 8px; font-size: 12px; font-weight: 800; border: 1px solid #dbeafe; }");
        writer.println(".book-title { color: #1e293b; font-weight: 600; font-size: 16px; }");
        writer.println(".book-meta { color: #94a3b8; font-size: 13px; }");
        writer.println(".btn { display: inline-flex; align-items: center; gap: 8px; padding: 12px 24px; text-decoration: none; border-radius: 12px; font-weight: 600; font-size: 14px; transition: 0.2s; border: none; cursor: pointer; }");
        writer.println(".btn-return { background: #fff1f2; color: #e11d48; border: 1px solid #ffe4e6; font-family: inherit; }");
        writer.println(".btn-return:hover { background: #e11d48; color: white; }");
        writer.println(".btn-borrow { background: #2563eb; color: white; }");
        writer.println(".btn-fines { background: #059669; color: white; }");
        writer.println(".btn-back { background: #f1f5f9; color: #475569; }");
        writer.println(".actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 40px; justify-content: center; border-top: 1px solid #f1f5f9; padding-top: 30px; }");
        writer.println("</style></head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='header-bar'><h2>" + ("ADMIN".equals(role) ? "System Administration: Borrow Logs" : "My Borrowed Library") + "</h2></div>");
        writer.println("<div class='card'>");

        // Handle Messages from Return Process
        String status = req.getParameter("status");
        String error = req.getParameter("error");
        if ("success".equals(status)) {
            writer.println("<div class='alert alert-success'>✅ Book returned and available inventory updated.</div>");
        } else if (error != null) {
            writer.println("<div class='alert alert-error'>❌ Transaction failed: " + error + "</div>");
        }

        if (borrowedList == null || borrowedList.isEmpty()) {
            writer.println("<p style='text-align:center; color:#94a3b8; padding:60px 0;'>📭 No active borrowed books found.</p>");
        } else {
            writer.println("<ul>");
            for (String record : borrowedList) {
                String[] parts = record.split("\\|");
                String id = "N/A", title = "Unknown", meta = "";

                for (String part : parts) {
                    if (part.contains("ID:")) {
                        id = part.replace("ID:", "").trim();
                    } else if (part.contains("Book:") || part.contains("Title:")) {
                        title = part.replace("Book:", "").replace("Title:", "").trim();
                    } else if (part.contains("User:") || part.contains("left") || part.contains("OVERDUE") || part.contains("Title:")) {
                        meta += part.trim() + " ";
                    }
                }

                writer.println("<li>");
                writer.println("<div class='record-content'>");
                writer.println("  <span class='id-badge'>#" + id + "</span>");
                writer.println("  <div style='display:flex; flex-direction:column;'>");
                writer.println("    <span class='book-title'>" + title + "</span>");
                writer.println("    <span class='book-meta'>" + meta + "</span>");
                writer.println("  </div>");
                writer.println("</div>");

                if ("ADMIN".equals(role)) {
                    writer.println("<form action='" + contextPath + "/return' method='POST' style='margin:0;'>");
                    writer.println("<input type='hidden' name='borrowId' value='" + id + "'>");
                    writer.println("<button type='submit' class='btn btn-return' onclick=\"return confirm('Confirm book return for record #" + id + "? Inventory will be restocked.')\">Process Return</button>");
                    writer.println("</form>");
                }
                writer.println("</li>");
            }
            writer.println("</ul>");
        }

        writer.println("<div class='actions'>");
        if (!"ADMIN".equals(role)) {
            writer.println("<a href='books' class='btn btn-borrow'> Borrow another book</a>");
        }
        writer.println("<a href='fines' class='btn btn-fines'>Fine Dashboard</a>");
        writer.println("<a href='books' class='btn btn-back'>Return to Catalog</a>");
        writer.println("</div></div></div></body></html>");
    }
}