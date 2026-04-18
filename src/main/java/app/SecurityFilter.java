package app;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebFilter("/*")
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        System.out.println("FILTER HIT: " + req.getServletPath());

        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        String path = req.getServletPath();

        // 🔥 MAP PATH TO SERVLET CLASS (Reflection)
        Class<?> servletClass = ServletRegistry.getServlet(path);

        // If no mapping → allow
        if (servletClass == null) {
            chain.doFilter(request, response);
            return;
        }
// USER ONLY CHECK (BLOCK ADMIN)
        if (servletClass.isAnnotationPresent(UserOnly.class)) {

            if (session == null || "ADMIN".equals(role)) {

                System.out.println("ACCESS DENIED → USER ONLY PAGE");

                res.setContentType("text/html");
                PrintWriter out = res.getWriter();

                out.println("<script>");
                out.println("alert('Only normal users can borrow books!');");
                out.println("window.location='books';");
                out.println("</script>");

                return;
            }
        }
        //  CHECK CUSTOM ANNOTATION reflection
        if (servletClass.isAnnotationPresent(AdminOnly.class)) {

            //  BLOCK NON-ADMIN USERS
            if (session == null || !"ADMIN".equals(role)) {

                System.out.println("ACCESS DENIED → " + path + " for role: " + role);

                // ✅ SHOW POPUP MESSAGE INSTEAD OF REDIRECT
                res.setContentType("text/html");
                PrintWriter out = res.getWriter();

                out.println("<!DOCTYPE html>");
                out.println("<html><head><title>Access Denied</title>");

                out.println("<style>");
                out.println("body { font-family: Arial; background:#f4f6f8; display:flex; justify-content:center; align-items:center; height:100vh; }");
                out.println(".box { background:white; padding:30px; border-radius:10px; width:300px; text-align:center; box-shadow:0 0 10px rgba(0,0,0,0.1);} ");
                out.println("button { padding:10px; margin:10px; border:none; cursor:pointer; }");
                out.println(".login { background:#3498db; color:white; }");
                out.println(".close { background:#ccc; }");
                out.println("</style>");

                out.println("</head><body>");

                out.println("<div class='box'>");
                out.println("<h3> Access Denied</h3>");
                out.println("<p>Only ADMIN users can access this page.</p>");

                // 🔵 LOGIN BUTTON
                out.println("<button class='login' onclick=\"window.location='login'\">Login as Admin</button>");

                // ⚪ CLOSE BUTTON
                out.println("<button class='close' onclick=\"window.history.back()\">Close</button>");

                out.println("</div>");

                out.println("</body></html>");

                return;
            }
        }

        // ✅ ALLOW REQUEST
        chain.doFilter(request, response);
    }

}
