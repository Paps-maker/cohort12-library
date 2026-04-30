package app;

import app.dao.BookDAO;
import app.model.Book;
import app.validation.BookValidator;
import app.validation.ValidatorQualifier; //  Consistently using  custom qualifier
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/addbook")
public class AddBookServlet extends HttpServlet {

    @Inject
    private BookDAO bookDAO;

    /**
     *  STRATEGY-BASED INJECTION
     * The qualifier ensures the container provides the specific BookValidator
     * implementation assigned to the 'BOOK' choice.
     */
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
        out.println("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }");
        out.println(".box { background: white; padding: 40px; border-radius: 16px; width: 100%; max-width: 450px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); text-align: center; }");
        out.println("h2 { color: #1e293b; margin-bottom: 24px; font-weight: 700; }");
        out.println("input, textarea { width: 100%; padding: 14px; margin: 12px 0; border: 1px solid #e2e8f0; border-radius: 10px; box-sizing: border-box; font-size: 15px; transition: border 0.3s; }");
        out.println("input:focus, textarea:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }");
        out.println(".btn-primary { width: 100%; padding: 14px; border: none; border-radius: 10px; background: #1e293b; color: white; font-weight: 600; cursor: pointer; transition: 0.2s; margin-top: 10px; }");
        out.println(".btn-primary:hover { background: #334155; transform: translateY(-1px); }");
        out.println(".success-msg { color: #059669; font-weight: bold; margin-bottom: 20px; font-size: 18px; background: #ecfdf5; padding: 15px; border-radius: 10px; }");
        out.println(".options { display: flex; gap: 12px; justify-content: center; margin-top: 25px; }");
        out.println(".btn-nav { padding: 12px 20px; text-decoration: none; border-radius: 10px; font-size: 14px; color: white; font-weight: 600; transition: 0.2s; }");
        out.println(".btn-list { background: #3b82f6; } .btn-list:hover { background: #2563eb; }");
        out.println(".btn-add { background: #64748b; } .btn-add:hover { background: #475569; }");
        out.println(".error { color: #dc2626; background: #fef2f2; padding: 12px; border-radius: 8px; margin-bottom: 15px; font-size: 14px; border: 1px solid #fee2e2; }");
        out.println("</style></head><body>");

        out.println("<div class='box'>");

        if (success != null && success) {
            out.println("<div class='success-msg'>✅ Book Added Successfully!</div>");
            out.println("<p style='color: #64748b;'>The catalog has been updated.</p>");
            out.println("<div class='options'>");
            out.println("<a href='" + contextPath + "/books' class='btn-nav btn-list'>View Catalog</a>");
            out.println("<a href='" + contextPath + "/addbook' class='btn-nav btn-add'>Add Another</a>");
            out.println("</div>");
        } else {
            out.println("<h2>Add New Book</h2>");
            if (error != null) {
                out.println("<div class='error'>⚠️ " + error + "</div>");
            }

            out.println("<form action='" + contextPath + "/addbook' method='post'>");
            out.println("<input type='text' name='title' placeholder='Book Title' required>");
            out.println("<input type='text' name='imageUrl' placeholder='Image URL (http...)'>");
            out.println("<textarea name='description' placeholder='Short description of the book...' rows='4'></textarea>");
            out.println("<button type='submit' class='btn-primary'>Save to Catalog</button>");
            out.println("</form>");
            out.println("<div style='margin-top:20px;'><a href='books' style='color:#94a3b8; text-decoration:none; font-size: 14px;'>Cancel and Return</a></div>");
        }

        out.println("</div>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Capture parameters
        String title = request.getParameter("title");
        String imageUrl = request.getParameter("imageUrl");
        String description = request.getParameter("description");

        // 2. Business Validation (Using injected validator)
        String validationError = validator.validate(title, imageUrl, description);

        if (validationError == null) {
            Book newBook = new Book(title, imageUrl, description);
            boolean added = bookDAO.addBook(newBook);

            if (added) {
                request.setAttribute("success", true);
            } else {
                request.setAttribute("error", "Database Error: Could not save to storage.");
            }
        } else {
            request.setAttribute("error", validationError);
        }

        // Forward back to doGet to render success or error message
        doGet(request, response);
    }
}