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

        // Get the path relative to the context root (e.g., "/login" or "/authenticate")
        String path = req.getServletPath();

        // ✅ 1. Updated Comprehensive White-list
        // Added "/authenticate" so the AuthController can actually log you in!
        boolean isPublicPage = path.equals("/login") ||
                path.equals("/authenticate") || // CRITICAL: Allows the POST request
                path.equals("/") ||
                path.equals("/index.jsp") ||
                path.equals("/register.jsp") ||
                path.equals("/registerProcess") ||
                path.equals("/contact.jsp") ||
                path.equals("/contactdisplay.jsp") ||
                path.equals("/submit-contact") ||
                path.equals("/addbook");

        // Allow static assets
        boolean isStaticResource = path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");

        // 2. Define Authentication Status
        boolean isLoggedIn = (session != null && session.getAttribute("username") != null);

        // 3. Security Enforcement
        if (isLoggedIn || isPublicPage || isStaticResource) {
            // Authorized or Public: Proceed
            chain.doFilter(request, response);
        } else {
            // Unauthorized: Redirect to login
            System.out.println("Blocked unauthorized access to: " + path);
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}