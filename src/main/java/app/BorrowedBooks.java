package app;

import app.LibraryService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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

        List<String> borrowedList;
        if ("ADMIN".equals(role)) {
            borrowedList = libraryService.getAdminBorrowedRecords();
        } else {
            borrowedList = libraryService.getMemberActiveLoans(username);
        }

        writer.println("<!DOCTYPE html><html><head><title>Library | View Loans</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px 20px; color: #0f172a; }");
        writer.println(".container { max-width: 1100px; margin: auto; }");
        writer.println(".admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }");
        writer.println(".admin-header h1 { font-size: 32px; font-weight: 800; letter-spacing: -1.5px; margin: 0; color: #1e293b; }");

        // Added search bar styling
        writer.println(".search-box { width: 100%; padding: 15px 25px; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 30px; font-family: inherit; font-size: 16px; outline: none; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }");
        writer.println(".search-box:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }");

        writer.println(".user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; }");
        writer.println(".user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; gap: 12px; }");
        writer.println(".user-avatar { background: #3b82f6; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; text-transform: uppercase; }");
        writer.println(".user-name { font-weight: 700; font-size: 18px; color: #334155; }");

        writer.println("table { width: 100%; border-collapse: collapse; background: white; }");
        writer.println("th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid #f1f5f9; }");
        writer.println("td { padding: 20px 30px; border-bottom: 1px solid #f8fafc; font-size: 15px; vertical-align: middle; }");
        writer.println("tr:last-child td { border-bottom: none; }");

        writer.println(".id-badge { background: #eff6ff; color: #2563eb; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }");
        writer.println(".book-title { font-weight: 600; color: #1e293b; display: block; }");

        writer.println(".status-pill { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; display: inline-flex; align-items: center; gap: 6px; }");
        writer.println(".pill-overdue { color: #e11d48; background: #fff1f2; border: 1px solid #ffe4e6; }");
        writer.println(".pill-ontime { color: #059669; background: #ecfdf5; border: 1px solid #d1fae5; }");

        writer.println(".btn { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 13px; transition: 0.2s; border: none; cursor: pointer; }");
        writer.println(".btn-return { background: #0f172a; color: white; }");
        writer.println(".btn-return:hover { background: #334155; }");
        writer.println(".nav-bar { display: flex; gap: 15px; margin-top: 40px; padding: 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0; justify-content: center; }");
        writer.println(".btn-outline { border: 1px solid #e2e8f0; color: #64748b; background: transparent; }");
        writer.println(".btn-outline:hover { background: #f8fafc; color: #1e293b; }");
        writer.println("</style>");

        // Added the Search Logic Script
        writer.println("<script>");
        writer.println("function filterContent() {");
        writer.println("  let filter = document.getElementById('searchEngine').value.toLowerCase();");
        writer.println("  let sections = document.getElementsByClassName('user-section');");
        writer.println("  for (let i = 0; i < sections.length; i++) {");
        writer.println("    let text = sections[i].innerText.toLowerCase();");
        writer.println("    sections[i].style.display = text.includes(filter) ? '' : 'none';");
        writer.println("  }");
        writer.println("}");
        writer.println("</script>");

        writer.println("</head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='admin-header'><h1>" + ("ADMIN".equals(role) ? "Books Borrowed" : "My Borrowed Books") + "</h1></div>");

        // Added search bar input
        writer.println("<input type='text' id='searchEngine' onkeyup='filterContent()' class='search-box' placeholder='Search records by ID, Title, or Member name...'>");

        if (borrowedList == null || borrowedList.isEmpty()) {
            writer.println("<div class='user-section' style='padding:80px; text-align:center;'><p style='color:#64748b; font-size:18px;'>📭 No borrowed books found.</p></div>");
        } else {
            Map<String, List<String>> groupedRecords = new LinkedHashMap<>();
            for (String record : borrowedList) {
                String userKey = "Member";
                String[] parts = record.split("\\|");
                for (String p : parts) if (p.contains("User:")) userKey = p.replace("User:", "").trim();
                groupedRecords.computeIfAbsent(userKey, k -> new ArrayList<>()).add(record);
            }

            for (Map.Entry<String, List<String>> entry : groupedRecords.entrySet()) {
                String currentUser = entry.getKey();
                writer.println("<div class='user-section'>");
                writer.println("  <div class='user-header'>");
                writer.println("    <div class='user-avatar'>" + currentUser.substring(0, 1) + "</div>");
                writer.println("    <span class='user-name'>" + currentUser + "'s Borrowed Books</span>");
                writer.println("  </div>");

                writer.println("  <table><thead><tr><th>Record ID</th><th>Book Title</th><th>Status & Timeline</th>");
                if ("ADMIN".equals(role)) writer.println("<th>Management</th>");
                writer.println("  </tr></thead><tbody>");

                for (String record : entry.getValue()) {
                    String[] parts = record.split("\\|");
                    String id = "0", title = "N/A", timeline = "";
                    boolean isOverdue = record.contains("OVERDUE");

                    for (String part : parts) {
                        if (part.contains("ID:")) id = part.replace("ID:", "").trim();
                        else if (part.contains("Book:") || part.contains("Title:")) title = part.replace("Book:", "").replace("Title:", "").trim();
                        else if (part.contains("left") || part.contains("OVERDUE")) timeline = part.trim();
                    }

                    String pillClass = isOverdue ? "pill-overdue" : "pill-ontime";
                    String icon = isOverdue ? "⚠️ " : "⏳ ";

                    writer.println("<tr>");
                    writer.println("  <td><span class='id-badge'>#" + id + "</span></td>");
                    writer.println("  <td><span class='book-title'>" + title + "</span></td>");
                    writer.println("  <td><span class='status-pill " + pillClass + "'>" + icon + timeline + "</span></td>");

                    if ("ADMIN".equals(role)) {
                        writer.println("  <td><form action='" + contextPath + "/return' method='POST' style='margin:0;'>");
                        writer.println("    <input type='hidden' name='borrowId' value='" + id + "'>");
                        writer.println("    <button type='submit' class='btn btn-return' onclick=\"return confirm('Confirm return for #" + id + "?')\">Process Return</button>");
                        writer.println("  </form></td>");
                    }
                    writer.println("</tr>");
                }
                writer.println("</tbody></table></div>");
            }
        }

        writer.println("<div class='nav-bar'>");
        writer.println("<a href='books' class='btn btn-return'>Book Catalog</a>");
        writer.println("<a href='fines' class='btn btn-outline'>Check Fines</a>");
        writer.println("<a href='books' class='btn btn-outline'>Dashboard</a>");
        writer.println("</div></div></body></html>");
    }
}