package app.pages;

import app.dao.UserDAO;
import jakarta.inject.Inject; // ✅ Required for Dependency Injection
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/delete-user")
public class DeleteUserServlet extends HttpServlet {

    // ✅ WildFly will now manage this instance and inject the DataSource
    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");

        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);

                // ✅ Use the injected userDAO instead of creating a new one
                boolean deleted = userDAO.deleteUser(id);

                if (deleted) {
                    System.out.println("✅ USER DELETED SUCCESSFULLY: ID " + id);
                } else {
                    System.err.println("❌ DELETE FAILED: User ID " + id + " not found or DB error.");
                }
            } catch (NumberFormatException e) {
                System.err.println("⚠️ INVALID ID FORMAT received: " + idStr);
            }
        }

        // Redirect back to the members list (relative to context path)
        response.sendRedirect(request.getContextPath() + "/members");
    }
}