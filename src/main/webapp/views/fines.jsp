<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, java.net.URLDecoder, java.nio.charset.StandardCharsets" %>
<%
    // Unpack all data model variables passed down from FineController
    String title = (String) request.getAttribute("title");
    String role = (String) request.getAttribute("role");
    String username = (String) request.getAttribute("username");
    Double displayTotal = (Double) request.getAttribute("displayTotal");
    String infoMessage = (String) request.getAttribute("infoMessage");
    List<String> activeLoans = (List<String>) request.getAttribute("activeLoans");
    List<String> history = (List<String>) request.getAttribute("history");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= title %></title>

    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;800&display=swap');
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f1f5f9; margin: 0; padding: 40px 20px; color: #1e293b; }
        .container { max-width: 1100px; margin: auto; }
        .card-header { background: <%= "ADMIN".equals(role) ? "#0f172a" : "#2563eb" %>; color: white; padding: 40px; border-radius: 24px; text-align: center; margin-bottom: 20px; }
        .card-header h1 { margin: 0; font-size: 32px; font-weight: 800; letter-spacing: -1px; }
        .alert-msg { background: #fffbeb; color: #b45309; padding: 15px 25px; border-radius: 12px; border-left: 4px solid #d97706; font-weight: 600; margin-bottom: 20px; text-align: center; font-size: 14px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
        .search-container { position: relative; max-width: 600px; margin: -30px auto 40px auto; padding: 0 20px; }
        #searchBar { width: 100%; padding: 18px 25px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); font-size: 16px; outline: none; transition: 0.2s; box-sizing: border-box; }
        .user-section { background: white; border-radius: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); margin-bottom: 30px; overflow: hidden; border: 1px solid #e2e8f0; }
        .user-header { background: #f8fafc; padding: 20px 30px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
        .user-info { display: flex; align-items: center; gap: 14px; }
        .user-avatar { background: #6366f1; color: white; width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-weight: 800; text-transform: uppercase; }
        .user-balance { background: #fee2e2; color: #dc2626; padding: 6px 16px; border-radius: 12px; font-weight: 800; font-size: 14px; }
        table { width: 100%; border-collapse: collapse; }
        th { text-align: left; padding: 16px 30px; background: #ffffff; color: #64748b; font-size: 11px; text-transform: uppercase; border-bottom: 1px solid #f1f5f9; letter-spacing: 0.5px; }
        td { padding: 15px 30px; border-bottom: 1px solid #f8fafc; font-size: 14px; vertical-align: middle; }
        .status-pill { padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; display: inline-block; }
        .pill-red { background: #fee2e2; color: #dc2626; }
        .pill-green { background: #dcfce7; color: #166534; }
        .btn-del { color: #dc2626; text-decoration: none; font-weight: 700; font-size: 12px; border: 1px solid #fecaca; padding: 6px 14px; border-radius: 8px; background: #fff; cursor: pointer; transition: 0.2s; }
        .btn-del:hover { background: #fee2e2; }
        .nav-bar { display: flex; justify-content: center; margin-top: 40px; padding-bottom: 20px; }
        .nav-bar a { color: #64748b; text-decoration: none; font-weight: 700; font-size: 14px; padding: 12px 24px; border-radius: 12px; background: #ffffff; border: 1px solid #e2e8f0; transition: all 0.2s ease-in-out; display: flex; align-items: center; gap: 8px; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); }
        .nav-bar a:hover { color: #0f172a; background: #f8fafc; border-color: #cbd5e1; transform: translateY(-2px); box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
    </style>

    <script>
        function filterDashboard() {
            const input = document.getElementById('searchBar').value.toLowerCase();
            const cards = document.getElementsByClassName('user-section');
            for (let card of cards) {
                const text = card.textContent.toLowerCase();
                card.style.display = text.includes(input) ? '' : 'none';
            }
        }
    </script>
</head>
<body>
<div class="container">
    <div class="card-header">
        <h1><%= title %></h1>
        <p style="opacity:0.8; font-weight:600;">System Total Risk: KSH <%= String.format("%.2f", displayTotal) %></p>
    </div>

    <% if (infoMessage != null && !infoMessage.trim().isEmpty()) { %>
        <div class="alert-msg"><%= URLDecoder.decode(infoMessage, StandardCharsets.UTF_8) %></div>
    <% } %>

    <div class="search-container">
        <input type="text" id="searchBar" onkeyup="filterDashboard()" placeholder="Search balances or usernames...">
    </div>

<%!
    // Helper method to consistently extract amount from the DAO string
    // Assumes your DAO format: ID | Date | Amount | Status | ...
    public double parseAmount(String fineString) {
        if (fineString == null || !fineString.contains("|")) return 0.0;
        String[] parts = fineString.split("\\|");
        // Index 2 is the Amount based on FineDAO.getUserFines
        if (parts.length > 2) {
            try {
                String raw = parts[2].replaceAll("[^0-9.]", "").trim();
                return raw.isEmpty() ? 0.0 : Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
%>

<%
    // --- DATA COLLECTION RE-GROUPING ALGORITHM ---
    Map<String, List<String>> userHistory = new LinkedHashMap<>();
    Map<String, List<String>> userLoans = new HashMap<>();

    if ("ADMIN".equals(role)) {
        if (history != null) {
            for (String h : history) {
                if (h == null || !h.contains("|")) continue;
                String[] p = h.split("\\|");
                String u = "system";
                for (String part : p) {
                    if (part.trim().toLowerCase().contains("user:")) {
                        u = part.toLowerCase().replaceAll("user:", "").trim();
                        break;
                    }
                }
                if ("system".equals(u) && p.length > 1) {
                    u = p[1].trim().toLowerCase();
                }
                userHistory.computeIfAbsent(u, k -> new ArrayList<>()).add(h);
            }
        }
        if (activeLoans != null) {
            for (String l : activeLoans) {
                if (l == null || !l.contains("|")) continue;
                String[] p = l.split("\\|");
                String u = "system";
                for (String part : p) {
                    if (part.trim().toLowerCase().contains("user:")) {
                        u = part.toLowerCase().replaceAll("user:", "").trim();
                        break;
                    }
                }
                if ("system".equals(u) && p.length > 0) {
                    u = p[0].trim().toLowerCase();
                }
                userLoans.computeIfAbsent(u, k -> new ArrayList<>()).add(l);
            }
        }
    } else {
        userHistory.put(username.toLowerCase(), history != null ? history : new ArrayList<>());
        userLoans.put(username.toLowerCase(), activeLoans != null ? activeLoans : new ArrayList<>());
    }

    Set<String> allUsers = new TreeSet<>();
    allUsers.addAll(userHistory.keySet());
    allUsers.addAll(userLoans.keySet());
    allUsers.remove("system");

    if (allUsers.isEmpty()) {
%>
    <div class="user-section" style="padding:60px; text-align:center; color:#64748b;">🎉 No active loans or outstanding fine records found.</div>
<%
    }

    // --- RENDERING LOOP ---
    for (String targetUser : allUsers) {
        if (targetUser.trim().isEmpty()) continue;

        double userTotal = 0.0;
        if (userHistory.containsKey(targetUser)) {
            for (String f : userHistory.get(targetUser)) {
                if (f.toUpperCase().contains("UNPAID")) {
                    userTotal += parseAmount(f);
                }
            }
        }

        String displayName = targetUser.substring(0,1).toUpperCase() + targetUser.substring(1);
%>
    <div class="user-section">
        <div class="user-header">
            <div class="user-info">
                <div class="user-avatar"><%= displayName.substring(0,1) %></div>
                <span style="font-weight:800; font-size:18px; color:#1e293b;"><%= displayName %></span>
            </div>
            <div class="user-balance">Outstanding: KSH <%= String.format("%.2f", userTotal) %></div>
        </div>

        <table>
            <thead><tr><th>Loan Details</th><th>Status</th></tr></thead>
            <tbody>
            <%
                List<String> loans = userLoans.getOrDefault(targetUser, new ArrayList<>());
                for (String loan : loans) {
                    if (loan == null || !loan.contains("|")) continue;
                    String[] lp = loan.split("\\|");
                    String t = ("ADMIN".equals(role)) ? (lp.length > 2 ? lp[2].trim() : lp[0].trim()) : lp[1].trim();
                    String s = lp[lp.length-1].trim();
            %>
                <tr><td><b><%= t %></b></td><td><span class="status-pill <%= s.toUpperCase().contains("DUE") ? "pill-red" : "pill-green" %>"><%= s %></span></td></tr>
            <% } %>
            </tbody>
        </table>

        <div style="padding:15px 30px; background:#f8fafc; font-weight:700; font-size:12px; color:#64748b; border-top:1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9;">PAYMENT AUDIT TRAIL</div>
        <table>
            <thead><tr><th>Audit ID</th><th>Date</th><th>Amount</th><th>Status</th><th style="text-align:right;">Action</th></tr></thead>
            <tbody>
            <%
                List<String> accountFines = userHistory.getOrDefault(targetUser, new ArrayList<>());
                for (String fine : accountFines) {
                    String[] fParts = fine.split("\\|");
                    String amt = String.format("%.2f", parseAmount(fine));
                    boolean isUnpaid = fine.toUpperCase().contains("UNPAID");
                    String fId = fParts[0].replaceAll("[^0-9]", "").trim();
                    String timestamp = ("ADMIN".equals(role)) ? (fParts.length > 4 ? fParts[4].trim() : fParts[1].trim()) : (fParts.length > 1 ? fParts[1].trim() : "Recent");
            %>
                <tr>
                    <td>ID: #<%= fId %></td>
                    <td><%= timestamp %></td>
                    <td><b>KSH <%= amt %></b></td>
                    <td><span class="status-pill <%= isUnpaid ? "pill-red" : "pill-green" %>"><%= isUnpaid ? "UNPAID" : "PAID" %></span></td>
                    <td style="text-align:right;">
                        <% if ("ADMIN".equals(role)) { %>
                            <form action="<%= contextPath %>/delete-fine" method="POST" style="margin:0; display:inline-block;"><input type="hidden" name="fineId" value="<%= fId %>"><button type="submit" class="btn-del">Delete</button></form>
                        <% } else if (isUnpaid) { %>
                            <form action="<%= contextPath %>/pay-fine" method="POST" style="margin:0; display:inline-block;"><input type="hidden" name="fineId" value="<%= fId %>"><button type="submit" style="background:#2563eb; color:white; border:none; padding:8px 16px; border-radius:8px; cursor:pointer; font-weight:700;">Pay Now</button></form>
                        <% } else { %>
                            <span style="color:#166534; background:#dcfce7; padding:4px 12px; border-radius:6px; font-weight:700;">Settled</span>
                        <% } %>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
<% } %>

    <div class="nav-bar"><a href="<%= contextPath %>/books">← Back to Catalog</a></div>
</div>
</body>
</html>