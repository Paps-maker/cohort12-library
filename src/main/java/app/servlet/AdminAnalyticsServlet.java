package app.servlet;

import app.services.AnalyticsService; // ✅ Updated import
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
 * Now utilizes AnalyticsService for specific reporting metrics.
 */
@WebServlet("/admin/analytics")
public class AdminAnalyticsServlet extends HttpServlet {

    @Inject
    private AnalyticsService analyticsService; // ✅ Using the specific AnalyticsService

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security check for Admin role
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (!"ADMIN".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin role required.");
            return;
        }

        try {
            // 2. Fetch KPI values via Service
            request.setAttribute("totalRevenue", analyticsService.getSystemTotalRevenue());
            request.setAttribute("unpaidCount", analyticsService.getUnpaidFinesCount());
            request.setAttribute("totalBooks", analyticsService.getLibraryTotalInventory());
            request.setAttribute("overdueCount", analyticsService.getSystemOverdueCount());

            // 3. Fetch Trend and Distribution data (Maps) for Charts
            request.setAttribute("topBooks", analyticsService.getTopBorrowedBooks());
            request.setAttribute("activeUsers", analyticsService.getMostActiveMembers());
            request.setAttribute("debtTrend", analyticsService.getWeeklyDebtTrend());

            // 4. Forward to the Dashboard JSP
            request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating analytics report.");
        }
    }
}