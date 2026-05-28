<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.net.URLDecoder, java.nio.charset.StandardCharsets" %>
<%
    String username = (String) request.getAttribute("username");
    String role = (String) request.getAttribute("role");
    String infoMessage = (String) request.getAttribute("infoMessage");
    List<String> borrowedList = (List<String>) request.getAttribute("borrowedList");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Library | View Loans</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px; color: #0f172a; }
        .container { max-width: 1100px; margin: auto; }
        .admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
        .admin-header h1 { font-size: 32px; font-weight: 800; letter-spacing: -1.5px; margin: 0; color: #1e293b; }
        .alert-msg { background: #fffbeb; color: #b45309; padding: 15px 25px; border-radius: 12px; border-left: 4px solid #d97706; font-weight: 600; margin-bottom: 20px; font-size: 14px; }
        .search-box { width: 100%; padding: 15px 25px; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 30px; font-family: inherit; font-size: 16px; outline: none; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); box-sizing: border-box; }
        .user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; }
        .user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; gap: 12px; }
        .user-avatar { background: #3b82f6; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; text-transform: uppercase; }
        .user-name { font-weight: 700; font-size: 18px; color: #334155; }
        table { width: 100%; border-collapse: collapse; background: white; }
        th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid #f1f5f9; }
        td { padding: 20px 30px; border-bottom: 1px solid #f8fafc; font-size: 15px; vertical-align: middle; }
        .id-badge { background: #eff6ff; color: #2563eb; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }
        .book-title { font-weight: 600; color: #1e293b; display: block; }
        .status-pill { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; display: inline-flex; align-items: center; gap: 6px; }
        .pill-overdue { color: #e11d48; background: #fff1f2; border: 1px solid #ffe4e6; }
        .pill-ontime { color: #059669; background: #ecfdf5; border: 1px solid #d1fae5; }
        .btn { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; text-decoration: none; border-radius: 10px; font-weight: 600; font-size: 13px; transition: 0.2s; border: none; cursor: pointer; }
        .btn-return { background: #0f172a; color: white; }
        .nav-bar { display: flex; gap: 15px; margin-top: 40px; padding: 20px; background: white; border-radius: 16px; border: 1px solid #e2e8f0; justify-content: center; }
        .btn-outline { border: 1px solid #e2e8f0; color: #64748b; background: transparent; }
    </style>
    <script>
        function filterContent() {
            let filter = document.getElementById('searchEngine').value.toLowerCase();
            let sections = document.getElementsByClassName('user-section');
            for (let i = 0; i < sections.length; i++) {
                let text = sections[i].innerText.toLowerCase();
                sections[i].style.display = text.includes(filter) ? '' : 'none';
            }
        }
    </script>
</head>
<body>
<div class="container">
    <div class="admin-header"><h1><%= "ADMIN".equals(role) ? "Records" : "My Loans" %></h1></div>

    <% if (infoMessage != null && !infoMessage.trim().isEmpty()) { %>
        <div class="alert-msg">🔔 <%= URLDecoder.decode(infoMessage, StandardCharsets.UTF_8) %></div>
    <% } %>

    <input type="text" id="searchEngine" onkeyup="filterContent()" class="search-box" placeholder="Search records...">

    <% if (borrowedList == null || borrowedList.isEmpty()) { %>
        <div class="user-section" style="padding:80px; text-align:center;">
            <p style="color:#64748b; font-size:18px;">📭 No active borrow logs found.</p>
        </div>
    <% } else {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        Map<String, String> originalNames = new HashMap<>();

        for (String r : borrowedList) {
            if (r == null || !r.contains("|")) continue;
            String rawUser = "Member";
            String[] parts = r.split("\\|");
            for (String p : parts) {
                if (p.contains("User:")) {
                    rawUser = p.replace("User:", "").trim();
                }
            }

            String lookupKey = rawUser.toLowerCase();
            groups.computeIfAbsent(lookupKey, k -> new ArrayList<>()).add(r);
            originalNames.putIfAbsent(lookupKey, rawUser);
        }

        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String displayName = originalNames.get(entry.getKey());
            String avatarLetter = displayName.isEmpty() ? "M" : displayName.substring(0, 1);
    %>
        <div class="user-section">
            <div class="user-header">
                <div class="user-avatar"><%= avatarLetter %></div>
                <span class="user-name"><%= displayName %></span>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>ID</th><th>Title</th><th>Timeline</th>
                        <%= "ADMIN".equals(role) ? "<th style='text-align:right;'>Action</th>" : "" %>
                    </tr>
                </thead>
                <tbody>
                <% for (String record : entry.getValue()) {
                    String[] parts = record.split("\\|");
                    String id = "0", title = "N/A", timeline = "";
                    boolean isOverdue = record.contains("OVERDUE");
                    for (String part : parts) {
                        if (part.contains("ID:")) id = part.replace("ID:", "").trim();
                        else if (part.contains("Book:") || part.contains("Title:")) title = part.replace("Book:", "").replace("Title:", "").trim();
                        else if (part.contains("left") || part.contains("OVERDUE")) timeline = part.trim();
                    }
                %>
                    <tr>
                        <td><span class="id-badge">#<%= id %></span></td>
                        <td><span class="book-title"><%= title %></span></td>
                        <td><span class="status-pill <%= isOverdue ? "pill-overdue" : "pill-ontime" %>"><%= timeline %></span></td>
                        <% if ("ADMIN".equals(role)) { %>
                            <td style="text-align:right;">
                                <form action="<%= contextPath %>/library/return" method="POST" style="margin:0; display:inline-block;">
                                    <input type="hidden" name="borrowId" value="<%= id %>">
                                    <button type="submit" class="btn btn-return">Return</button>
                                </form>
                            </td>
                        <% } %>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    <%  }
    } %>
    <div class="nav-bar">
        <a href="<%= contextPath %>/books" class="btn btn-return">Catalog</a>
        <a href="<%= contextPath %>/fines" class="btn btn-outline">Fines</a>
    </div>
</div>
</body>
</html>