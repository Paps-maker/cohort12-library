package app;

import app.dao.BookDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/books")
public class BookServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ✅ CHECK SESSION
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role"); // ✅ GET ROLE

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // ✅ DAO
        BookDAO dao = new BookDAO();
        List<Book> books = dao.getAllBooks();

        // ✅ CONTEXT
        ServletContext context = getServletContext();
        List<String> borrowed = (List<String>) context.getAttribute("borrowedBooks");

        int totalBooks = books.size();
        int borrowedCount = borrowed != null ? borrowed.size() : 0;
        int available = totalBooks - borrowedCount;

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Library Dashboard</title>");

        out.println("<style>");

        /* ===== KEEP YOUR CSS EXACTLY ===== */
        out.println(".links { margin-top: 25px; display: flex; flex-wrap: wrap; gap: 10px; }");

        out.println(".links a { display: inline-block; padding: 10px 16px; border-radius: 8px; text-decoration: none; font-size: 13px; font-weight: 600; transition: all 0.3s ease; box-shadow: 0 3px 8px rgba(0,0,0,0.08); background:#2a5298; color:white;}");

        out.println(".links a:hover { background:#1e3c72; transform:translateY(-2px);} ");

        out.println(".links a[href='add-book.jsp'] { background:#27ae60; }");
        out.println(".links a[href='add-book.jsp']:hover { background:#1e8449; }");

        out.println(".links a[href='logout'] { background:#e74c3c; }");
        out.println(".links a[href='logout']:hover { background:#c0392b; }");

        out.println("body { font-family:'Segoe UI'; margin:0; background:#f4f6f8; }");

        out.println(".header { background:linear-gradient(135deg,#1e3c72,#2a5298); color:white; padding:18px; text-align:center; font-size:20px; font-weight:bold;}");

        out.println(".container { max-width:1100px; margin:auto; padding:20px;}");

        out.println(".welcome { font-size:18px; margin:15px 0; color:#2c3e50;}");

        out.println(".stats { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:12px; margin-bottom:20px;}");

        out.println(".stat-card { background:white; padding:15px; border-radius:10px; text-align:center; box-shadow:0 3px 10px rgba(0,0,0,0.08); border-left:4px solid #2a5298;}");

        out.println(".stat-title { font-size:12px; color:#777;}");

        out.println(".stat-value { font-size:20px; font-weight:bold; color:#2c3e50; margin-top:5px;}");

        out.println(".library { margin-top:25px;}");

        out.println(".library-title { font-size:18px; font-weight:bold; margin-bottom:12px; color:#2c3e50; border-left:4px solid #2a5298; padding-left:10px;}");

        out.println(".shelf { display:flex; flex-wrap:wrap; gap:12px; background:white; padding:20px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);} ");

        out.println(".book { display:flex; width:230px; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 3px 10px rgba(0,0,0,0.05); transition:0.3s;}");

        out.println(".book:hover { transform:translateY(-5px); box-shadow:0 8px 18px rgba(0,0,0,0.12);} ");

        out.println(".spine { width:6px; background:linear-gradient(180deg,#2a5298,#1e3c72);} ");

        out.println(".book-content { padding:12px;}");

        out.println(".book-title { font-size:13px; font-weight:600; color:#2c3e50;}");

        /* ✅ NEW: ACTION BUTTONS (SMALL, CLEAN) */
        out.println(".book-actions { margin-top:8px; display:flex; gap:6px; }");

        out.println(".btn-edit, .btn-delete { font-size:11px; padding:4px 8px; border-radius:5px; text-decoration:none; color:white;}");

        out.println(".btn-edit { background:#3498db; }");
        out.println(".btn-edit:hover { background:#2c80b4; }");

        out.println(".btn-delete { background:#e74c3c; }");
        out.println(".btn-delete:hover { background:#c0392b; }");

        out.println("</style>");

        out.println("</head><body>");

        out.println("<div class='header'> SCHOOL LIBRARY </div>");
        out.println("<div class='container'>");

        out.println("<div class='welcome'>Welcome, " + username + " </div>");

        out.println("<div class='stats'>");
        out.println("<div class='stat-card'><div class='stat-title'>Active Users</div><div class='stat-value'><span id='activeUsers'>Loading...</span></div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Total Books</div><div class='stat-value'>" + totalBooks + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Borrowed</div><div class='stat-value'>" + borrowedCount + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Available</div><div class='stat-value'>" + available + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>System Usage</div><div class='stat-value'>" + RequestListener.getTotalRequests() + "</div></div>");
        out.println("</div>");

        out.println("<script>");
        out.println("function loadUsers(){fetch('active-users').then(r=>r.text()).then(d=>document.getElementById('activeUsers').innerText=d);} ");
        out.println("setInterval(loadUsers,2000);loadUsers();");
        out.println("</script>");

        out.println("<div class='library'>");
        out.println("<div class='library-title'>Available Books Collection</div>");
        out.println("<div class='shelf'>");

        // ✅ BOOK LOOP WITH ADMIN ACTIONS
        for (Book book : books) {

            out.println("<div class='book'>");
            out.println("<div class='spine'></div>");
            out.println("<div class='book-content'>");

            out.println("<div class='book-title'>" + book.getTitle() + "</div>");

            // ✅ ONLY ADMIN SEES THESE
            if ("ADMIN".equals(role)) {
                out.println("<div class='book-actions'>");
                out.println("<a class='btn-edit' href='edit-book?id=" + book.getId() + "'>Edit</a>");
                out.println("<a class='btn-delete' href='delete-book?id=" + book.getId() + "' onclick='return confirm(\"Delete this book?\")'>Delete</a>");
                out.println("</div>");
            }

            out.println("</div>");
            out.println("</div>");
        }

        out.println("</div></div>");

        out.println("<div class='links'>");
        out.println("<a href='members'> Members</a>");
        out.println("<a href='borrow'>Borrow Book</a>");

        if ("ADMIN".equals(role)) {
            out.println("<a href='add-book.jsp'>Add Book</a>");
        }

        out.println("<a href='logout'> Logout</a>");
        out.println("</div>");

        out.println("</div></body></html>");
    }
}