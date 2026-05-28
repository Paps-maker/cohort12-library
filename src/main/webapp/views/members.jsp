<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, app.model.User" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Member Management</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;700;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; margin: 0; color: #1e293b; }
        .container { max-width: 1000px; margin: auto; padding: 40px 20px; }
        .header-flex { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
        h2 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }
        .stats-badge { background: #e2e8f0; color: #475569; padding: 4px 12px; border-radius: 20px; font-size: 14px; margin-left: 12px; }
        .card-list { background: white; border-radius: 24px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
        .user-row { display: flex; justify-content: space-between; align-items: center; padding: 20px 30px; border-bottom: 1px solid #f1f5f9; transition: 0.2s; }
        .user-row:hover { background: #fbfcfd; }
        .user-main { display: flex; align-items: center; gap: 16px; }
        .avatar { width: 48px; height: 48px; background: #f1f5f9; color: #6366f1; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 18px; border: 1px solid #e2e8f0; }
        .name { font-weight: 700; margin: 0; font-size: 16px; color: #0f172a; }
        .email { font-size: 13px; color: #64748b; margin: 2px 0 0 0; }
        .badge { padding: 5px 12px; border-radius: 8px; font-size: 11px; font-weight: 800; text-transform: uppercase; }
        .badge-ADMIN { background: #fee2e2; color: #dc2626; }
        .badge-USER { background: #dcfce7; color: #166534; }
        .btn { padding: 12px 24px; border-radius: 12px; font-weight: 700; text-decoration: none; font-size: 14px; border: none; cursor: pointer; transition: 0.2s; display: inline-block; }
        .btn-primary { background: #6366f1; color: white; }
        .btn-primary:hover { background: #4f46e5; transform: translateY(-1px); }
        .btn-outline { background: white; color: #64748b; border: 1px solid #e2e8f0; }
        .btn-group { display: flex; gap: 10px; }
        .action-group { display: flex; gap: 6px; }
        .action-link { font-size: 12px; font-weight: 700; text-decoration: none; padding: 8px 14px; border-radius: 8px; }
        .edit-link { background: #eff6ff; color: #2563eb; }
        .delete-link { background: #fff1f2; color: #dc2626; }
    </style>
    <script>
        // Deletion alert tracker integration
        function confirmDelete(id, name) {
            if(confirm('Permanently delete ' + name + '?')) {
                document.getElementById('deleteId').value = id;
                document.getElementById('deleteForm').submit();
            }
        }
    </script>
</head>
<body>
<div class="container">
    <div class="header-flex">
        <div><h2>System Members<span class="stats-badge"><%= users != null ? users.size() : 0 %></span></h2></div>
        <div class="btn-group">
            <a class="btn btn-primary" href="<%= contextPath %>/register.jsp">+ Add Member</a>
            <a class="btn btn-outline" href="<%= contextPath %>/books">← Catalog</a>
        </div>
    </div>

    <div class="card-list">
        <% if (users == null || users.isEmpty()) { %>
            <div style="padding:60px; text-align:center; color:#94a3b8;">No members found.</div>
        <% } else {
            for (User u : users) {
                String firstLetter = u.getUsername() != null && !u.getUsername().isEmpty() ? u.getUsername().substring(0, 1).toUpperCase() : "U";
        %>
            <div class="user-row">
                <div class="user-main">
                    <div class="avatar"><%= firstLetter %></div>
                    <div>
                        <p class="name"><%= u.getUsername() %></p>
                        <p class="email"><%= u.getEmail() %></p>
                    </div>
                </div>
                <div style="display:flex; align-items:center; gap:20px;">
                    <span class="badge badge-<%= u.getRole() %>"><%= u.getRole() %></span>
                    <div class="action-group">
                        <a class="action-link edit-link" href="<%= contextPath %>/edit-user?id=<%= u.getId() %>">Edit</a>
                        <a class="action-link delete-link" href="javascript:void(0)" onclick="confirmDelete(<%= u.getId() %>, '<%= u.getUsername() %>')">Delete</a>
                    </div>
                </div>
            </div>
        <%  }
        } %>
    </div>
</div>

<form id="deleteForm" action="<%= contextPath %>/delete-user" method="POST" style="display:none;">
    <input type="hidden" name="id" id="deleteId">
</form>
</body>
</html>