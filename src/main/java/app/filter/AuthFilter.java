package app.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getServletPath();
        String fullUri = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Safely normalize path defaults to prevent null pointer comparisons
        if (path == null) {
            path = "";
        }

        // 🌟 REFACTORED: API requests now completely bypass web session state checking.
        // They pass through seamlessly here to be validated statelessly by your RestSecurityFilter.
        boolean isApiRequest = path.startsWith("/api") || fullUri.startsWith(contextPath + "/api");

        // Detect incoming SOAP Web Service infrastructure traffic routes
        boolean isSoapRequest = path.startsWith("/BookWebService") || fullUri.startsWith(contextPath + "/BookWebService");

        // Robust check for incoming WebSocket traffic routes across path configurations
        boolean isWebSocket = path.startsWith("/websocket") ||
                path.contains("/chat") ||
                fullUri.contains("/websocket") ||
                fullUri.contains("/chat");

        // Clean public whitelist mappings matching automated controllers
        boolean isPublicPage = path.equals("/") ||
                path.equals("/login") ||
                path.equals("/authenticate") ||
                path.equals("/index.jsp") ||
                path.equals("/register.jsp") ||
                path.equals("/account/register") ||
                path.equals("/account/contact") ||
                path.contains("contact.jsp") ||
                path.equals("/BookWebService");

        // Allow static assets to parse seamlessly
        boolean isStaticResource = path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");

        // Whitelist the /views/ directory for internal forward requests
        boolean isInternalView = path.startsWith("/views/");

        boolean isLoggedIn = (session != null && session.getAttribute("username") != null);

        // Security Enforcement Pipeline Flow
        if (isLoggedIn || isPublicPage || isStaticResource || isApiRequest || isSoapRequest || isWebSocket || isInternalView) {
            chain.doFilter(request, response);
        } else {
            System.out.println("AUTOMATION SECURITY: Blocked unauthorized web browser route attempt to: " + path + " [URI: " + fullUri + "]");
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}