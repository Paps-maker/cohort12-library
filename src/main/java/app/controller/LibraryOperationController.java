package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.model.Book;
import app.services.BorrowService;
import app.services.BookService;
import app.services.FineService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LibraryOperationController {

    @Inject
    private BorrowService borrowService;
    @Inject
    private BookService bookService;
    @Inject
    private FineService fineService;

    // =========================================================================
    // 1. BORROW INTERFACE (Existing)
    // =========================================================================
    @ActionGetMethod("/library/borrow")
    public void showBorrowForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        List<Book> books = bookService.getAllBooks();
        double unpaidFines = "USER".equals(role) ? fineService.getUnpaidFines(username) : 0;

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html><html><head><title>Library | Borrow</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f0f2f5; margin: 0; padding: 40px; color: #1a1f36; }");
        writer.println(".container { max-width: 500px; margin: auto; background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }");
        writer.println("header { background: #0f172a; color: white; padding: 30px; text-align: center; }");
        writer.println("header h1 { margin: 0; font-size: 24px; letter-spacing: -0.5px; }");
        writer.println("section { padding: 30px; }");
        writer.println("h3 { margin-top: 0; color: #1e293b; }");
        writer.println(".fine-msg { background: #fff1f2; color: #e11d48; padding: 15px; border-radius: 12px; border-left: 4px solid #e11d48; font-weight: 600; margin-bottom: 20px; }");
        writer.println(".admin-msg { background: #eff6ff; color: #1e40af; padding: 15px; border-radius: 12px; border-left: 4px solid #1e40af; font-weight: 600; margin-bottom: 20px; font-size: 14px; }");
        writer.println("select, input[type='number'] { width: 100%; padding: 14px; border: 1px solid #e2e8f0; border-radius: 12px; font-size: 16px; margin: 10px 0; transition: 0.2s; box-sizing: border-box; font-family: inherit; }");
        writer.println("select:focus, input[type='number']:focus { outline: none; border-color: #2563eb; }");
        writer.println("label { font-size: 13px; color: #64748b; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }");
        writer.println("button { background: #0f172a; color: white; border: none; width: 100%; padding: 16px; border-radius: 12px; font-weight: 700; cursor: pointer; transition: 0.3s; margin-top: 10px; }");
        writer.println("button:hover { background: #1e293b; transform: translateY(-1px); }");
        writer.println(".nav-links { margin-top: 25px; display: flex; justify-content: center; gap: 20px; }");
        writer.println(".nav-links a { color: #64748b; text-decoration: none; font-size: 14px; font-weight: 600; padding: 8px 12px; border-radius: 8px; transition: 0.2s; }");
        writer.println(".nav-links a:hover { color: #0f172a; background: #f1f5f9; }");
        writer.println("</style></head><body>");

        writer.println("<div class='container'><header><h1>Library Catalog</h1></header><section>");
        writer.println("<h3>Welcome, " + username + "</h3>");

        boolean isAdmin = "ADMIN".equals(role);
        boolean hasFines = unpaidFines > 0;
        boolean canBorrow = !isAdmin && !hasFines;

        if (isAdmin) {
            writer.println("<div class='admin-msg'>Administrator Account: Borrowing privileges are restricted.</div>");
        } else if (hasFines) {
            writer.println("<div class='fine-msg'> Unpaid Fine: KSH " + String.format("%.2f", unpaidFines) + "<br><small>Borrowing disabled.</small></div>");
        }

        writer.println("<form method='post' action='borrow'>");
        writer.println("<label>Select Book</label><select name='bookId' required " + (!canBorrow ? "disabled" : "") + ">");
        writer.println("<option value='' disabled selected>-- Search Collection --</option>");
        for (Book b : books) { if (b.getAvailableCopies() > 0) writer.println("<option value='" + b.getId() + "'>" + b.getTitle() + "</option>"); }
        writer.println("</select><label>Borrow Duration (1-10 Days)</label>");
        writer.println("<input type='number' name='days' min='1' max='10' value='7' required " + (!canBorrow ? "disabled" : "") + ">");
        writer.println("<button type='submit' " + (!canBorrow ? "style='background:#cbd5e1; cursor:not-allowed;' disabled" : "") + ">" + (isAdmin ? "Borrowing Restricted" : "Confirm Checkout") + "</button>");
        writer.println("</form></section></div>");
        writer.println("<div class='nav-links'><a href='loans'> Borrow History</a><a href='../books'> Catalog</a></div></body></html>");
    }

    @ActionPostMethod("/library/borrow")
    public void processBorrow(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role)) {
            showResponsePage(resp, "Unauthorized", "Administrators cannot borrow books.", false);
            return;
        }
        String result = borrowService.attemptBorrow(username, role, req.getParameter("bookId"), req.getParameter("days"));
        showResponsePage(resp, result.contains("Success") ? "Success" : "Blocked", result, result.contains("Success"));
    }

    // =========================================================================
    // 2. VIEW LOANS (Existing)
    // =========================================================================
    @ActionGetMethod("/library/loans")
    public void viewLoans(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        List<String> borrowedList = "ADMIN".equals(role) ? borrowService.getAdminBorrowedRecords() : borrowService.getMemberActiveLoans(username);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html><html><head><title>Library | View Loans</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px 20px; color: #0f172a; }");
        writer.println(".container { max-width: 1100px; margin: auto; }");
        writer.println(".admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }");
        writer.println(".admin-header h1 { font-size: 32px; font-weight: 800; letter-spacing: -1.5px; margin: 0; color: #1e293b; }");
        writer.println(".search-box { width: 100%; padding: 15px 25px; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 30px; font-family: inherit; font-size: 16px; outline: none; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }");
        writer.println(".user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; }");
        writer.println(".user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; gap: 12px; }");
        writer.println(".user-avatar { background: #3b82f6; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; text-transform: uppercase; }");
        writer.println(".user-name { font-weight: 700; font-size: 18px; color: #334155; }");
        writer.println("table { width: 100%; border-collapse: collapse; background: white; }");
        writer.println("th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid #f1f5f9; }");
        writer.println("td { padding: 20px 30px; border-bottom: 1px solid #f8fafc; font-size: 15px; }");
        writer.println(".id-badge { background: #eff6ff; color: #2563eb; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }");
        writer.println(".book-title { font-weight: 600; color: #1e293b; display: block; }");
        writer.println(".status-pill { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; display: inline-flex; align-items: center; gap: 6px; }");
        writer.println(".pill-overdue { color: #e11d48; background: #fff1f2; border: 1px solid #ffe4e6; }");
        writer.println(".pill-ontime { color: #059669; background: #ecfdf5; border: 1px solid #d1fae5; }");
        writer.println(".btn { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 13px; transition: 0.2s; border: none; cursor: pointer; }");
        writer.println(".btn-return { background: #0f172a; color: white; }");
        writer.println(".nav-bar { display: flex; gap: 15px; margin-top: 40px; padding: 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0; justify-content: center; }");
        writer.println(".btn-outline { border: 1px solid #e2e8f0; color: #64748b; background: transparent; }");
        writer.println("</style>");
        writer.println("<script>function filterContent() { let filter = document.getElementById('searchEngine').value.toLowerCase(); let sections = document.getElementsByClassName('user-section'); for (let i = 0; i < sections.length; i++) { let text = sections[i].innerText.toLowerCase(); sections[i].style.display = text.includes(filter) ? '' : 'none'; } }</script>");
        writer.println("</head><body><div class='container'>");
        writer.println("<div class='admin-header'><h1>" + ("ADMIN".equals(role) ? "Records" : "My Loans") + "</h1></div>");
        writer.println("<input type='text' id='searchEngine' onkeyup='filterContent()' class='search-box' placeholder='Search records...'>");

        if (borrowedList == null || borrowedList.isEmpty()) {
            writer.println("<div class='user-section' style='padding:80px; text-align:center;'><p style='color:#64748b; font-size:18px;'>📭 No records found.</p></div>");
        } else {
            Map<String, List<String>> groups = new LinkedHashMap<>();
            for (String r : borrowedList) {
                String u = "Member";
                String[] parts = r.split("\\|");
                for (String p : parts) if (p.contains("User:")) u = p.replace("User:", "").trim();
                groups.computeIfAbsent(u, k -> new ArrayList<>()).add(r);
            }
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                writer.println("<div class='user-section'><div class='user-header'><div class='user-avatar'>" + entry.getKey().substring(0, 1) + "</div><span class='user-name'>" + entry.getKey() + "</span></div>");
                writer.println("<table><thead><tr><th>ID</th><th>Title</th><th>Timeline</th>" + ("ADMIN".equals(role) ? "<th>Action</th>" : "") + "</tr></thead><tbody>");
                for (String record : entry.getValue()) {
                    String[] parts = record.split("\\|");
                    String id = "0", title = "N/A", timeline = "";
                    boolean isOverdue = record.contains("OVERDUE");
                    for (String part : parts) {
                        if (part.contains("ID:")) id = part.replace("ID:", "").trim();
                        else if (part.contains("Book:") || part.contains("Title:")) title = part.replace("Book:", "").replace("Title:", "").trim();
                        else if (part.contains("left") || part.contains("OVERDUE")) timeline = part.trim();
                    }
                    writer.println("<tr><td><span class='id-badge'>#" + id + "</span></td><td><span class='book-title'>" + title + "</span></td><td><span class='status-pill " + (isOverdue ? "pill-overdue" : "pill-ontime") + "'>" + timeline + "</span></td>");
                    if ("ADMIN".equals(role)) {
                        writer.println("<td><form action='return' method='POST' style='margin:0;'><input type='hidden' name='borrowId' value='" + id + "'><button type='submit' class='btn btn-return'>Return</button></form></td>");
                    }
                    writer.println("</tr>");
                }
                writer.println("</tbody></table></div>");
            }
        }
        writer.println("<div class='nav-bar'><a href='../books' class='btn btn-return'>Catalog</a><a href='../fines' class='btn btn-outline'>Fines</a></div></div></body></html>");
    }

    // =========================================================================
    // 3. ADD BOOK INTERFACE (Updated to include Image URL & Description)
    // =========================================================================
    @ActionGetMethod("/library/addbook")
    public void showAddBookForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html><html><head><title>Library | Add Book</title>");
        writer.println("<style>");
        writer.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }");
        writer.println(".box { background: white; padding: 40px; border-radius: 24px; width: 100%; max-width: 450px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); border: 1px solid #e2e8f0; }");
        writer.println("h2 { color: #0f172a; margin-bottom: 24px; font-weight: 800; letter-spacing: -1px; text-align: center; }");
        writer.println("input, textarea { width: 100%; padding: 14px; margin: 12px 0; border: 1px solid #e2e8f0; border-radius: 12px; box-sizing: border-box; font-size: 15px; font-family: inherit; transition: 0.2s; }");
        writer.println("input:focus, textarea:focus { outline: none; border-color: #2563eb; }");
        writer.println(".btn-primary { width: 100%; padding: 16px; border: none; border-radius: 12px; background: #0f172a; color: white; font-weight: 700; cursor: pointer; margin-top: 10px; transition: 0.2s; }");
        writer.println(".btn-primary:hover { background: #1e293b; transform: translateY(-1px); }");
        writer.println(".label-left { text-align: left; display: block; font-weight: 700; color: #475569; font-size: 12px; margin-top: 10px; text-transform: uppercase; letter-spacing: 0.5px; }");
        writer.println("</style></head><body>");

        writer.println("<div class='box'><h2>Add New Book</h2>");
        writer.println("<form action='addbook' method='POST'>");
        writer.println("<label class='label-left'>Book Details</label>");
        writer.println("<input type='text' name='title' placeholder='Book Title' required>");
        writer.println("<input type='text' name='author' placeholder='Author Name' required>");
        writer.println("<input type='text' name='isbn' placeholder='ISBN Number' required>");
        writer.println("<input type='text' name='imageUrl' placeholder='Cover Image URL (Optional)'>");
        writer.println("<textarea name='description' placeholder='Short Synopsis...' rows='3'></textarea>");
        writer.println("<label class='label-left'>Inventory</label>");
        writer.println("<input type='number' name='copies' value='1' min='1' max='50' required>");
        writer.println("<button type='submit' class='btn-primary'>Save to Database</button>");
        writer.println("<a href='../books' style='display:block; text-align:center; margin-top:15px; font-size:13px; color:#64748b; text-decoration:none;'>Cancel</a>");
        writer.println("</form></div></body></html>");
    }

    @ActionPostMethod("/library/addbook")
    public void processAddBook(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String isbn = req.getParameter("isbn");
        String imageUrl = req.getParameter("imageUrl");
        String description = req.getParameter("description");
        int copies = 1;

        try {
            copies = Integer.parseInt(req.getParameter("copies"));
        } catch (Exception e) { /* default to 1 */ }

        // Create Book with full metadata
        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setIsbn(isbn);
        newBook.setImageUrl(imageUrl);
        newBook.setDescription(description);
        newBook.setTotalQuantity(copies);
        newBook.setAvailableCopies(copies);

        boolean success = bookService.addBook(newBook);

        showResponsePage(resp, success ? "✅ Success" : "⚠️ Error",
                success ? "Title initialized! Stock is now available for borrowing." : "Failed to save the book. Synopsis may be too long.", success);
    }

    // =========================================================================
    // 4. RETURN PROCESSING (Existing)
    // =========================================================================
    @ActionPostMethod("/library/return")
    public void processReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String role = (String) req.getSession().getAttribute("role");
        String id = req.getParameter("borrowId");
        if (id != null) id = id.replaceAll("[^0-9]", "").trim();
        String result = borrowService.processReturnRequest(role, id);
        resp.sendRedirect(req.getContextPath() + "/library/loans?message=" + URLEncoder.encode(result, StandardCharsets.UTF_8));
    }

    private void showResponsePage(HttpServletResponse resp, String title, String message, boolean isSuccess) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        String color = isSuccess ? "#059669" : "#ef4444";
        String bg = isSuccess ? "#ecfdf5" : "#fef2f2";

        writer.println("<html><head><style>@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        writer.println("body{font-family:'Plus Jakarta Sans', sans-serif; display:flex; justify-content:center; align-items:center; height:100vh; background:#f8fafc; margin:0;}");
        writer.println(".card{background:white; padding:40px; border-radius:24px; text-align:center; box-shadow:0 20px 25px -5px rgba(0,0,0,0.1); max-width:400px; border: 1px solid #e2e8f0;}");
        writer.println("h2{color:" + color + "; margin-top:0; font-weight:800;}p{color:#475569; line-height:1.6;}.btn{display:inline-block; margin-top:20px; padding:14px 28px; background:#0f172a; color:white; text-decoration:none; border-radius:12px; font-weight:700;}</style></head><body>");
        writer.println("<div class='card'><h2>" + title + "</h2><p>" + message + "</p><a href='loans' class='btn'>Continue to Dashboard</a></div></body></html>");
    }
}