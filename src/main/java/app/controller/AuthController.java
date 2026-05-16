package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.dao.UserDAO;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthController {

    @Inject
    private UserDAO userDAO;

    // --- 1. RENDER LOGIN UI ---
    @ActionGetMethod("/login")
    public void showLoginPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String error = request.getParameter("error");
        String contextPath = request.getContextPath();

        out.println("<!DOCTYPE html><html><head><title>Library | Login</title>");
        out.println("<style>");
        out.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;800&display=swap');");
        out.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background:#f8fafc; display:flex; justify-content:center; align-items:center; height:100vh; margin:0; color: #1e293b; }");
        out.println(".box { background:white; padding:40px; border-radius:24px; width:340px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); border: 1px solid #f1f5f9; }");
        out.println("h2 { margin-top: 0; font-weight: 800; letter-spacing: -1px; color: #0f172a; }");
        out.println("input { width:100%; padding:12px; margin:10px 0; border: 1px solid #e2e8f0; border-radius: 12px; box-sizing: border-box; font-family: inherit; }");
        out.println("button { width:100%; padding:12px; margin-top:10px; background:#2563eb; color:white; border:none; border-radius:12px; font-weight: 600; cursor:pointer; transition: 0.2s; }");
        out.println("button:hover { background:#1d4ed8; transform: translateY(-1px); }");
        out.println(".error-msg { color: #e11d48; background: #fff1f2; padding: 10px; border-radius: 8px; font-size: 14px; text-align: center; margin-bottom: 15px; border: 1px solid #ffe4e6; }");
        out.println("</style></head><body>");

        out.println("<div class='box'>");
        out.println("<h2>Welcome Back</h2>");

        if (error != null) {
            out.println("<div class='error-msg'>Invalid username or password</div>");
        }

        // Using a relative path for the action is often safer when the Dispatcher is at "/"
        out.println("<form action='authenticate' method='POST'>");
        out.println("<input type='text' name='username' placeholder='Username' required autofocus/>");
        out.println("<input type='password' name='password' placeholder='Password' required/>");
        out.println("<button type='submit'>Sign In</button>");
        out.println("<button type='button' onclick=\"window.location='" + contextPath + "/index.jsp'\" style='background:#f3f4f6; color:#4b5563; border:none; margin-top:8px; padding:12px; border-radius:12px; font-weight:600; cursor:pointer; width:100%;'>Back to Home</button>");
        out.println("</form>");

        out.println("</div></body></html>");
    }

    // --- 2. HANDLE LOGIN LOGIC ---
    @ActionPostMethod("/authenticate")
    public void authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Use the injected UserDAO to find user credentials
        User user = userDAO.findUser(username, password);

        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // Simple relative redirect to the books route
            response.sendRedirect("books");
        } else {
            // Failed login: back to login page with error flag
            response.sendRedirect("login?error=true");
        }
    }

    // --- 3. HANDLE LOGOUT ---
    @ActionGetMethod("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("login");
    }
}