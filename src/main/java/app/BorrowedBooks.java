package app;

import app.dao.BorrowDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/borrowed")
public class BorrowedBooks extends HttpServlet {

    BorrowDAO dao = new BorrowDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        List<String> borrowedList;

        // ✅ ADMIN → SEE ALL
        if ("ADMIN".equals(role)) {
            borrowedList = dao.getAllBorrowed();
        } else {
            // ✅ USER → SEE ONLY OWN
            borrowedList = dao.getUserBorrowed(username);
        }

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head><title>Borrowed Books</title>");

        writer.println("<style>");
        writer.println("body { font-family: Arial; background:#f4f6f8; padding:40px; }");
        writer.println(".container { max-width:600px; margin:auto; }");
        writer.println(".card { background:white; padding:20px; border-radius:10px; box-shadow:0 3px 10px rgba(0,0,0,0.1);} ");
        writer.println("li { margin:10px 0; }");
        writer.println("a { display:block; margin-top:10px; color:#3498db; }");
        writer.println("</style>");

        writer.println("</head><body>");

        writer.println("<div class='container'>");
        writer.println("<div class='card'>");

        if ("ADMIN".equals(role)) {
            writer.println("<h2> All Borrowed Books</h2>");
        } else {
            writer.println("<h2> My Borrowed Books</h2>");
        }

        if (borrowedList == null || borrowedList.isEmpty()) {
            writer.println("<p>No borrowed books found.</p>");
        } else {
            writer.println("<ul>");
            for (String record : borrowedList) {
                writer.println("<li>" + record + "</li>");
            }
            writer.println("</ul>");
        }

        writer.println("</div>");

        writer.println("<a href='borrow'>Borrow a Book</a>");
        writer.println("<a href='books'>Back to Books</a>");

        writer.println("</div>");
        writer.println("</body></html>");
    }
}