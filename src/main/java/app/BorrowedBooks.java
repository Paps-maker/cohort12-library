package app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
@WebServlet("/borrowed")
@AdminOnly
public class BorrowedBooks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        // ✅ GLOBAL DATA
        ServletContext context = getServletContext();

        List<String> borrowedList =
                (List<String>) context.getAttribute("borrowedBooks");

        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Borrowed Books</title>");
        writer.println("<style>");
        writer.println("body { font-family: Arial; background:#f4f6f8; padding:40px; }");
        writer.println(".container { max-width:600px; margin:auto; }");
        writer.println(".card { background:white; padding:20px; border-radius:10px; }");
        writer.println("li { margin:10px 0; }");
        writer.println("a { display:block; margin-top:10px; color:#3498db; }");
        writer.println("</style>");
        writer.println("</head>");

        writer.println("<body>");
        writer.println("<div class='container'>");
        writer.println("<div class='card'>");

        writer.println("<h2> All Borrowed Books</h2>");

        if (borrowedList == null || borrowedList.isEmpty()) {
            writer.println("<p>No books have been borrowed yet.</p>");
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
        writer.println("</body>");
        writer.println("</html>");
    }
}