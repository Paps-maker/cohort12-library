package app;

import app.dao.BookDAO;
import jakarta.inject.Inject; //  Added for Injection
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/delete-book")
public class DeleteBookServlet extends HttpServlet {

    // ✅ WildFly manages this instance so the DataSource is never null
    @Inject
    private BookDAO bookDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        // ✅ SECURITY: Restrict deletion to ADMIN only
        if (!"ADMIN".equals(role)) {
            System.out.println(" UNAUTHORIZED DELETE ATTEMPT: " + (session != null ? session.getAttribute("username") : "Guest"));
            response.sendRedirect(request.getContextPath() + "/books");
            return;
        }

        try {
            String idParam = request.getParameter("id");
            if (idParam != null) {
                int id = Integer.parseInt(idParam);

                //  Use the injected bookDAO
                boolean deleted = bookDAO.deleteBook(id);

                if (deleted) {
                    System.out.println("✅ Book deleted: ID " + id);
                } else {
                    System.err.println("⚠ Delete failed for ID " + id + " (Not found in DB)");
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Invalid ID format in delete request");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during book deletion");
            e.printStackTrace();
        }

        // Send back to books list with the context path
        response.sendRedirect(request.getContextPath() + "/books");
    }
}