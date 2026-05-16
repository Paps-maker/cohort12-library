package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.model.User;
import app.services.UserService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class UserController {

    @Inject
    private UserService userService;

    // =========================================================================
    // 1. LIST VIEW (MEMBERS DASHBOARD)
    // =========================================================================
    @ActionGetMethod("/members")
    public void viewMembers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();

        // Admin Security Check
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }

        List<User> users = userService.getAllUsers();
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html><html><head><title>Member Management</title>");
        injectModernStyles(writer);
        writer.println("</head><body>");

        writer.println("<div class='container'>");
        writer.println("    <div class='header-flex'>");
        writer.println("        <div><h2>System Members<span class='stats-badge'>" + users.size() + "</span></h2></div>");
        writer.println("        <div class='btn-group'>");
        writer.println("            <a class='btn btn-primary' href='register.jsp'>+ Add Member</a>");
        writer.println("            <a class='btn btn-outline' href='books'>← Catalog</a>");
        writer.println("        </div>");
        writer.println("    </div>");

        writer.println("    <div class='card-list'>");

        if (users.isEmpty()) {
            writer.println("        <div style='padding:60px; text-align:center; color:#94a3b8;'>No members found.</div>");
        } else {
            for (User u : users) {
                String firstLetter = u.getUsername().substring(0, 1).toUpperCase();
                writer.println("        <div class='user-row'>");
                writer.println("            <div class='user-main'>");
                writer.println("                <div class='avatar'>" + firstLetter + "</div>");
                writer.println("                <div><p class='name'>" + u.getUsername() + "</p>");
                writer.println("                <p class='email'>" + u.getEmail() + "</p></div>");
                writer.println("            </div>");
                writer.println("            <div style='display:flex; align-items:center; gap:20px;'>");
                writer.println("                <span class='badge badge-" + u.getRole() + "'>" + u.getRole() + "</span>");
                writer.println("                <div class='action-group'>");
                writer.println("                    <a class='action-link edit-link' href='edit-user?id=" + u.getId() + "'>Edit</a>");
                writer.println("                    <a class='action-link delete-link' href='javascript:void(0)' onclick=\"confirmDelete(" + u.getId() + ", '" + u.getUsername() + "')\">Delete</a>");
                writer.println("                </div>");
                writer.println("            </div>");
                writer.println("        </div>");
            }
        }

        writer.println("    </div>");
        writer.println("</div>");

        // Hidden form for secure deletion via POST
        writer.println("<form id='deleteForm' action='delete-user' method='POST' style='display:none;'>");
        writer.println("<input type='hidden' name='id' id='deleteId'></form>");

        writer.println("<script>function confirmDelete(id, name) { if(confirm('Permanently delete ' + name + '?')) { document.getElementById('deleteId').value = id; document.getElementById('deleteForm').submit(); } }</script>");
        writer.println("</body></html>");
    }

    // =========================================================================
    // 2. EDIT VIEW (MODERN FORM)
    // =========================================================================
    @ActionGetMethod("/edit-user")
    public void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/members");
            return;
        }

        User user = userService.getUserById(Integer.parseInt(idParam));
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        writer.println("<!DOCTYPE html><html><head><title>Edit Member</title>");
        injectModernStyles(writer);
        writer.println("</head><body><div class='container flex-center'>");

        writer.println("    <div class='form-card'>");
        writer.println("        <div class='form-header'>Edit Account Settings</div>");
        if (user != null) {
            writer.println("        <form action='edit-user' method='POST'>");
            writer.println("            <input type='hidden' name='id' value='" + user.getId() + "'>");
            writer.println("            <div class='input-group'><label>Username</label><input type='text' name='username' value='" + user.getUsername() + "' required></div>");
            writer.println("            <div class='input-group'><label>Email Address</label><input type='email' name='email' value='" + user.getEmail() + "' required></div>");
            writer.println("            <div class='input-group'><label>System Access Role</label><select name='role'>");
            writer.println("                <option value='USER' " + ("USER".equals(user.getRole()) ? "selected" : "") + ">USER</option>");
            writer.println("                <option value='ADMIN' " + ("ADMIN".equals(user.getRole()) ? "selected" : "") + ">ADMIN</option>");
            writer.println("            </select></div>");
            writer.println("            <div class='form-actions'>");
            writer.println("                <button type='submit' class='btn btn-primary' style='width:100%'>Save Changes</button>");
            writer.println("                <a href='members' class='btn-cancel'>Discard & Return</a>");
            writer.println("            </div>");
            writer.println("        </form>");
        }
        writer.println("    </div>");
        writer.println("</div></body></html>");
    }

    // =========================================================================
    // 3. LOGIC HANDLERS (POST)
    // =========================================================================
    @ActionPostMethod("/edit-user")
    public void processUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            User updated = new User(id, req.getParameter("username"), req.getParameter("email"), null, req.getParameter("role"));
            userService.updateUser(updated);
        } catch (Exception e) { e.printStackTrace(); }
        resp.sendRedirect(req.getContextPath() + "/members");
    }

    @ActionPostMethod("/delete-user")
    public void processDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        if (idParam != null) {
            userService.deleteUser(Integer.parseInt(idParam));
        }
        resp.sendRedirect(req.getContextPath() + "/members");
    }

    // =========================================================================
    // CSS INJECTION
    // =========================================================================
    private void injectModernStyles(PrintWriter w) {
        w.println("<style>");
        w.println("@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap');");
        w.println("body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; margin: 0; color: #1e293b; }");
        w.println(".container { max-width: 1000px; margin: auto; padding: 40px 20px; }");
        w.println(".flex-center { display: flex; justify-content: center; align-items: center; min-height: 100vh; }");

        // Headers & Badges
        w.println(".header-flex { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }");
        w.println("h2 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }");
        w.println(".stats-badge { background: #e2e8f0; color: #475569; padding: 4px 12px; border-radius: 20px; font-size: 14px; margin-left: 12px; }");

        // Cards & Rows
        w.println(".card-list { background: white; border-radius: 24px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }");
        w.println(".user-row { display: flex; justify-content: space-between; align-items: center; padding: 20px 30px; border-bottom: 1px solid #f1f5f9; transition: 0.2s; }");
        w.println(".user-row:hover { background: #fbfcfd; }");
        w.println(".user-main { display: flex; align-items: center; gap: 16px; }");
        w.println(".avatar { width: 48px; height: 48px; background: #f1f5f9; color: #6366f1; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 18px; border: 1px solid #e2e8f0; }");
        w.println(".name { font-weight: 700; margin: 0; font-size: 16px; color: #0f172a; }");
        w.println(".email { font-size: 13px; color: #64748b; margin: 2px 0 0 0; }");

        // Status Badges
        w.println(".badge { padding: 5px 12px; border-radius: 8px; font-size: 11px; font-weight: 800; text-transform: uppercase; }");
        w.println(".badge-ADMIN { background: #fee2e2; color: #dc2626; }");
        w.println(".badge-USER { background: #dcfce7; color: #166534; }");

        // Buttons & Links
        w.println(".btn { padding: 12px 24px; border-radius: 12px; font-weight: 700; text-decoration: none; font-size: 14px; border: none; cursor: pointer; transition: 0.2s; }");
        w.println(".btn-primary { background: #6366f1; color: white; }");
        w.println(".btn-primary:hover { background: #4f46e5; transform: translateY(-1px); }");
        w.println(".btn-outline { background: white; color: #64748b; border: 1px solid #e2e8f0; }");
        w.println(".action-link { font-size: 12px; font-weight: 700; text-decoration: none; padding: 8px 14px; border-radius: 8px; margin-left: 6px; }");
        w.println(".edit-link { background: #eff6ff; color: #2563eb; }");
        w.println(".delete-link { background: #fff1f2; color: #dc2626; }");

        // Form Specific
        w.println(".form-card { background: white; width: 450px; border-radius: 24px; border: 1px solid #e2e8f0; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); overflow: hidden; }");
        w.println(".form-header { background: #0f172a; color: white; padding: 24px; text-align: center; font-weight: 800; text-transform: uppercase; letter-spacing: 1px; }");
        w.println(".input-group { padding: 0 32px; margin-top: 20px; }");
        w.println("label { display: block; font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; margin-bottom: 8px; }");
        w.println("input, select { width: 100%; padding: 12px; border-radius: 10px; border: 1px solid #e2e8f0; font-family: inherit; box-sizing: border-box; }");
        w.println(".form-actions { padding: 32px; display: flex; flex-direction: column; gap: 12px; }");
        w.println(".btn-cancel { text-align: center; font-size: 13px; color: #94a3b8; text-decoration: none; font-weight: 600; }");
        w.println("</style>");
    }
}