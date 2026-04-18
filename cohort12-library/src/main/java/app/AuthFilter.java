package app;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// APPLY FILTER TO MULTIPLE PAGES
@WebFilter({"/borrow", "/borrowed", "/books"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        // CHECK IF USER IS LOGGED IN
        if (session == null || session.getAttribute("username") == null) {

            // NOT LOGGED IN → REDIRECT TO LOGIN
            resp.sendRedirect("login");

        } else {

            // ✅ LOGGED IN → ALLOW REQUEST
            chain.doFilter(request, response);
        }
    }
}