package app;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ADMIN ANALYTICS SERVLET
 * Orchestrates the data flow for the Administrative Dashboard.
 * Updated to utilize LibraryService for centralized business logic.
 */
@WebServlet("/admin/analytics")
public class AdminAnalyticsServlet extends HttpServlet {

    @Inject
    private LibraryService libraryService; // ✅ Updated to use Service Layer

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security check for Admin role
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (role == null || !"ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        // 2. Pass KPI and aggregate data to the request scope via LibraryService
        // These methods map to the corrected SQL logic in your AnalyticsDAO
        request.setAttribute("totalRevenue", libraryService.getSystemTotalRevenue());
        request.setAttribute("unpaidCount", libraryService.getUnpaidFinesCount());
        request.setAttribute("totalBooks", libraryService.getLibraryTotalInventory());
        request.setAttribute("overdueCount", libraryService.getSystemOverdueCount());

        // 3. Pass Trend and Distribution data for Charts
        request.setAttribute("topBooks", libraryService.getTopBorrowedBooks());
        request.setAttribute("activeUsers", libraryService.getMostActiveMembers());
        request.setAttribute("debtTrend", libraryService.getWeeklyDebtTrend());

        // 4. Forward to the Dashboard JSP
        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }
}