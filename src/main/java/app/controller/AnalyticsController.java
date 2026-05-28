package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ModelAndView;
import app.ejbs.FineBean;
import app.ejbs.BorrowingBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import app.framework.Controller;

@Controller
public class AnalyticsController {

    @Inject
    private FineBean fineBean;

    @Inject
    private BorrowingBean borrowingBean;

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    @ActionGetMethod("/admin/analytics")
    public ModelAndView showAnalytics(HttpServletRequest req) {
        if (!isAdmin(req)) {
            return new ModelAndView("redirect:/login");
        }

        // Return the ModelAndView with aggregated data
        return new ModelAndView("/analytics_dashboard.jsp")
                .addObject("sevenDayTrend", fineBean.getLastSevenDaysDebt())
                .addObject("topBorrowed", borrowingBean.getTopBorrowedTitles(5))
                .addObject("userStats", borrowingBean.getUserActivityDistribution());
    }
}