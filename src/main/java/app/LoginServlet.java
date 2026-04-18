package app;

import app.dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String error = (String) request.getAttribute("error");

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Login</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; background:#f4f6f8; display:flex; justify-content:center; align-items:center; height:100vh; }");
        out.println(".box { background:white; padding:30px; border-radius:10px; width:300px; box-shadow:0 0 10px rgba(0,0,0,0.1);} ");
        out.println("input,button { width:100%; padding:10px; margin:10px 0; }");
        out.println("button { background:#3498db; color:white; border:none; }");
        out.println("</style>");
        out.println("</head><body>");

        out.println("<div class='box'>");
        out.println("<h2>Login</h2>");

        if (error != null) {
            out.println("<p style='color:red;'> Invalid username or password</p>");
        }

        out.println("<form method='post'>");
        out.println("<input type='text' name='username' placeholder='Username' required/>");
        out.println("<input type='password' name='password' placeholder='Password' required/>");
        out.println("<button type='submit'>Login</button>");
        out.println("</form>");

        out.println("</div>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 🔥 DAO LOGIN (NEW SYSTEM)
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUser(username, password);

        if (user != null) {

            HttpSession session = request.getSession();
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            System.out.println("LOGIN SUCCESS → " + user.getUsername() + " (" + user.getRole() + ")");

            response.sendRedirect("books");

        } else {
            request.setAttribute("error", "Invalid");
            doGet(request, response);
        }
    }
}