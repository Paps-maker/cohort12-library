package app;

import app.dao.BookDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/edit-book")
public class EditBookServlet extends HttpServlet {

    BookDAO dao = new BookDAO();

    // =========================
    // SHOW EDIT FORM
    // =========================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // 🔐 SECURITY
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("books");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        Book book = dao.getBookById(id);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Edit Book</title>");

        // ✅ PROFESSIONAL CSS
        out.println("<style>");
        out.println("body { font-family:'Segoe UI'; background:#f4f6f8; display:flex; justify-content:center; align-items:center; height:100vh; }");

        out.println(".card { background:white; padding:30px; border-radius:12px; width:400px; box-shadow:0 4px 12px rgba(0,0,0,0.1);} ");

        out.println("h2 { margin-bottom:20px; color:#2c3e50; text-align:center; }");

        out.println("input { width:100%; padding:10px; margin:10px 0; border-radius:6px; border:1px solid #ccc; }");

        out.println(".btn { width:100%; padding:10px; border:none; border-radius:6px; font-weight:bold; cursor:pointer; }");

        out.println(".update-btn { background:#27ae60; color:white; }");
        out.println(".update-btn:hover { background:#1e8449; }");

        out.println(".back-btn { display:block; text-align:center; margin-top:10px; text-decoration:none; color:#2a5298; font-weight:bold; }");

        out.println("</style>");

        out.println("</head><body>");

        out.println("<div class='card'>");
        out.println("<h2>Edit Book</h2>");

        out.println("<form method='post'>");

        out.println("<input type='hidden' name='id' value='" + book.getId() + "'/>");

        out.println("<label>Book Title</label>");
        out.println("<input name='title' value='" + book.getTitle() + "' required/>");

        out.println("<button class='btn update-btn'>Update Book</button>");

        out.println("</form>");

        out.println("<a class='back-btn' href='books'>⬅ Back to Library</a>");

        out.println("</div>");

        out.println("</body></html>");
    }

    // =========================
    // UPDATE BOOK
    // =========================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        // 🔐 SECURITY
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.sendRedirect("books");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        String title = request.getParameter("title");

        BookDAO dao = new BookDAO();
        dao.updateBook(new Book(id, title));

        response.sendRedirect("books");
    }
}