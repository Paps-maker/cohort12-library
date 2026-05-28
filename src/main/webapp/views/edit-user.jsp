<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="app.model.User" %>
<%
    User user = (User) request.getAttribute("user");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Edit Member</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;700;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; margin: 0; color: #1e293b; }
        .container { max-width: 1000px; margin: auto; padding: 40px 20px; }
        .flex-center { display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .form-card { background: white; width: 450px; border-radius: 24px; border: 1px solid #e2e8f0; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); overflow: hidden; }
        .form-header { background: #0f172a; color: white; padding: 24px; text-align: center; font-weight: 800; text-transform: uppercase; letter-spacing: 1px; }
        .input-group { padding: 0 32px; margin-top: 20px; box-sizing: border-box; }
        label { display: block; font-size: 11px; font-weight: 800; color: #64748b; text-transform: uppercase; margin-bottom: 8px; }
        input, select { width: 100%; padding: 12px; border-radius: 10px; border: 1px solid #e2e8f0; font-family: inherit; box-sizing: border-box; }
        .form-actions { padding: 32px; display: flex; flex-direction: column; gap: 12px; }
        .btn { padding: 12px 24px; border-radius: 12px; font-weight: 700; text-decoration: none; font-size: 14px; border: none; cursor: pointer; transition: 0.2s; text-align: center; }
        .btn-primary { background: #6366f1; color: white; }
        .btn-primary:hover { background: #4f46e5; transform: translateY(-1px); }
        .btn-cancel { text-align: center; font-size: 13px; color: #94a3b8; text-decoration: none; font-weight: 600; }
    </style>
</head>
<body>
<div class="container flex-center">
    <div class="form-card">
        <div class="form-header">Edit Account Settings</div>
        <% if (user != null) { %>
            <form action="<%= contextPath %>/edit-user" method="POST">
                <input type="hidden" name="id" value="<%= user.getId() %>">
                <div class="input-group">
                    <label>Username</label>
                    <input type="text" name="username" value="<%= user.getUsername() %>" required>
                </div>
                <div class="input-group">
                    <label>Email Address</label>
                    <input type="email" name="email" value="<%= user.getEmail() %>" required>
                </div>
                <div class="input-group">
                    <label>System Access Role</label>
                    <select name="role">
                        <option value="USER" <%= "USER".equals(user.getRole()) ? "selected" : "" %>>USER</option>
                        <option value="ADMIN" <%= "ADMIN".equals(user.getRole()) ? "selected" : "" %>>ADMIN</option>
                    </select>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary" style="width:100%">Save Changes</button>
                    <a href="<%= contextPath %>/members" class="btn-cancel">Discard & Return</a>
                </div>
            </form>
        <% } else { %>
            <div style="padding: 40px; text-align: center; color: #64748b;">Member record profile not available.</div>
        <% } %>
    </div>
</div>
</body>
</html>