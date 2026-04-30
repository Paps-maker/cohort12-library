package app;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * ✅ ACTION SERVLET: Handles the processing of fine payments.
 */
@WebServlet("/pay-fine")
public class FinePaymentServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Identify the Actor
        HttpSession session = req.getSession(false);

        // Security check
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role"); // Capture role

        // 2. Capture the Input
        String fineIdParam = req.getParameter("fineId");

        // 3. Business Logic Execution
        boolean success = false;

        if (fineIdParam != null && !fineIdParam.isEmpty()) {
            // Note: LibraryService.processFinePayment already validates if the fine belongs
            // to the user OR if the actor is an ADMIN in its internal logic.
            success = libraryService.processFinePayment(username, fineIdParam);
        }

        // 4. Feedback Loop & Smart Redirection
        if (success) {
            // If an Admin pays, they stay on their global view; Member stays on personal view
            resp.sendRedirect(req.getContextPath() + "/fines?status=success");
        } else {
            resp.sendRedirect(req.getContextPath() + "/fines?error=payment_failed");
        }
    }
}