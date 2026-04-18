package app;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/borrow")
@LoginRequired

public class borrow extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login");
            return;
        }

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

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
        writer.println("</head>");

        writer.println("<body>");

        writer.println("<header>");
        writer.println("<h1>Borrow a Book</h1>");
        writer.println("</header>");

        writer.println("<section>");
        writer.println("<h2>Select a Book to Borrow</h2>");
        writer.println("<form method='post' action='./borrow'>");

        writer.println("<label>Member Name:</label>");
        writer.println("<input type='text' name='memberName' placeholder='Enter your name' required />");

        writer.println("<label>Book to Borrow:</label>");
        writer.println("<select name='bookName' required>");

        for (String book : BookStore.books) {
            writer.println("<option value='" + book + "'>" + book + "</option>");
        }

        writer.println("</select>");
        writer.println("<button type='submit'>Borrow Book</button>");
        writer.println("</form>");
        writer.println("</section>");

        writer.println("<section>");
        writer.println("<a href='./borrowed'>View Borrowed Books</a><br>");
        writer.println("<a href='./books'>&larr; Back to Book List</a>");
        writer.println("</section>");

        writer.println("</body>");
        writer.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        //  SESSION CHECK
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login");
            return;
        }

        String role = (String) session.getAttribute("role");

        //  ADMIN NOT ALLOWED TO BORROW (CUSTOM POPUP PAGE)
        if ("ADMIN".equals(role)) {

            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();

            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Access Denied</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial; background:#f4f6f8; display:flex; justify-content:center; align-items:center; height:100vh; }");
            writer.println(".box { background:white; padding:30px; border-radius:10px; text-align:center; width:320px; box-shadow:0 0 15px rgba(0,0,0,0.2);} ");
            writer.println(".box h2 { color:red; }");
            writer.println(".btn { margin-top:15px; display:inline-block; padding:10px 15px; background:#3498db; color:white; text-decoration:none; border-radius:5px; }");
            writer.println("</style>");
            writer.println("</head>");

            writer.println("<body>");

            writer.println("<div class='box'>");
            writer.println("<h2>Access Denied</h2>");
            writer.println("<p>Only normal users are allowed to borrow books.</p>");
            writer.println("<a class='btn' href='books'>Go Back</a>");
            writer.println("</div>");

            writer.println("</body>");
            writer.println("</html>");

            return;
        }

        String memberName = req.getParameter("memberName");
        String bookName = req.getParameter("bookName");

        ServletContext context = getServletContext();

        List<String> borrowedList =
                (List<String>) context.getAttribute("borrowedBooks");

        if (borrowedList == null) {
            borrowedList = new ArrayList<>();
        }

        borrowedList.add(memberName + " borrowed \"" + bookName + "\"");

        context.setAttribute("borrowedBooks", borrowedList);

        // ================= SUCCESS PAGE =================

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Borrow Confirmation</title>");
        writer.println("<style>");
        writer.println("body { font-family: Arial; margin: 40px; background-color: #f4f6f8; }");
        writer.println("header { background-color: #2c3e50; color: white; padding: 15px; }");
        writer.println("section { margin-top: 20px; padding: 20px; background: white; border-radius: 5px; max-width: 400px; }");
        writer.println("a { display: inline-block; margin-top: 10px; color: #3498db; }");
        writer.println("</style>");
        writer.println("</head>");

        writer.println("<body>");

        writer.println("<header>");
        writer.println("<h1>Borrow Confirmation</h1>");
        writer.println("</header>");

        writer.println("<section>");
        writer.println("<h2>Book Borrowed Successfully!</h2>");
        writer.println("<p>Member Name: <strong>" + memberName + "</strong></p>");
        writer.println("<p>Book Name: <strong>" + bookName + "</strong></p>");
        writer.println("</section>");

        writer.println("<section>");
        writer.println("<a href='./borrow'>&larr; Borrow Another Book</a><br>");
        writer.println("<a href='./borrowed'>View Borrowed Books</a><br>");
        writer.println("<a href='./books'>&larr; Back to Book List</a>");
        writer.println("</section>");

        writer.println("</body>");
        writer.println("</html>");
    }
}