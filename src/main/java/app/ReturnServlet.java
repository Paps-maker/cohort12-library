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
 * Fully synchronized with LibraryService to handle inventory restoration and fine calculations.
 */
@WebServlet("/return")
public class ReturnServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Identify Actor & Security Check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String role = (String) session.getAttribute("role");

        // 2. Capture and Clean Input
        String borrowIdStr = req.getParameter("borrowId");

        // ✅ Robust Input Cleaning: extracts digits from strings like "Borrow ID: 102"
        if (borrowIdStr != null) {
            borrowIdStr = borrowIdStr.replaceAll("[^0-9]", "").trim();
        }

        // 3. Execution Logic via Service Layer
        String resultMessage;
        if (borrowIdStr == null || borrowIdStr.isEmpty()) {
            resultMessage = "Invalid transaction ID format.";
        } else {
            /*
             * ✅ INVENTORY & FINE LOGIC:
             * libraryService.processReturnRequest (via BorrowingBean) now:
             * 1. Validates the existence of the borrow record.
             * 2. Calculates overdue fines based on the return date.
             * 3. Marks the borrow record as 'RETURNED'.
             * 4. CRITICAL: Increments 'available_copies' in the book table.
             */
            resultMessage = libraryService.processReturnRequest(role, borrowIdStr);
        }

        // 4. Navigation & Feedback
        String contextPath = req.getContextPath();

        // If the service returns null, it indicates success.
        // Otherwise, it returns a specific error/instruction string.
        if (resultMessage == null) {
            // Success: Book returned and inventory restored
            resp.sendRedirect(contextPath + "/borrowed?status=returned");
        } else if (resultMessage.contains("Success") || resultMessage.toLowerCase().contains("returned")) {
            // Success with a message (e.g., "Returned with KSH 50 fine")
            String encodedMsg = URLEncoder.encode(resultMessage, StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/borrowed?message=" + encodedMsg);
        } else {
            // Error: Pass specific failure message (e.g., "Transaction not found")
            String encodedError = URLEncoder.encode(resultMessage, StandardCharsets.UTF_8);
            resp.sendRedirect(contextPath + "/borrowed?error=" + encodedError);
        }
    }

    /**
     * Redirect GET requests to the borrowed list to prevent manual URL manipulation
     * from triggering a return without a POST body.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/borrowed");
    }
}