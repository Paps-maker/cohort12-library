package app;

import app.model.Book;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/books")
public class BookServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String contextPath = request.getContextPath();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // DATA FETCHING
        List<Book> allBooks = libraryService.getAllBooks();
        int totalBooks = allBooks.size();
        int availableCount = libraryService.getAvailableCount();
        int displayBorrowedCount = libraryService.getBorrowedCountForUser(username, role);
        String borrowedLabel = libraryService.getBorrowedLabel(role);

        double totalOwed;
        String fineLabel;

        if ("ADMIN".equals(role)) {
            totalOwed = libraryService.getTotalSystemRiskDebt();
            fineLabel = "SYSTEM DEBT: KSH ";
        } else {
            double recordedFines = libraryService.getUnpaidFines(username);
            double projectedFines = libraryService.getProjectedLateFees(username);
            totalOwed = recordedFines + projectedFines;
            fineLabel = "UNPAID FINES: KSH ";
        }

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Library Dashboard</title>");

        // CLEAN WHITE THEME CSS - NO HOVER REQUIRED
        out.println("<style>");
        out.println(":root { --primary: #1e3c72; --secondary: #2a5298; --bg: #ffffff; --card-bg: #f8fafc; --text: #334155; }");
        out.println("body { font-family:'Segoe UI', system-ui, sans-serif; margin:0; background:var(--bg); color:var(--text); }");
        out.println(".header { background: linear-gradient(135deg, #1e3c72, #2a5298); color:white; padding:20px; text-align:center; font-size:22px; font-weight:bold; }");
        out.println(".container { max-width: 1200px; margin: auto; padding: 20px; }");

        out.println(".welcome-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }");
        out.println(".welcome { font-size: 18px; color: #1e293b; font-weight: 600; }");
        out.println(".fine-alert { background: #fee2e2; color: #b91c1c; padding: 8px 16px; border-radius: 6px; font-size: 12px; font-weight: bold; text-decoration: none; border: 1px solid #fecaca; }");

        out.println(".stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 30px; }");
        out.println(".stat-card { background: var(--card-bg); padding: 15px; border-radius: 8px; border: 1px solid #e2e8f0; text-align: center; }");
        out.println(".stat-title { font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 700; }");
        out.println(".stat-value { font-size: 22px; font-weight: 800; color: var(--primary); }");

        out.println(".library-title { font-size: 20px; font-weight: 700; margin-bottom: 20px; color: #1e293b; border-left: 4px solid var(--secondary); padding-left: 10px; }");

        // GRID SETUP
        out.println(".shelf { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }");

        // CARD DESIGN (STATIC)
        out.println(".book { background: white; border-radius: 10px; overflow: hidden; border: 1px solid #e2e8f0; display: flex; flex-direction: column; transition: box-shadow 0.3s; }");
        out.println(".book:hover { box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); }");

        out.println(".book-cover { width: 100%; height: 260px; object-fit: cover; background: #f1f5f9; border-bottom: 1px solid #f1f5f9; }");

        out.println(".book-details { padding: 15px; flex-grow: 1; display: flex; flex-direction: column; }");
        out.println(".book-title { font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 5px; min-height: 40px; }");
        out.println(".book-description { font-size: 12px; color: #64748b; margin-bottom: 12px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }");

        out.println(".badge { font-size: 10px; font-weight: 800; padding: 4px 8px; border-radius: 4px; text-transform: uppercase; display: inline-block; margin-bottom: 5px; }");
        out.println(".bg-green { background: #dcfce7; color: #166534; }");
        out.println(".bg-red { background: #fee2e2; color: #991b1b; }");
        out.println(".wait-text { font-size: 11px; color: #ef4444; font-weight: 600; margin-bottom: 10px; display: block; }");

        out.println(".book-actions { display: flex; gap: 8px; margin-top: auto; }");
        out.println(".btn-edit, .btn-delete { flex: 1; text-align: center; font-size: 11px; padding: 7px; border-radius: 4px; text-decoration: none; color: white; font-weight: bold; }");
        out.println(".btn-edit { background: #3b82f6; }");
        out.println(".btn-delete { background: #ef4444; }");

        out.println(".links { margin-top: 40px; display: flex; flex-wrap: wrap; gap: 10px; }");
        out.println(".links a { padding: 10px 20px; border-radius: 6px; text-decoration: none; font-size: 13px; font-weight: 600; background: #f1f5f9; color: #1e293b; border: 1px solid #e2e8f0; transition: 0.2s; }");
        out.println(".links a:hover { background: var(--secondary); color: white; }");
        out.println("</style>");

        out.println("</head><body>");

        out.println("<div class='header'> SCHOOL LIBRARY SYSTEM </div>");
        out.println("<div class='container'>");

        out.println("<div class='welcome-row'>");
        out.println("<div class='welcome'>Welcome back, " + username + "</div>");

        if (totalOwed > 0) {
            String adminStyle = "style='background:#1e293b; color:#fbbf24; border-color:#fbbf24;'";
            out.println("<a href='fines' class='fine-alert' " + ("ADMIN".equals(role) ? adminStyle : "") + ">"
                    + fineLabel + String.format("%.2f", totalOwed) + "</a>");
        }
        out.println("</div>");

        out.println("<div class='stats'>");
        out.println("<div class='stat-card'><div class='stat-title'>Active Users</div><div class='stat-value'><span id='activeUsers'>...</span></div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Total Books</div><div class='stat-value'>" + totalBooks + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>" + borrowedLabel + "</div><div class='stat-value'>" + displayBorrowedCount + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Available Now</div><div class='stat-value'>" + availableCount + "</div></div>");
        out.println("</div>");

        out.println("<div class='library'>");
        out.println("<div class='library-title'>Book Collection</div>");
        out.println("<div class='shelf'>");

        for (Book book : allBooks) {
            boolean isAvailable = libraryService.isBookAvailable(book.getId());
            String imgUrl = (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) ? book.getImageUrl() : "https://via.placeholder.com/300x450?text=No+Cover";
            String desc = (book.getDescription() != null && !book.getDescription().isEmpty()) ? book.getDescription() : "No description provided.";

            out.println("<div class='book'>");
            out.println("<img src='" + imgUrl + "' class='book-cover' alt='Book Cover'>");

            out.println("<div class='book-details'>");

            if (!isAvailable) {
                out.println("<span class='badge bg-red'>Borrowed</span>");
                int daysLeft = libraryService.getDaysUntilAvailable(book.getTitle());
                String waitMsg = (daysLeft > 0) ? "Available in " + daysLeft + " days" : "Return in process..";
                out.println("<span class='wait-text'>" + waitMsg + "</span>");
            } else {
                out.println("<span class='badge bg-green'>Available</span>");
            }

            out.println("<div class='book-title'>" + book.getTitle() + "</div>");
            out.println("<div class='book-description'>" + desc + "</div>");

            if ("ADMIN".equals(role)) {
                out.println("<div class='book-actions'>");
                out.println("<a class='btn-edit' href='edit-book?id=" + book.getId() + "'>Edit</a>");
                out.println("<a class='btn-delete' href='delete-book?id=" + book.getId() + "' onclick='return confirm(\"Delete this book?\")'>Delete</a>");
                out.println("</div>");
            }
            out.println("</div></div>");
        }

        out.println("</div></div>");

        out.println("<div class='links'>");
        out.println("<a href='members'>Members</a>");
        out.println("<a href='borrow'>Borrow Book</a>");
        out.println("<a href='fines' style='background:#f59e0b; color:white; border:none;'>Fines Dashboard</a>");
        out.println("<a href='borrowed'>Borrowed books</a>");

        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/addbook' style='background:#10b981; color:white; border:none;'>+ Add Book</a>");
        }

        out.println("<a href='logout' style='background:#ef4444; color:white; border:none;'>Logout</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("<script>");
        out.println("function loadUsers() {");
        out.println("  fetch('active-users')");
        out.println("    .then(r => { if (r.redirected) { window.location.reload(); return; } return r.text(); })");
        out.println("    .then(d => { if (d && d.length < 10) { document.getElementById('activeUsers').innerText = d; } })");
        out.println("    .catch(err => console.log('Session expired'));");
        out.println("}");
        out.println("setInterval(loadUsers, 3000); loadUsers();");
        out.println("</script>");

        out.println("</body></html>");
    }
}