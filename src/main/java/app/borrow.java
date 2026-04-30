package app;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import app.model.Book;
import app.validation.ValidatorQualifier;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/borrow")
@LoginRequired
public class borrow extends HttpServlet {

    /**
     * ✅ CDI MANAGED INJECTION
     * Provides the shared LibraryService facade.
     */
    @Inject
    private LibraryService libraryService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute("username");

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        List<Book> books = libraryService.getAllBooks();
        double unpaidFines = libraryService.getUnpaidFines(username);

        writer.println("<!DOCTYPE html><html><head><title>Library | Borrow</title>");
        writer.println("<style>");
        writer.println("body { font-family: 'Inter', system-ui, sans-serif; background: #f0f2f5; margin: 0; padding: 40px; color: #1a1f36; }");
        writer.println(".container { max-width: 500px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); }");
        writer.println("header { background: #6366f1; color: white; padding: 30px; text-align: center; }");
        writer.println("header h1 { margin: 0; font-size: 24px; letter-spacing: -0.5px; }");
        writer.println("section { padding: 30px; }");
        writer.println("h3 { margin-top: 0; color: #4f46e5; }");
        writer.println(".fine-msg { background: #fff1f2; color: #e11d48; padding: 15px; border-radius: 8px; border-left: 4px solid #e11d48; font-weight: 600; margin-bottom: 20px; }");
        writer.println("select, input[type='number'] { width: 100%; padding: 12px; border: 2px solid #e2e8f0; border-radius: 8px; font-size: 16px; margin: 10px 0; transition: border-color 0.2s; box-sizing: border-box; }");
        writer.println("select:focus, input[type='number']:focus { outline: none; border-color: #6366f1; }");
        writer.println("label { font-size: 14px; color: #64748b; font-weight: 600; }");
        writer.println("button { background: #6366f1; color: white; border: none; width: 100%; padding: 14px; border-radius: 8px; font-weight: 700; cursor: pointer; transition: 0.3s; margin-top: 10px; }");
        writer.println("button:hover { background: #4f46e5; transform: translateY(-1px); }");
        writer.println(".nav-links { margin-top: 25px; display: flex; justify-content: center; gap: 20px; }");
        writer.println(".nav-links a { color: #64748b; text-decoration: none; font-size: 14px; font-weight: 500; padding: 8px 12px; border-radius: 6px; transition: 0.2s; }");
        writer.println(".nav-links a:hover { color: #6366f1; background: #eef2ff; }");
        writer.println("</style></head><body>");

        writer.println("<div class='container'>");
        writer.println("<header><h1>Library Catalog</h1></header>");
        writer.println("<section>");
        writer.println("<h3>Welcome, " + username + "</h3>");

        if (unpaidFines > 0) {
            writer.println("<div class='fine-msg'> Unpaid Fine: KSH " + String.format("%.2f", unpaidFines) + "<br><small>Borrowing is disabled until settled.</small></div>");
        }

        writer.println("<form method='post' action='./borrow'>");

        writer.println("<label>Select Book</label>");
        writer.println("<select name='bookId' required " + (unpaidFines > 0 ? "disabled" : "") + ">");
        writer.println("<option value='' disabled selected>-- Search Collection --</option>");

        for (Book book : books) {
            if (libraryService.isBookAvailable(book.getId())) {
                writer.println("<option value='" + book.getId() + "'>" + book.getTitle() + "</option>");
            }
        }
        writer.println("</select>");

        writer.println("<label>Borrow Duration (1-10 Days)</label>");
        writer.println("<input type='number' name='days' min='1' max='10' value='7' required " + (unpaidFines > 0 ? "disabled" : "") + ">");

        writer.println("<button type='submit' " + (unpaidFines > 0 ? "style='background:#cbd5e1; cursor:not-allowed;' disabled" : "") + ">Confirm Checkout</button>");
        writer.println("</form>");
        writer.println("</section>");
        writer.println("</div>");

        writer.println("<div class='nav-links'>");
        writer.println("<a href='borrowed'> My Borrow History</a>");
        writer.println("<a href='books'> View All Books</a>");
        writer.println("</div>");

        writer.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        String bookIdParam = req.getParameter("bookId");
        String daysParam = req.getParameter("days");

        // ✅ ONE-SHOT TRANSACTION
        // attemptBorrow handles validation AND the database insert in a single call to the EJB.
        String result = libraryService.attemptBorrow(username, role, bookIdParam, daysParam);

        if (result == null) {
            // Success (EJB returned null)
            showResponsePage(resp, "Success", "Book checked out! Return within " + daysParam + " days to avoid late fees.", true);
        } else {
            // Failure (EJB returned a validation error message)
            showResponsePage(resp, "Checkout Blocked", result, false);
        }
    }

    private void showResponsePage(HttpServletResponse resp, String title, String message, boolean isSuccess) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        String color = isSuccess ? "#10b981" : "#ef4444";

        writer.println("<html><head><style>");
        writer.println("body{font-family:'Inter', sans-serif; display:flex; justify-content:center; align-items:center; height:100vh; background:#f0f2f5; margin:0;}");
        writer.println(".card{background:white; padding:40px; border-radius:12px; text-align:center; box-shadow:0 10px 25px rgba(0,0,0,0.05); max-width:400px; border-top: 5px solid " + color + ";}");
        writer.println("h2{color:" + color + "; margin-top:0;}");
        writer.println("p{color:#64748b; line-height:1.6;}");
        writer.println(".btn{display:inline-block; margin-top:20px; padding:12px 24px; background:#6366f1; color:white; text-decoration:none; border-radius:8px; font-weight:600;}");
        writer.println("</style></head><body>");
        writer.println("<div class='card'><h2>" + title + "</h2><p>" + message + "</p><a href='borrow' class='btn'>Continue</a></div>");
        writer.println("</body></html>");
    }
}