package app;

import app.dao.BookDAO;
import app.dao.BorrowDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/borrow")
@LoginRequired
public class borrow extends HttpServlet {

    BorrowDAO borrowDAO = new BorrowDAO();
    BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login");
            return;
        }

        String username = (String) session.getAttribute("username");

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        // GET BOOKS FROM DATABASE (NO BOOKSTORE ANYMORE)
        List<Book> books = bookDAO.getAllBooks();

        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Borrow Books</title>");

        writer.println("<style>");
        writer.println("body { font-family: Arial; margin: 40px; background-color: #f4f6f8; }");
        writer.println("header { background-color: #2c3e50; color: white; padding: 15px; }");
        writer.println("section { margin: 20px; padding: 20px; background: white; border-radius: 5px; max-width: 400px; }");
        writer.println("input, select { width: 100%; padding: 8px; margin: 10px 0; }");
        writer.println("button { padding: 10px; background-color: #3498db; color: white; border: none; width: 100%; }");
        writer.println("a { display: inline-block; margin-top: 10px; color: #3498db; }");
        writer.println("</style>");

        writer.println("</head><body>");

        writer.println("<header><h1>Borrow a Book</h1></header>");

        writer.println("<section>");
        writer.println("<h2>Welcome " + username + "</h2>");

        writer.println("<form method='post' action='./borrow'>");

        writer.println("<input type='hidden' name='username' value='" + username + "'/>");

        writer.println("<label>Select Book:</label>");
        writer.println("<select name='bookId' required>");

        for (Book book : books) {
            writer.println("<option value='" + book.getId() + "'>" +
                    book.getTitle() + "</option>");
        }

        writer.println("</select>");

        writer.println("<button type='submit'>Borrow Book</button>");
        writer.println("</form>");
        writer.println("</section>");

        writer.println("<section>");
        writer.println("<a href='./borrowed'>View Borrowed Books</a><br>");
        writer.println("<a href='./books'>&larr; Back to Book List</a>");
        writer.println("</section>");

        writer.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login");
            return;
        }

        String role = (String) session.getAttribute("role");
        String username = (String) session.getAttribute("username");

        // ❌ ADMIN NOT ALLOWED
        if ("ADMIN".equals(role)) {
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();

            writer.println("<html><head><title>Access Denied</title>");
            writer.println("<style>");
            writer.println("body{font-family:Arial;display:flex;justify-content:center;align-items:center;height:100vh;background:#f4f6f8;}");
            writer.println(".box{background:white;padding:30px;border-radius:10px;text-align:center;box-shadow:0 0 15px rgba(0,0,0,0.2)}");
            writer.println("a{display:inline-block;margin-top:10px;padding:10px 15px;background:#3498db;color:white;text-decoration:none;border-radius:5px}");
            writer.println("</style></head><body>");

            writer.println("<div class='box'>");
            writer.println("<h2 style='color:red;'>Access Denied</h2>");
            writer.println("<p>Only users can borrow books.</p>");
            writer.println("<a href='books'>Go Back</a>");
            writer.println("</div>");

            writer.println("</body></html>");
            return;
        }

        int bookId = Integer.parseInt(req.getParameter("bookId"));

        // DATABASE BORROW
        boolean success = borrowDAO.borrowBook(username, bookId);

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        writer.println("<html><head><title>Borrow Confirmation</title>");
        writer.println("<style>");
        writer.println("body{font-family:Arial;margin:40px;background:#f4f6f8;}");
        writer.println("section{background:white;padding:20px;border-radius:6px;max-width:400px;}");
        writer.println("a{display:inline-block;margin-top:10px;color:#3498db;}");
        writer.println("</style></head><body>");

        writer.println("<section>");

        if (success) {
            writer.println("<h2 style='color:green;'>Book Borrowed Successfully!</h2>");
        } else {
            writer.println("<h2 style='color:red;'>Borrow Failed</h2>");
        }

        writer.println("</section>");

        writer.println("<section>");
        writer.println("<a href='borrow'>Borrow Another</a><br>");
        writer.println("<a href='books'>Back to Books</a>");
        writer.println("</section>");

        writer.println("</body></html>");
    }
}