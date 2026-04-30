package app;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get existing session without creating a new one
        HttpSession session = request.getSession(false);

        if (session != null) {
            // Log the user out in the console for tracking
            System.out.println("LOGOUT → User: " + session.getAttribute("username"));

            //  This triggers SessionListener.sessionDestroyed and lowers active count
            session.invalidate();
        }

        // ✅ Use ContextPath to ensure the redirect works in all environments
        response.sendRedirect(request.getContextPath() + "/login");
    }
}