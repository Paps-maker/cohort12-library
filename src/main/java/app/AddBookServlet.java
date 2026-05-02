package app;

import app.dao.BookDAO;
import app.model.Book;
import app.validation.BookValidator;
import app.validation.ValidatorQualifier;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ADD BOOK SERVLET
 * Manages the addition of new titles and their initial physical stock.
 */
@WebServlet("/addbook")
public class AddBookServlet extends HttpServlet {

    @Inject
    private BookDAO bookDAO;

    @Inject
    @ValidatorQualifier(ValidatorQualifier.ValidationChoice.BOOK)
    private BookValidator validator;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String error = (String) request.getAttribute("error");
        Boolean success = (Boolean) request.getAttribute("success");
        String contextPath = request.getContextPath();

        out.println("<!DOCTYPE html><html><head><title>Library | Add New Book</title>");
        out.println("<style>");
        out.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&display=swap');");
        out.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }");
        out.println(".box { background: white; padding: 40px; border-radius: 24px; width: 100%; max-width: 450px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); text-align: center; border: 1px solid #e2e8f0; }");
        out.println("h2 { color: #0f172a; margin-bottom: 24px; font-weight: 800; letter-spacing: -1px; }");
        out.println("input, textarea { width: 100%; padding: 14px; margin: 12px 0; border: 1px solid #e2e8f0; border-radius: 12px; box-sizing: border-box; font-size: 15px; font-family: inherit; transition: 0.2s; }");
        out.println("input:focus, textarea:focus { outline: none; border-color: #2563eb; ring: 2px solid #dbeafe; }");
        out.println(".btn-primary { width: 100%; padding: 14px; border: none; border-radius: 12px; background: #0f172a; color: white; font-weight: 700; cursor: pointer; margin-top: 10px; transition: 0.2s; }");
        out.println(".btn-primary:hover { background: #1e293b; transform: translateY(-1px); }");
        out.println(".success-msg { color: #065f46; font-weight: 700; margin-bottom: 20px; font-size: 16px; background: #dcfce7; padding: 15px; border-radius: 12px; border: 1px solid #bbf7d0; }");
        out.println(".error { color: #991b1b; background: #fee2e2; padding: 12px; border-radius: 10px; margin-bottom: 15px; border: 1px solid #fecaca; font-size: 14px; font-weight: 600; }");
        out.println(".label-left { text-align: left; display: block; font-weight: 700; color: #475569; font-size: 13px; margin-top: 10px; text-transform: uppercase; letter-spacing: 0.5px; }");
        out.println(".btn-secondary { display: inline-block; text-decoration: none; background: #f1f5f9; color: #475569; padding: 12px 24px; border-radius: 12px; font-weight: 700; font-size: 14px; margin-top: 20px; }");
        out.println("</style></head><body>");

        out.println("<div class='box'>");

        if (success != null && success) {
            out.println("<div class='success-msg'>✅ Book & Inventory Initialized!</div>");
            out.println("<p style='color:#64748b;'>The title has been added and stock is now available for borrowing.</p>");
            out.println("<a href='books' class='btn-secondary'>View Updated Catalog</a>");
        } else {
            out.println("<h2>Add New Book</h2>");
            if (error != null) out.println("<div class='error'>⚠️ " + error + "</div>");

            out.println("<form action='" + contextPath + "/addbook' method='post'>");
            out.println("<label class='label-left'>Book Information</label>");
            out.println("<input type='text' name='title' placeholder='Book Title' required>");
            out.println("<input type='text' name='imageUrl' placeholder='Cover Image URL'>");
            out.println("<textarea name='description' placeholder='Short Synopsis...' rows='3'></textarea>");

            out.println("<label class='label-left'>Initial Physical Stock (Copies)</label>");
            out.println("<input type='number' name='copies' value='1' min='1' max='50' required>");

            out.println("<button type='submit' class='btn-primary'>Save to Catalog</button>");
            out.println("<a href='books' style='display:block; margin-top:15px; font-size:13px; color:#64748b; text-decoration:none;'>Cancel</a>");
            out.println("</form>");
        }

        out.println("</div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String title = request.getParameter("title");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");
        String copiesStr = request.getParameter("copies");

        int copies = 1;
        try {
            copies = (copiesStr != null) ? Integer.parseInt(copiesStr) : 1;
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid number of copies.");
            doGet(request, response);
            return;
        }

        // Validate the book data
        String validationError = validator.validate(title, imageUrl, description);

        if (validationError == null) {
            Book newBook = new Book(title, imageUrl, description);

            /*
             * ✅ CALLS UPDATED DAO:
             * This method now handles setting both total_quantity and available_copies
             * to the 'copies' value, ensuring your dashboard starts at 0 borrowed.
             */
            boolean added = bookDAO.addBookWithCopies(newBook, copies);

            if (added) {
                request.setAttribute("success", true);
            } else {
                request.setAttribute("error", "Synopsis should no more than 500");
            }
        } else {
            request.setAttribute("error", validationError);
        }

        doGet(request, response);
    }
}