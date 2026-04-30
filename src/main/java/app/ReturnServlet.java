package app;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ✅ ACTION SERVLET: Processes book returns.
 * Now fully managed by WildFly CDI.
 */
@WebServlet("/return")
public class ReturnServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Identify Actor
        HttpSession session = req.getSession(false);

        // Security check
        if (session == null || session.getAttribute("role") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");

        // 2. Capture and Clean Input
        String borrowIdStr = req.getParameter("borrowId");

        if (borrowIdStr != null) {
            // ✅ CRITICAL FIX: Strip "ID:", spaces, and non-numeric characters
            // This ensures "ID: 5 " becomes just "5"
            borrowIdStr = borrowIdStr.replaceAll("[^0-9]", "").trim();
        }

        // 3. Execution
        // If borrowIdStr is empty after cleaning, set a custom error
        String error;
        if (borrowIdStr == null || borrowIdStr.isEmpty()) {
            error = "Invalid ID format received.";
        } else {
            error = libraryService.processReturnRequest(role, borrowIdStr);
        }

        // 4. Navigation
        String contextPath = req.getContextPath();
        if (error == null) {
            resp.sendRedirect(contextPath + "/borrowed?status=returned");
        } else {
            String encodedError = URLEncoder.encode(error, StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/borrowed?error=" + encodedError);
        }
    }
}