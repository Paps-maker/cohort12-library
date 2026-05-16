package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.model.Book;
import app.services.BookService;
import app.services.FineService;
import app.services.BorrowService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookController {

    @Inject
    private BookService bookService;

    @Inject
    private FineService fineService;

    @Inject
    private BorrowService borrowService;

    // --- 1. LIST BOOKS (DASHBOARD) ---
    @ActionGetMethod("/books")
    public void listBooks(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();

        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(contextPath + "/login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Server-side Date Calculation
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
        String formattedDate = now.format(formatter);

        // Data Fetching Logic from your original Servlet
        List<Book> allBooks = bookService.getAllBooks();
        int totalUniqueTitles = allBooks.size();
        int totalPhysicalCopies = allBooks.stream().mapToInt(Book::getQuantity).sum();
        int availableCount = bookService.getAvailableCount();
        int totalSystemBorrowed = totalPhysicalCopies - availableCount;
        int userBorrowedCount = borrowService.getMemberBorrowedCount(username);

        int displayBorrowedCount = "ADMIN".equals(role) ? totalSystemBorrowed : userBorrowedCount;
        String borrowedLabel = "ADMIN".equals(role) ? "Total Copies Borrowed" : "My Borrowed Books";

        // Fine Calculation Logic from original Servlet
        double totalOwed;
        String fineLabel;
        if ("ADMIN".equals(role)) {
            totalOwed = fineService.getTotalSystemRiskDebt();
            fineLabel = "Outstanding FINES: KSH ";
        } else {
            totalOwed = fineService.getUnpaidFines(username) + fineService.getProjectedLateFees(username);
            fineLabel = "UNPAID FINES: KSH ";
        }

        out.println("<!DOCTYPE html><html><head><title>Library Dashboard</title>");
        sendDashboardStyles(out);
        out.println("</head><body>");

        out.println("<div class='header'> SCHOOL LIBRARY </div>");

        // Time Display
        out.println("<div class='time-container'>");
        out.println("    <div class='time-card'>");
        out.println("        <div class='time-value'>" + formattedDate + "</div>");
        out.println("        <div id='clock' style='font-size: 13px; opacity: 0.9; margin-top: 4px; font-family: monospace;'></div>");
        out.println("    </div>");
        out.println("</div>");

        out.println("<div class='container'>");

        // Welcome Row with Fine Alerts
        out.println("<div class='welcome-row'>");
        out.println("<div class='welcome'>Welcome back, " + username + "</div>");
        if (totalOwed > 0) {
            String adminStyle = "ADMIN".equals(role) ? "style='background:#1e293b; color:#fbbf24; border-color:#fbbf24;'" : "";
            out.println("<a href='" + contextPath + "/fines' class='fine-alert' " + adminStyle + ">"
                    + fineLabel + String.format("%.2f", totalOwed) + "</a>");
        }
        out.println("</div>");

        // Stats Grid
        out.println("<div class='stats'>");
        out.println("<div class='stat-card'><div class='stat-title'>Active Users</div><div class='stat-value'><span id='activeUsers'>...</span></div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Total Titles</div><div class='stat-value'>" + totalUniqueTitles + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>" + borrowedLabel + "</div><div class='stat-value'>" + displayBorrowedCount + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Copies Available</div><div class='stat-value'>" + availableCount + "</div></div>");
        out.println("</div>");

        // Book Shelf
        out.println("<div class='library'>");
        out.println("<div class='library-title'>Book Collection</div>");
        out.println("<div class='shelf'>");

        for (Book book : allBooks) {
            int remaining = book.getAvailableCopies();
            boolean isAvailable = remaining > 0;
            String imgUrl = (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) ? book.getImageUrl() : "https://via.placeholder.com/300x450?text=No+Cover";
            String desc = (book.getDescription() != null) ? book.getDescription() : "No description.";

            out.println("<div class='book'>");
            out.println("<img src='" + imgUrl + "' class='book-cover'>");
            out.println("<div class='book-details'>");

            if (!isAvailable) {
                out.println("<span class='badge bg-red'>All copies borrowed</span>");
                int daysLeft = bookService.getDaysUntilAvailable(book.getTitle());
                out.println("<span class='wait-text'>" + (daysLeft > 0 ? "Available in " + daysLeft + " days" : "Check back later") + "</span>");
            } else {
                out.println("<span class='badge bg-green'>" + remaining + (remaining == 1 ? " Copy Left" : " Copies Left") + "</span>");
            }

            out.println("<div class='book-title'>" + book.getTitle() + "</div>");
            out.println("<div class='book-description'>" + desc + "</div>");

            if ("ADMIN".equals(role)) {
                out.println("<div class='book-actions'>");
                out.println("<a class='btn-edit' href='" + contextPath + "/edit-book?id=" + book.getId() + "'>Edit</a>");
                out.println("<a class='btn-delete' href='" + contextPath + "/delete-book?id=" + book.getId() + "' onclick='return confirm(\"Delete this book?\")'>Delete</a>");
                out.println("</div>");
            }
            out.println("</div></div>");
        }
        out.println("</div></div>");

        // Footer Navigation
        out.println("<div class='links'>");
        out.println("<a href='" + contextPath + "/members'>Members</a>");
        out.println("<a href='" + contextPath + "/library/borrow'>Borrow Book</a>");
        out.println("<a href='" + contextPath + "/fines' style='background:#f59e0b; color:white; border:none;'>Fines Dashboard</a>");
        out.println("<a href='" + contextPath + "/library/loans'>Borrowed Books</a>");

        if ("ADMIN".equals(role)) {
            out.println("<a href='" + contextPath + "/library/addbook' style='display:inline-block; padding:10px 20px; background:#10b981; color:white; border:none; border-radius:8px; text-decoration:none; font-weight:600;'>+ Add Book</a>");
            out.println("<a href='" + contextPath + "/admin/analytics' style='background:#4f46e5; color:white; border:none;'>Analytics</a>");
        }
        out.println("<a href='" + contextPath + "/logout' style='background:#ef4444; color:white; border:none;'>Logout</a>");
        out.println("</div></div>");

        sendScripts(out);
        out.println("</body></html>");
    }

    // --- 2. DELETE BOOK ---
    @ActionGetMethod("/delete-book")
    public void deleteBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        String contextPath = request.getContextPath();

        try {
            String idParam = request.getParameter("id");
            if (idParam != null && "ADMIN".equals(role)) {
                bookService.deleteBook(Integer.parseInt(idParam), role);
            }
        } catch (Exception e) { e.printStackTrace(); }
        response.sendRedirect(contextPath + "/books");
    }

    // --- 3. SHOW EDIT FORM ---
    @ActionGetMethod("/edit-book")
    public void showEditForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();

        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect(contextPath + "/books");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        Book book = bookService.getBookById(id);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head><title>Edit Book</title>");
        sendDashboardStyles(out);
        sendFormStyles(out);
        out.println("</head><body>");
        out.println("<div class='header'> EDIT: " + book.getTitle().toUpperCase() + " </div>");
        out.println("<div class='container'><div class='form-wrapper'>");
        out.println("<form action='" + contextPath + "/update-book' method='POST' class='add-form'>");
        out.println("<input type='hidden' name='id' value='" + book.getId() + "'/>");
        out.println("<div class='input-group'><label>Book Title</label><input type='text' name='title' value='" + book.getTitle() + "' required></div>");
        out.println("<div class='input-group'><label>Image URL</label><input type='url' name='imageUrl' value='" + (book.getImageUrl() != null ? book.getImageUrl() : "") + "'></div>");
        out.println("<div class='input-group'><label>Current Quantity: " + book.getQuantity() + " (Add extra copies below)</label>");
        out.println("<input type='number' name='addCopies' value='0' min='0'></div>");
        out.println("<div class='input-group'><label>Description</label><textarea name='description' rows='4'>" + (book.getDescription() != null ? book.getDescription() : "") + "</textarea></div>");
        out.println("<div class='form-actions'><button type='submit' class='btn-save'>Save Changes</button>");
        out.println("<a href='" + contextPath + "/books' class='btn-cancel'>Cancel</a></div>");
        out.println("</form></div></div></body></html>");
    }

    // --- 4. UPDATE BOOK (POST) ---
    @ActionPostMethod("/update-book")
    public void updateBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String contextPath = request.getContextPath();

        if (session != null && "ADMIN".equals(session.getAttribute("role"))) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                int addCopies = Integer.parseInt(request.getParameter("addCopies"));

                Book book = bookService.getBookById(id);
                book.setTitle(request.getParameter("title"));
                book.setImageUrl(request.getParameter("imageUrl"));
                book.setDescription(request.getParameter("description"));

                bookService.updateBook(book, addCopies);
            } catch (Exception e) { e.printStackTrace(); }
        }
        response.sendRedirect(contextPath + "/books");
    }

    private void sendDashboardStyles(PrintWriter out) {
        out.println("<style>");
        out.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        out.println(":root { --primary: #1e3c72; --secondary: #2a5298; --bg: #ffffff; --card-bg: #f8fafc; --text: #334155; }");
        out.println("body { font-family: 'Plus Jakarta Sans', sans-serif; margin:0; background:var(--bg); color:var(--text); }");
        out.println(".header { background: linear-gradient(135deg, #1e3c72, #2a5298); color:white; padding:25px; text-align:center; font-size:24px; font-weight:800; letter-spacing: 1px; }");
        out.println(".time-container { display: flex; justify-content: center; margin-top: -15px; margin-bottom: 20px; }");
        out.println(".time-card { background: #0f172a; color: white; padding: 12px 25px; border-radius: 0 0 15px 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; }");
        out.println(".time-value { font-size: 15px; font-weight: 700; }");
        out.println(".container { max-width: 1200px; margin: auto; padding: 20px; }");
        out.println(".welcome-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }");
        out.println(".welcome { font-size: 20px; font-weight: 700; color: #0f172a; }");
        out.println(".fine-alert { background: #fee2e2; color: #b91c1c; padding: 10px 18px; border-radius: 12px; font-size: 13px; font-weight: 800; text-decoration: none; border: 1px solid #fecaca; }");
        out.println(".stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 15px; margin-bottom: 30px; }");
        out.println(".stat-card { background: var(--card-bg); padding: 20px; border-radius: 16px; border: 1px solid #e2e8f0; text-align: center; }");
        out.println(".stat-title { font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: 800; letter-spacing: 0.5px; }");
        out.println(".stat-value { font-size: 24px; font-weight: 800; color: var(--primary); margin-top: 5px; }");
        out.println(".library-title { font-size: 20px; font-weight: 800; margin-bottom: 20px; border-left: 5px solid var(--secondary); padding-left: 12px; }");
        out.println(".shelf { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 25px; }");
        out.println(".book { background: white; border-radius: 15px; overflow: hidden; border: 1px solid #e2e8f0; display: flex; flex-direction: column; transition: 0.3s; }");
        out.println(".book:hover { transform: translateY(-5px); box-shadow: 0 12px 20px rgba(0,0,0,0.08); }");
        out.println(".book-cover { width: 100%; height: 280px; object-fit: cover; background: #f1f5f9; }");
        out.println(".book-details { padding: 18px; flex-grow: 1; display: flex; flex-direction: column; }");
        out.println(".book-title { font-size: 16px; font-weight: 800; color: #0f172a; margin-bottom: 8px; line-height: 1.3; }");
        out.println(".book-description { font-size: 13px; color: #64748b; margin-bottom: 15px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }");
        out.println(".badge { font-size: 11px; font-weight: 800; padding: 5px 10px; border-radius: 6px; text-transform: uppercase; margin-bottom: 8px; align-self: flex-start; }");
        out.println(".bg-green { background: #dcfce7; color: #166534; }");
        out.println(".bg-red { background: #fee2e2; color: #991b1b; }");
        out.println(".wait-text { font-size: 12px; color: #ef4444; font-weight: 700; margin-bottom: 10px; }");
        out.println(".book-actions { display: flex; gap: 8px; margin-top: auto; }");
        out.println(".btn-edit, .btn-delete { flex: 1; text-align: center; font-size: 12px; padding: 10px; border-radius: 8px; text-decoration: none; color: white; font-weight: 700; }");
        out.println(".btn-edit { background: #3b82f6; }");
        out.println(".btn-delete { background: #ef4444; }");
        out.println(".links { margin-top: 40px; display: flex; flex-wrap: wrap; gap: 12px; }");
        out.println(".links a { padding: 12px 20px; border-radius: 10px; text-decoration: none; font-size: 14px; font-weight: 700; background: #f1f5f9; color: #1e293b; border: 1px solid #e2e8f0; transition: 0.2s; }");
        out.println(".links a:hover { background: var(--secondary); color: white; }");
        out.println("</style>");
    }

    private void sendFormStyles(PrintWriter out) {
        out.println("<style>");
        out.println(".form-wrapper { max-width: 550px; margin: 30px auto; background: white; padding: 35px; border-radius: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }");
        out.println(".add-form { display: flex; flex-direction: column; gap: 18px; }");
        out.println(".input-group { display: flex; flex-direction: column; gap: 8px; }");
        out.println(".input-group label { font-size: 12px; font-weight: 800; color: #64748b; text-transform: uppercase; }");
        out.println(".input-group input, .input-group textarea { padding: 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-family: inherit; font-size: 14px; transition: 0.2s; }");
        out.println(".input-group input:focus { border-color: var(--secondary); outline: none; }");
        out.println(".form-actions { display: flex; gap: 12px; margin-top: 15px; }");
        out.println(".btn-save { flex: 2; background: #10b981; color: white; border: none; padding: 14px; border-radius: 10px; font-weight: 700; cursor: pointer; font-size: 15px; }");
        out.println(".btn-cancel { flex: 1; text-align: center; background: #f8fafc; color: #64748b; text-decoration: none; padding: 14px; border-radius: 10px; font-weight: 700; border: 1.5px solid #e2e8f0; }");
        out.println("</style>");
    }

    private void sendScripts(PrintWriter out) {
        out.println("<script>");
        out.println("function updateClock() {");
        out.println("    const now = new Date();");
        out.println("    document.getElementById('clock').innerHTML = now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', second:'2-digit'});");
        out.println("}");
        out.println("setInterval(updateClock, 1000); updateClock();");

        // Polling for active users
        out.println("function loadUsers() {");
        out.println("  fetch('active-users').then(r => r.text()).then(d => {");
        out.println("    if (d && d.length < 10) document.getElementById('activeUsers').innerText = d;");
        out.println("  }).catch(() => {});");
        out.println("}");
        out.println("setInterval(loadUsers, 5000); loadUsers();");
        out.println("</script>");
    }
}