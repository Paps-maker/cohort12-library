package app;

import app.dao.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/members")
@AdminOnly
public class members extends GenericServlet {

    @Override
    public void service(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        UserDAO dao = new UserDAO();
        List<User> users = dao.getAllUsers();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Library Members</title>");

        // ================= CSS =================
        out.println("<style>");

        out.println(".add-member-btn {");
        out.println("    display: inline-block;");
        out.println("    background: linear-gradient(135deg, #27ae60, #2ecc71);");
        out.println("    color: white;");
        out.println("    padding: 10px 16px;");
        out.println("    border-radius: 8px;");
        out.println("    text-decoration: none;");
        out.println("    font-weight: 600;");
        out.println("    font-size: 14px;");
        out.println("    transition: all 0.3s ease;");
        out.println("    box-shadow: 0 4px 10px rgba(39, 174, 96, 0.25);");
        out.println("}");

        out.println(".add-member-btn:hover {");
        out.println("    transform: translateY(-2px);");
        out.println("    box-shadow: 0 6px 14px rgba(39, 174, 96, 0.35);");
        out.println("    background: linear-gradient(135deg, #2ecc71, #27ae60);");
        out.println("}");

        out.println(".add-member-btn:active {");
        out.println("    transform: scale(0.98);");
        out.println("}");

        out.println("</style>");
        out.println("<style>");


        out.println("body {");
        out.println("    font-family: 'Segoe UI', sans-serif;");
        out.println("    background: linear-gradient(135deg,#f4f6f8,#e9eef5);");
        out.println("    padding: 30px;");
        out.println("}");

        out.println(".header {");
        out.println("    display:flex;");
        out.println("    justify-content:space-between;");
        out.println("    align-items:center;");
        out.println("    max-width:1000px;");
        out.println("    margin:auto;");
        out.println("    margin-bottom:20px;");
        out.println("}");

        out.println("h2 { color:#2c3e50; margin:0; }");

        out.println(".back {");
        out.println("    text-decoration:none;");
        out.println("    background:#2a5298;");
        out.println("    color:white;");
        out.println("    padding:10px 15px;");
        out.println("    border-radius:8px;");
        out.println("}");

        out.println(".container {");
        out.println("    max-width:1000px;");
        out.println("    margin:auto;");
        out.println("}");

        out.println(".card {");
        out.println("    background:white;");
        out.println("    padding:20px;");
        out.println("    border-radius:15px;");
        out.println("    box-shadow:0 10px 25px rgba(0,0,0,0.08);");
        out.println("}");

        out.println(".user {");
        out.println("    display:flex;");
        out.println("    justify-content:space-between;");
        out.println("    align-items:center;");
        out.println("    padding:12px;");
        out.println("    margin-bottom:10px;");
        out.println("    background:#f9fbfd;");
        out.println("    border-radius:10px;");
        out.println("    transition:0.3s;");
        out.println("}");

        out.println(".user:hover {");
        out.println("    transform: translateX(5px);");
        out.println("    background:#eef3fb;");
        out.println("}");

        out.println(".info { display:flex; flex-direction:column; }");

        out.println(".name { font-weight:bold; color:#2c3e50; }");

        out.println(".email { font-size:12px; color:#7f8c8d; }");

        out.println(".role {");
        out.println("    padding:5px 10px;");
        out.println("    border-radius:20px;");
        out.println("    font-size:11px;");
        out.println("    color:white;");
        out.println("    margin-left:10px;");
        out.println("}");

        out.println(".ADMIN { background:#e74c3c; }");
        out.println(".USER { background:#3498db; }");

        out.println(".actions a {");
        out.println("    text-decoration:none;");
        out.println("    padding:6px 10px;");
        out.println("    margin-left:5px;");
        out.println("    border-radius:6px;");
        out.println("    font-size:12px;");
        out.println("    color:white;");
        out.println("}");

        out.println(".edit { background:#27ae60; }");
        out.println(".delete { background:#e74c3c; }");

        out.println("</style>");

        out.println("</head><body>");

        // ================= HEADER =================
        out.println("<div class='header'>");
        out.println("<h2> Library Members</h2>");
        out.println("<a href='register.jsp' style='background:#27ae60;'> Add Member</a>");
        out.println("<a class='back' href='books'> Back to Books</a>");
        out.println("</div>");

        out.println("<div class='container'>");
        out.println("<div class='card'>");

        // ================= USERS =================
        for (User u : users) {

            out.println("<div class='user'>");

            out.println("<div class='info'>");
            out.println("<div class='name'> " + u.getUsername() + "</div>");
            out.println("<div class='email'>" + u.getEmail() + "</div>");
            out.println("</div>");

            out.println("<div>");
            out.println("<span class='role " + u.getRole() + "'>" + u.getRole() + "</span>");

            out.println("<span class='actions'>");

            // EDIT BUTTON
            out.println("<a class='edit' href='edit-user?id=" + u.getId() + "'>Edit</a>");

            // DELETE BUTTON
            out.println("<a class='delete' href='delete-user?id=" + u.getId() + "' onclick=\"return confirm('Delete user?')\">Delete</a>");

            out.println("</span>");

            out.println("</div>");

            out.println("</div>");
        }

        out.println("</div>");
        out.println("</div>");

        out.println("</body></html>");
    }
}