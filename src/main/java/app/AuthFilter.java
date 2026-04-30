package app;

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

        // Get the path relative to the context root
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // ✅ 1. Comprehensive White-list
        // Added "/addbook" to match your new Unified Servlet mapping
        boolean isPublicPage = path.equals("/login") ||
                path.equals("/") ||
                path.equals("/index.jsp") ||
                path.equals("/register.jsp") ||
                path.equals("/registerProcess") ||
                path.equals("/addbook"); // Matches @WebServlet("/addbook")

        // Allow static assets so the UI stays styled
        boolean isStaticResource = path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/");

        // 2. Define Authentication Status
        boolean isLoggedIn = (session != null && session.getAttribute("username") != null);

        // 3. Security Enforcement
        if (isLoggedIn || isPublicPage || isStaticResource) {
            // Authorized or Public: Proceed to the Servlet
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