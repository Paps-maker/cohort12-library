package app;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Handle POST requests to delete a fine record.
 * Accessible only by ADMIN users.
 */
@WebServlet("/delete-fine")
public class DeleteFineServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        // 1. Security check: Only Admins should be allowed to delete records
        if (!"ADMIN".equals(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access.");
            return;
        }

        // 2. Retrieve the fineId from the hidden form field
        String fineIdParam = req.getParameter("fineId");

        if (fineIdParam != null && !fineIdParam.isEmpty()) {
            try {
                int fineId = Integer.parseInt(fineIdParam);

                // 3. ACTUALLY EXECUTE THE DELETE
                boolean deleted = libraryService.deleteFineRecord(fineId);

                // 4. Redirect with a status message for user feedback
                if (deleted) {
                    resp.sendRedirect(req.getContextPath() + "/fines?success=deleted");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/fines?error=db_error");
                }

            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/fines?error=invalid_format");
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/fines?error=missing_id");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Prevent users from accessing this URL directly via the browser address bar
        resp.sendRedirect(req.getContextPath() + "/fines");
    }
}