package app;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
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

        //  CHECK SESSION
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        //  DAOs
        BookDAO bookDao = new BookDAO();
        BorrowDAO borrowDao = new BorrowDAO();

        //  DATA FETCHING
        List<Book> allBooks = bookDao.getAllBooks();

        // Logic for the Borrowed Card
        int displayBorrowedCount;
        String borrowedLabel;

        if ("ADMIN".equals(role)) {
            displayBorrowedCount = borrowDao.getAllBorrowed().size();
            borrowedLabel = "Total Borrowed";
        } else {
            displayBorrowedCount = borrowDao.getUserBorrowed(username).size();
            borrowedLabel = "My Borrowed";
        }

        int totalBooks = allBooks.size();
        int totalBorrowedGlobal = borrowDao.getAllBorrowed().size();
        int available = totalBooks - totalBorrowedGlobal;

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Library Dashboard</title>");

        out.println("<style>");
        /* ===== STYLING UPDATES FOR IMAGES ===== */
        out.println("body { font-family:'Segoe UI'; margin:0; background:#f4f6f8; }");
        out.println(".header { background:linear-gradient(135deg,#1e3c72,#2a5298); color:white; padding:18px; text-align:center; font-size:20px; font-weight:bold;}");
        out.println(".container { max-width:1100px; margin:auto; padding:20px;}");
        out.println(".welcome { font-size:18px; margin:15px 0; color:#2c3e50;}");

        out.println(".stats { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:12px; margin-bottom:20px;}");
        out.println(".stat-card { background:white; padding:15px; border-radius:10px; text-align:center; box-shadow:0 3px 10px rgba(0,0,0,0.08); border-left:4px solid #2a5298;}");
        out.println(".stat-title { font-size:11px; color:#777; text-transform:uppercase;}");
        out.println(".stat-value { font-size:20px; font-weight:bold; color:#2c3e50; margin-top:5px;}");

        out.println(".library { margin-top:25px;}");
        out.println(".library-title { font-size:18px; font-weight:bold; margin-bottom:12px; color:#2c3e50; border-left:4px solid #2a5298; padding-left:10px;}");
        out.println(".shelf { display:flex; flex-wrap:wrap; gap:15px; background:white; padding:20px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);} ");

        /* UPDATED BOOK CARD WITH IMAGE SUPPORT */
        out.println(".book { display:flex; width:280px; background:#fff; border-radius:10px; overflow:hidden; box-shadow:0 3px 10px rgba(0,0,0,0.05); transition:0.3s; border:1px solid #eee;}");
        out.println(".book:hover { transform:translateY(-5px); box-shadow:0 8px 18px rgba(0,0,0,0.12);} ");
        out.println(".spine { width:8px; background:linear-gradient(180deg,#2a5298,#1e3c72); flex-shrink:0;} ");

        out.println(".book-cover { width:90px; height:120px; object-fit:cover; background:#ecf0f1; border-right:1px solid #f0f0f0; }");

        out.println(".book-content { padding:12px; flex-grow:1; display:flex; flex-direction:column; justify-content:space-between;}");
        out.println(".book-title { font-size:14px; font-weight:600; color:#2c3e50; margin-bottom:4px; line-height:1.3;}");

        /*  ADDED DESCRIPTION STYLE (Limited to 3 lines) */
        out.println(".book-description { font-size:11px; color:#666; margin-bottom:8px; display:-webkit-box; -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; line-height:1.4; }");

        out.println(".book-actions { display:flex; gap:6px; }");
        out.println(".btn-edit, .btn-delete { font-size:10px; padding:4px 8px; border-radius:4px; text-decoration:none; color:white; font-weight:bold;}");
        out.println(".btn-edit { background:#3498db; }");
        out.println(".btn-delete { background:#e74c3c; }");

        out.println(".links { margin-top: 25px; display: flex; flex-wrap: wrap; gap: 10px; }");
        out.println(".links a { display: inline-block; padding: 10px 18px; border-radius: 8px; text-decoration: none; font-size: 13px; font-weight: 600; background:#2a5298; color:white; transition:0.3s;}");
        out.println(".links a:hover { background:#1e3c72; transform:translateY(-2px); }");
        out.println("</style>");

        out.println("</head><body>");

        out.println("<div class='header'> SCHOOL LIBRARY </div>");
        out.println("<div class='container'>");

        out.println("<div class='welcome'>Welcome, " + username + " </div>");

        out.println("<div class='stats'>");
        out.println("<div class='stat-card'><div class='stat-title'>Active Users</div><div class='stat-value'><span id='activeUsers'>...</span></div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Total Books</div><div class='stat-value'>" + totalBooks + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>" + borrowedLabel + "</div><div class='stat-value'>" + displayBorrowedCount + "</div></div>");
        out.println("<div class='stat-card'><div class='stat-title'>Available</div><div class='stat-value'>" + available + "</div></div>");
        out.println("</div>");

        out.println("<div class='library'>");
        out.println("<div class='library-title'>Available Books Collection</div>");
        out.println("<div class='shelf'>");

        for (Book book : allBooks) {
            // ✅ Image logic: use URL if exists, otherwise a placeholder
            String imgUrl = (book.getImageUrl() != null && !book.getImageUrl().isEmpty())
                    ? book.getImageUrl()
                    : "https://via.placeholder.com/90x120?text=No+Cover";

            // ✅ Description logic
            String desc = (book.getDescription() != null && !book.getDescription().isEmpty())
                    ? book.getDescription()
                    : "No description available.";

            out.println("<div class='book'>");
            out.println("<div class='spine'></div>");

            // ✅ Render Image
            out.println("<img src='" + imgUrl + "' class='book-cover' alt='Book Cover'>");

            out.println("<div class='book-content'>");
            out.println("<div class='book-title'>" + book.getTitle() + "</div>");

            // ✅ ADDED DESCRIPTION DIV
            out.println("<div class='book-description'>" + desc + "</div>");

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
        out.println("<a href='borrowed'>Borrowed History</a>");

        if ("ADMIN".equals(role)) {
            out.println("<a href='add-book.jsp' style='background:#27ae60;'>Add Book</a>");
        }

        out.println("<a href='logout' style='background:#e74c3c;'> Logout</a>");
        out.println("</div>");

        out.println("</div>");

        // Active Users Script
        out.println("<script>");
        out.println("function loadUsers(){fetch('active-users').then(r=>r.text()).then(d=>document.getElementById('activeUsers').innerText=d);} ");
        out.println("setInterval(loadUsers,2000);loadUsers();");
        out.println("</script>");

        out.println("</body></html>");
    }
}