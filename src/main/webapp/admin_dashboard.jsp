<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="app.model.Book" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Library System </title>

    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Inter:wght@400;500;600;700&display=swap');

        :root {
            --slate-950: #020617;
            --slate-900: #0f172a;
            --slate-800: #1e293b;
            --slate-700: #334155;
            --slate-600: #475569;
            --slate-400: #94a3b8;
            --indigo-600: #4f46e5;
            --indigo-700: #4338ca;
            --blue-600: #2563eb;
            --emerald-600: #10b981;
            --rose-600: #e11d48;
            --amber-500: #f59e0b;
            --bg-main: #f8fafc;
            --border-color: #f1f5f9;
            --card-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05), 0 1px 2px -1px rgba(0, 0, 0, 0.05);
            --sidebar-width: 270px;
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            margin: 0;
            padding: 0;
            background-color: var(--bg-main);
            color: var(--slate-800);
            display: flex;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
        }

        /* Sidebar Architecture */
        .sidebar {
            width: var(--sidebar-width);
            background-color: var(--slate-950);
            color: white;
            display: flex;
            flex-direction: column;
            padding: 32px 24px;
            box-sizing: border-box;
            position: fixed;
            height: 100vh;
            left: 0;
            top: 0;
            z-index: 50;
        }

        .sidebar-brand {
            font-size: 20px;
            font-weight: 800;
            letter-spacing: -0.5px;
            margin-bottom: 40px;
            padding-left: 8px;
            color: white;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .sidebar-brand i {
            color: var(--indigo-600);
        }

        .menu-section-label {
            font-size: 11px;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--slate-600);
            letter-spacing: 1px;
            margin: 16px 0 8px 8px;
        }

        .sidebar-menu {
            list-style: none;
            padding: 0;
            margin: 0;
            display: flex;
            flex-direction: column;
            gap: 4px;
        }

        .sidebar-link {
            display: flex;
            align-items: center;
            gap: 12px;
            color: var(--slate-400);
            text-decoration: none;
            padding: 12px 16px;
            border-radius: 10px;
            font-weight: 600;
            font-size: 14px;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
        }

        .sidebar-link i {
            font-size: 16px;
            width: 20px;
            text-align: center;
        }

        .sidebar-link:hover {
            background-color: rgba(255, 255, 255, 0.03);
            color: white;
        }

        .sidebar-link.active {
            background-color: var(--indigo-600);
            color: white;
            box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
        }

        .sidebar-link.logout-btn {
            margin-top: auto;
            background-color: rgba(225, 29, 72, 0.08);
            color: #fda4af;
        }

        .sidebar-link.logout-btn:hover {
            background-color: var(--rose-600);
            color: white;
        }

        /* Workspace Core Container */
        .main-workspace {
            margin-left: var(--sidebar-width);
            flex-grow: 1;
            padding: 40px 48px;
            box-sizing: border-box;
            max-width: 1600px;
        }

        .top-navbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 40px;
        }

        .welcome-title {
            font-size: 28px;
            font-weight: 800;
            color: var(--slate-900);
            margin: 0;
            letter-spacing: -0.75px;
        }

        .date-badge {
            background-color: white;
            padding: 12px 20px;
            border-radius: 14px;
            font-size: 13px;
            font-weight: 700;
            border: 1px solid #e2e8f0;
            color: var(--slate-700);
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 4px;
            box-shadow: var(--card-shadow);
        }

        /* Metrics Infrastructure */
        .kpi-row {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 20px;
            margin-bottom: 40px;
        }

        .kpi-card {
            background-color: white;
            border-radius: 16px;
            padding: 24px;
            border: 1px solid #e2e8f0;
            box-shadow: var(--card-shadow);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            overflow: hidden;
        }

        .kpi-header {
            font-size: 12px;
            font-weight: 700;
            color: var(--slate-400);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .kpi-header i {
            font-size: 16px;
            color: var(--slate-400);
        }

        .kpi-body {
            font-size: 28px;
            font-weight: 800;
            color: var(--slate-900);
            margin-top: 14px;
            letter-spacing: -0.5px;
        }

        /* Quick Tools Panel */
        .workbench-section {
            background-color: white;
            border-radius: 18px;
            border: 1px solid #e2e8f0;
            padding: 28px;
            margin-bottom: 40px;
            box-shadow: var(--card-shadow);
        }

        .section-title {
            font-size: 20px;
            font-weight: 800;
            color: var(--slate-900);
            margin-top: 0;
            margin-bottom: 6px;
            letter-spacing: -0.5px;
        }

        .section-desc {
            font-size: 14px;
            color: var(--slate-400);
            margin-bottom: 24px;
        }

        .button-group {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }

        .action-button {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 13px;
            text-decoration: none;
            transition: all 0.2s ease;
            cursor: pointer;
            border: none;
        }

        .btn-add-book { background-color: var(--emerald-600); color: white; }
        .btn-add-book:hover { background-color: #0d9488; transform: translateY(-1px); }

        .btn-add-member { background-color: var(--blue-600); color: white; }
        .btn-add-member:hover { background-color: #1d4ed8; transform: translateY(-1px); }

        .btn-analytics { background-color: var(--indigo-600); color: white; }
        .btn-analytics:hover { background-color: var(--indigo-700); transform: translateY(-1px); }

        .btn-fines { background-color: var(--amber-500); color: white; }
        .btn-fines:hover { background-color: #d97706; transform: translateY(-1px); }

        /* Datatable Framework */
        .catalog-table-container {
            background-color: white;
            border-radius: 18px;
            border: 1px solid #e2e8f0;
            overflow: hidden;
            margin-bottom: 40px;
            box-shadow: var(--card-shadow);
        }

        .table-header-bar {
            padding: 24px 32px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 14px;
        }

        th {
            background-color: #fafafa;
            color: var(--slate-600);
            font-weight: 700;
            padding: 16px 32px;
            border-bottom: 1px solid #e2e8f0;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        td {
            padding: 20px 32px;
            border-bottom: 1px solid var(--border-color);
            color: var(--slate-800);
            vertical-align: middle;
        }

        tr:last-child td {
            border-bottom: none;
        }

        .book-meta {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .table-book-cover {
            width: 44px;
            height: 64px;
            object-fit: cover;
            border-radius: 8px;
            background-color: var(--bg-main);
            border: 1px solid #e2e8f0;
            box-shadow: 0 2px 4px rgba(0,0,0,0.02);
        }

        .table-book-title {
            font-weight: 700;
            color: var(--slate-900);
            margin-bottom: 4px;
            font-size: 15px;
        }

        .table-book-desc {
            font-size: 13px;
            color: var(--slate-400);
            max-width: 400px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            font-weight: 700;
            padding: 6px 12px;
            border-radius: 8px;
        }

        .status-available { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
        .status-empty { background-color: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }

        .table-actions {
            display: flex;
            gap: 8px;
        }

        .action-link {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 14px;
            border-radius: 8px;
            font-size: 13px;
            font-weight: 700;
            text-decoration: none;
            transition: all 0.2s;
        }

        .action-edit { background-color: #f8fafc; color: var(--slate-700); border: 1px solid #e2e8f0; }
        .action-edit:hover { background-color: var(--slate-900); color: white; border-color: var(--slate-900); }

        .action-delete { background-color: rgba(225, 29, 72, 0.04); color: var(--rose-600); border: 1px solid rgba(225, 29, 72, 0.1); }
        .action-delete:hover { background-color: var(--rose-600); color: white; border-color: var(--rose-600); }

        /* Grid Infrastructure Layout */
        .analytics-grid-row {
            display: grid;
            grid-template-columns: repeat(12, 1fr);
            gap: 24px;
            margin-top: 20px;
        }

        .col-12 { grid-column: span 12; }
        .col-lg-7 { grid-column: span 7; }
        .col-lg-5 { grid-column: span 5; }

        .card {
            background: white;
            border-radius: 20px;
            border: 1px solid #e2e8f0;
            box-shadow: var(--card-shadow);
            overflow: hidden;
        }

        .card-header {
            background-color: white;
            border-bottom: 1px solid var(--border-color);
            padding: 24px 28px;
            font-weight: 700;
            font-size: 16px;
            color: var(--slate-900);
            display: flex;
            justify-content: space-between;
            align-items: center;
            letter-spacing: -0.3px;
        }

        .card-body {
            padding: 24px;
        }

        .chart-container { position: relative; height: 320px; width: 100%; }

        /* Modal Overlay and Content Core Framework */
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(2, 6, 23, 0.65);
            backdrop-filter: blur(4px);
            z-index: 200;
            display: flex;
            align-items: center;
            justify-content: center;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .modal-overlay.active {
            opacity: 1;
            pointer-events: auto;
        }

        .modal-wrapper {
            background-color: white;
            width: 100%;
            max-width: 580px;
            border-radius: 20px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            transform: scale(0.95);
            transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            overflow: hidden;
        }

        .modal-overlay.active .modal-wrapper {
            transform: scale(1);
        }

        .modal-header {
            padding: 24px 28px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .modal-close-btn {
            background: none;
            border: none;
            color: var(--slate-400);
            font-size: 20px;
            cursor: pointer;
            padding: 4px;
            transition: color 0.15s ease;
        }

        .modal-close-btn:hover {
            color: var(--slate-900);
        }

        .modal-body {
            padding: 28px;
        }

        /* Notification Panel Form Styling */
        .form-group {
            margin-bottom: 18px;
            display: flex;
            flex-direction: column;
            gap: 6px;
        }
        .form-group label {
            font-size: 13px;
            font-weight: 700;
            color: var(--slate-700);
        }
        .form-control {
            font-family: 'Inter', sans-serif;
            padding: 12px 16px;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            font-size: 14px;
            color: var(--slate-900);
            outline: none;
            transition: border-color 0.2s;
        }
        .form-control:focus {
            border-color: var(--indigo-600);
        }
        .btn-broadcast {
            background-color: var(--indigo-600);
            color: white;
            padding: 12px 24px;
            border-radius: 10px;
            font-weight: 700;
            border: none;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            font-size: 13px;
            transition: background 0.2s;
            width: 100%;
            margin-top: 8px;
        }
        .btn-broadcast:hover { background-color: var(--indigo-700); }
        /* ================= NOTIFICATION BELL CUSTOM CSS ================= */
        .header-actions-wrapper {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .bell-navigation-container {
            position: relative;
            cursor: pointer;
            padding: 10px;
            border-radius: 12px;
            background-color: white;
            border: 1px solid #e2e8f0;
            color: var(--slate-700);
            box-shadow: var(--card-shadow);
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            text-decoration: none; /* Prevents underlines if enclosed in an anchor */
        }

        .bell-navigation-container:hover {
            background-color: var(--slate-900);
            color: white;
            border-color: var(--slate-900);
            transform: translateY(-1px);
        }

        .bell-navigation-container i {
            font-size: 18px;
        }

        .bell-badge-counter {
            position: absolute;
            top: -5px;
            right: -5px;
            background-color: var(--rose-600);
            color: white;
            font-size: 11px;
            font-family: 'Inter', sans-serif;
            font-weight: 700;
            border-radius: 10px;
            padding: 2px 6px;
            min-width: 18px;
            text-align: center;
            box-shadow: 0 2px 8px rgba(225, 29, 72, 0.4);
            border: 2px solid white;
            display: none; /* Auto-hidden when unread is zero */
        }
    </style>
</head>
<body>

    <div class="sidebar">
        <div class="sidebar-brand">
            <i class="fa-solid fa-graduation-cap"></i>
            <span>School Library</span>
        </div>

        <div class="menu-section-label">Core Operations</div>
        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/books" class="sidebar-link active"><i class="fa-solid fa-chart-pie"></i>Dashboard Hub</a></li>
            <li><a href="${pageContext.request.contextPath}/members" class="sidebar-link"><i class="fa-solid fa-users"></i>Manage Members</a></li>
            <li><a href="${pageContext.request.contextPath}/library/borrow" class="sidebar-link"><i class="fa-solid fa-book-open"></i>Borrow Book</a></li>
            <li><a onclick="openNotificationModal()" class="sidebar-link"><i class="fa-solid fa-bullhorn"></i>Send Broadcast</a></li>
        </ul>

        <div class="menu-section-label">Finance & Tracks</div>
        <ul class="sidebar-menu">
            <li><a href="${pageContext.request.contextPath}/fines" class="sidebar-link"><i class="fa-solid fa-wallet"></i>Fines</a></li>
            <li><a href="${pageContext.request.contextPath}/library/loans" class="sidebar-link"><i class="fa-solid fa-clock-history"></i>Borrowed Books</a></li>
            <li><a href="${pageContext.request.contextPath}/logout" class="sidebar-link logout-btn"><i class="fa-solid fa-right-from-bracket"></i>Logout</a></li>
        </ul>
    </div>

    <div class="main-workspace">

        <div class="top-navbar">
            <div>
                <h1 class="welcome-title">Welcome Back, ${username}</h1>
                <div style="font-size: 14px; color: var(--slate-400); margin-top: 4px; font-weight: 500;">Library Command & Control Center</div>
            </div>
            <div>
            <!-- Dashboard Live Routing Notification Bell Icon -->
                    <a href="${pageContext.request.contextPath}/chat.jsp" class="bell-navigation-container" title="Open Librarian Support Desk">
                        <i class="fa-solid fa-bell"></i>
                        <div class="bell-badge-counter" id="global-bell-badge">0</div>
                    </a>
            </div>
            <div class="date-badge">
                <div style="display: flex; align-items: center; gap: 6px;"><i class="fa-regular fa-calendar" style="color: var(--indigo-600)"></i> <%= request.getAttribute("formattedDate") %></div>
                <div id="clock" style="font-size: 12px; font-weight: 600; color: var(--slate-400); font-family: monospace;"></div>
            </div>
        </div>

        <div class="kpi-row">
            <div class="kpi-card">
                <div class="kpi-header"><span>Active Session</span><i class="fa-solid fa-user-group"></i></div>
                <div class="kpi-body" id="activeUsers">...</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-header"><span> Titles</span><i class="fa-solid fa-book"></i></div>
                <div class="kpi-body">${totalUniqueTitles}</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-header"><span>Borrowed Books</span><i class="fa-solid fa-handshake"></i></div>
                <div class="kpi-body">${displayBorrowedCount}</div>
            </div>
            <div class="kpi-card">
                <div class="kpi-header"><span>Available Inventory</span><i class="fa-solid fa-circle-check"></i></div>
                <div class="kpi-body" style="color: var(--emerald-600);">
                    <%
                        List<Book> checkBooks = (List<Book>) request.getAttribute("allBooks");
                        int availableSum = 0;
                        if(checkBooks != null) {
                            for(Book b : checkBooks) {
                                availableSum += b.getAvailableCopies();
                            }
                        }
                        out.print(availableSum);
                    %>
                </div>
            </div>
            <div class="kpi-card">
                <div class="kpi-header"><span>Outstanding Fines</span><i class="fa-solid fa-money-bill-wave"></i></div>
                <div class="kpi-body" style="color: var(--rose-600);">KSh <%= String.format("%.2f", request.getAttribute("totalOwed")) %></div>
            </div>
        </div>

        <div class="workbench-section">
            <div class="section-title">Quick Actions Workbench</div>
            <div class="section-desc">Instantly provision records, register student accounts, or run data evaluations.</div>
            <div class="button-group">
                <a href="${pageContext.request.contextPath}/library/addbook" class="action-button btn-add-book"><i class="fa-solid fa-plus"></i> Add New Book</a>
                <a href="${pageContext.request.contextPath}/members" class="action-button btn-add-member"><i class="fa-solid fa-user-plus"></i> View & Add Members</a>
                <a href="#analytical-dashboard" class="action-button btn-analytics"><i class="fa-solid fa-chart-line"></i> Analytics Dashboard</a>
                <a href="${pageContext.request.contextPath}/fines" class="action-button btn-fines"><i class="fa-solid fa-receipt"></i> Fines Management</a>
            </div>
        </div>

        <%
            int criticalCount = 0;
            java.util.List<String> outOfStockTitles = new java.util.ArrayList<>();

            if (checkBooks != null) {
                for (Book b : checkBooks) {
                    if (b.getAvailableCopies() <= 0) {
                        outOfStockTitles.add(b.getTitle());
                        criticalCount++;
                    } else if (b.getAvailableCopies() == 1) {
                        criticalCount++;
                    }
                }
            }

            if (criticalCount > 0) {
        %>
        <div style="background: #ffffff; border-left: 4px solid var(--amber-500); border-radius: 16px; padding: 24px; margin-bottom: 40px; display: flex; flex-direction: column; gap: 16px; box-shadow: var(--card-shadow); border-top: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0;">
            <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f1f5f9; padding-bottom: 14px;">
                <div style="display: flex; align-items: center; gap: 12px; color: #b45309;">
                    <i class="fa-solid fa-triangle-exclamation" style="font-size: 20px;"></i>
                    <div style="font-size: 15px; font-weight: 700; letter-spacing: -0.3px; color: var(--slate-900);">
                        All Copies Borrowed Books
                    </div>
                </div>
                <span style="background-color: #fff7ed; color: #c2410c; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 20px; border: 1px solid #ffedd5;">
                    <%= criticalCount %> Books
                </span>
            </div>

            <% if (!outOfStockTitles.isEmpty()) { %>
                <div style="background-color: #fafafa; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;">
                    <div style="font-size: 12px; font-weight: 700; color: var(--slate-400); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px;">
                        Completely Borrowed (0 Copies Available)
                    </div>
                    <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                        <% for (String title : outOfStockTitles) { %>
                            <div style="display: inline-flex; align-items: center; gap: 6px; background-color: #fff1f2; color: var(--rose-600); border: 1px solid #ffe4e6; padding: 6px 12px; border-radius: 8px; font-size: 13px; font-weight: 600;">
                                <i class="fa-solid fa-ban" style="font-size: 11px;"></i>
                                <%= title %>
                            </div>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </div>
        <% } %>

        <div class="catalog-table-container">
            <div class="table-header-bar">
                <div class="section-title" style="margin: 0;">Active Catalog Inventory</div>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Book Details</th>
                        <th>Inventory Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<Book> books = (List<Book>) request.getAttribute("allBooks");
                        if(books != null) {
                            for(Book b : books) {
                                boolean avail = b.getAvailableCopies() > 0;
                                String cover = (b.getImageUrl() != null && !b.getImageUrl().isEmpty()) ? b.getImageUrl() : "https://via.placeholder.com/300x450?text=No+Cover";
                                String d = (b.getDescription() != null) ? b.getDescription() : "No description provided.";
                    %>
                    <tr>
                        <td>
                            <div class="book-meta">
                                <img src="<%= cover %>" alt="Cover" class="table-book-cover">
                                <div>
                                    <div class="table-book-title"><%= b.getTitle() %></div>
                                    <div class="table-book-desc"><%= d %></div>
                                </div>
                            </div>
                        </td>
                        <td>
                            <% if(avail) { %>
                                <span class="status-badge status-available"><i class="fa-solid fa-circle"></i> <%= b.getAvailableCopies() %> Copies Left</span>
                            <% } else { %>
                                <span class="status-badge status-empty"><i class="fa-solid fa-circle-xmark"></i> All copies Borrowed</span>
                            <% } %>
                        </td>
                        <td>
                            <div class="table-actions">
                                <a href="${pageContext.request.contextPath}/edit-book?id=<%= b.getId() %>" class="action-link action-edit"><i class="fa-regular fa-pen-to-square"></i> Edit</a>
                                <a href="${pageContext.request.contextPath}/delete-book?id=<%= b.getId() %>" class="action-link action-delete" onclick="return confirm('Confirm permanent deletion of this book record?');"><i class="fa-regular fa-trash-can"></i> Delete</a>
                            </div>
                        </td>
                    </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
            </table>
        </div>

        <div class="analytics-grid-row" style="margin-bottom: 40px;">

            <div class="card col-lg-7">
                <div class="card-header">
                    <span><i class="fa-solid fa-clock-history" style="color: var(--blue-600); margin-right: 8px;"></i>Live Borrowing Matrix By User</span>
                    <span style="font-size: 11px; font-weight: 700; background: #eff6ff; color: #1d4ed8; padding: 4px 10px; border-radius: 20px;">System Active</span>
                </div>
                <div class="card-body" style="padding: 0; max-height: 400px; overflow-y: auto;">
                    <table>
                        <thead style="position: sticky; top: 0; z-index: 10;">
                            <tr>
                                <th style="padding: 12px 24px;">User / Member</th>
                                <th style="padding: 12px 24px;">Book Title</th>
                                <th style="padding: 12px 24px;">Due Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                Map<String, List<app.model.BorrowedBook>> userLoans = (Map<String, List<app.model.BorrowedBook>>) request.getAttribute("userLoans");
                                if (userLoans == null || userLoans.isEmpty()) {
                            %>
                                <tr>
                                    <td colspan="3" style="text-align: center; padding: 40px; color: var(--slate-400); font-weight: 600;">
                                        <i class="fa-solid fa-folder-open" style="font-size: 24px; display: block; margin-bottom: 8px;"></i>
                                        No active borrowing accounts found.
                                    </td>
                                </tr>
                            <%
                                } else {
                                    for (Map.Entry<String, List<app.model.BorrowedBook>> entry : userLoans.entrySet()) {
                                        String borrower = entry.getKey();
                                        for (app.model.BorrowedBook loan : entry.getValue()) {
                            %>
                                <tr>
                                    <td style="padding: 14px 24px; font-weight: 700; color: var(--slate-900);"><i class="fa-regular fa-user" style="margin-right: 8px; color: var(--slate-400)"></i><%= borrower %></td>
                                    <td style="padding: 14px 24px; font-weight: 600;"><%= loan.getBook() != null ? loan.getBook().getTitle() : "Unknown Book" %></td>
                                    <td style="padding: 14px 24px; font-family: monospace; font-weight: 700; color: var(--slate-600);"><%= loan.getDueDate() != null ? loan.getDueDate().toString().substring(0, 10) : "N/A" %></td>
                                </tr>
                            <%
                                        }
                                    }
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="card col-lg-5">
                <div class="card-header">
                    <span><i class="fa-solid fa-wallet" style="color: var(--rose-600); margin-right: 8px;"></i>Outstanding Debts Ledger</span>
                    <span style="font-size: 11px; font-weight: 700; background: #fff1f2; color: #e11d48; padding: 4px 10px; border-radius: 20px;">Fines Issued</span>
                </div>
                <div class="card-body" style="padding: 0; max-height: 400px; overflow-y: auto;">
                    <table>
                        <thead style="position: sticky; top: 0; z-index: 10;">
                            <tr>
                                <th style="padding: 12px 24px;">Debtor</th>
                                <th style="padding: 12px 24px; text-align: right;">Total Arrears</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                Map<String, List<app.model.Fine>> userHistory = (Map<String, List<app.model.Fine>>) request.getAttribute("userHistory");
                                if (userHistory == null || userHistory.isEmpty()) {
                            %>
                                <tr>
                                    <td colspan="2" style="text-align: center; padding: 40px; color: var(--slate-400); font-weight: 600;">
                                        <i class="fa-solid fa-circle-check" style="font-size: 24px; display: block; margin-bottom: 8px; color: var(--emerald-600);"></i>
                                        System clear! No outstanding balances.
                                    </td>
                                </tr>
                            <%
                                } else {
                                    for (Map.Entry<String, List<app.model.Fine>> entry : userHistory.entrySet()) {
                                        String debtor = entry.getKey();
                                        double totalUserDebt = 0.0;
                                        for (app.model.Fine f : entry.getValue()) {
                                            totalUserDebt += f.getAmount();
                                        }
                            %>
                                <tr>
                                    <td style="padding: 14px 24px; font-weight: 700; color: var(--slate-900);"><%= debtor %></td>
                                    <td style="padding: 14px 24px; text-align: right; font-weight: 800; color: var(--rose-600); font-family: monospace;">KSh <%= String.format("%.2f", totalUserDebt) %></td>
                                </tr>
                            <%
                                    }
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div id="analytical-dashboard" class="section-title" style="margin-top: 45px; margin-bottom: 5px;">Library Intelligence Canvas</div>
        <div style="font-size: 14px; color: var(--slate-400); margin-bottom: 25px; font-weight: 500;">Administrative monitoring and real-time operational analytics insights.</div>

        <div class="analytics-grid-row">
            <div class="card col-12">
                <div class="card-header">
                    <span>Revenue & Fine Accumulation (7-Day Trend)</span>
                    <i class="fa-solid fa-chart-line text-muted" style="color: var(--slate-400)"></i>
                </div>
                <div class="card-body">
                    <div class="chart-container">
                        <canvas id="debtTrendChart"></canvas>
                    </div>
                </div>
            </div>

            <div class="card col-lg-7">
                <div class="card-header">
                    <span>Top Borrowed Titles</span>
                    <i class="fa-solid fa-ranking-star" style="color: var(--slate-400)"></i>
                </div>
                <div class="card-body">
                    <div class="chart-container">
                        <canvas id="topBooksChart"></canvas>
                    </div>
                </div>
            </div>

            <div class="card col-lg-5">
                <div class="card-header">
                    <span>Distribution Profile</span>
                    <i class="fa-solid fa-pie-chart" style="color: var(--slate-400)"></i>
                </div>
                <div class="card-body">
                    <div class="chart-container">
                        <canvas id="userActivityChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div class="modal-overlay <% if(request.getAttribute("broadcastSuccess") != null) { out.print("active"); } %>" id="notificationModal" onclick="closeNotificationModal(event)">
        <div class="modal-wrapper">
            <div class="modal-header">
                <div>
                    <div class="section-title" style="margin: 0; font-size: 18px;">Broadcast Notification</div>
                    <div style="font-size: 13px; color: var(--slate-400); margin-top: 2px;">Dispatch system updates directly to all library users</div>
                </div>
                <button type="button" class="modal-close-btn" onclick="toggleModalState(false)">&times;</button>
            </div>
            <div class="modal-body">
                <% if(request.getAttribute("broadcastSuccess") != null) { %>
                    <div style="background-color: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; padding: 12px 16px; border-radius: 10px; margin-bottom: 20px; font-size: 13px; font-weight: 600;">
                        <i class="fa-solid fa-circle-check"></i> <%= request.getAttribute("broadcastSuccess") %>
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/library/broadcast" method="POST" onsubmit="document.getElementById('submitBroadcastBtn').innerHTML = '<i class=\'fa-solid fa-spinner fa-spin\'></i> Dispatching Emails...';">
                    <div class="form-group">
                        <label for="broadcastSubject">Message Subject</label>
                        <input type="text" id="broadcastSubject" name="subject" class="form-control" placeholder="e.g., Notice: Delayed Book Returns Policy Update" required>
                    </div>
                    <div class="form-group">
                        <label for="broadcastMessage">Message Content</label>
                        <textarea id="broadcastMessage" name="message" class="form-control" rows="5" placeholder="Type your formal system notice details here..." required></textarea>
                    </div>
                    <button type="submit" id="submitBroadcastBtn" class="btn-broadcast">
                        <i class="fa-solid fa-paper-plane"></i> Send Message
                    </button>
                </form>
            </div>
        </div>
    </div>

<script>
    // --- UTILITIES & CLOCK ---
    function updateClock() {
        const now = new Date();
        document.getElementById('clock').innerHTML = now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit', second:'2-digit'});
    }
    setInterval(updateClock, 1000); updateClock();

    function loadUsers() {
      fetch('active-users').then(r => r.text()).then(d => {
        if (d && d.length < 10) document.getElementById('activeUsers').innerText = d;
      }).catch(() => {});
    }
    setInterval(loadUsers, 5000); loadUsers();

    // --- MODAL LOGIC ---
    function toggleModalState(isOpen) {
        const modal = document.getElementById('notificationModal');
        if (isOpen) { modal.classList.add('active'); }
        else { modal.classList.remove('active'); }
    }

    function openNotificationModal() { toggleModalState(true); }

    function closeNotificationModal(e) {
        if (e.target.id === 'notificationModal') { toggleModalState(false); }
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') { toggleModalState(false); }
    });

    // --- CHART.JS GLOBAL CONFIG ---
    Chart.defaults.font.family = "'Plus Jakarta Sans', 'Inter', sans-serif";
    Chart.defaults.font.weight = '600';
    Chart.defaults.color = '#94a3b8';

    // --- CHART 1: REVENUE TREND (7-Day Fine Trend) ---
    const trendLabels = [];
    const trendData = [];
    <%
        // Expecting a Map<String, Double> where key is date string/day and value is total fine amount
        Map<String, Double> fineTrend = (Map<String, Double>) request.getAttribute("fineTrend");
        if (fineTrend != null && !fineTrend.isEmpty()) {
            for (Map.Entry<String, Double> entry : fineTrend.entrySet()) {
    %>
                trendLabels.push("<%= entry.getKey().replace("\"", "\\\"") %>");
                trendData.push(<%= entry.getValue() %>);
    <%
            }
        } else {
    %>
            trendLabels.push('6 Days Ago', '5 Days Ago', '4 Days Ago', '3 Days Ago', '2 Days Ago', 'Yesterday', 'Today');
            trendData.push(0, 0, 0, 0, 0, 0, 0);
    <%
        }
    %>

    const trendCtx = document.getElementById('debtTrendChart').getContext('2d');
    new Chart(trendCtx, {
        type: 'line',
        data: {
            labels: trendLabels,
            datasets: [{
                label: 'Fines Accumulated (KSh)',
                data: trendData,
                borderColor: '#e11d48',
                backgroundColor: 'rgba(225, 29, 72, 0.05)',
                fill: true,
                tension: 0.4,
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: '#f1f5f9' } },
                x: { grid: { display: false } }
            }
        }
    });

    // --- CHART 2: TOP BORROWED TITLES ---
    const topBookLabels = [];
    const topBookCounts = [];
    <%
        // Expecting a Map<String, Integer> tracking top borrowed book records
        Map<String, Integer> topBooks = (Map<String, Integer>) request.getAttribute("topBooks");
        if (topBooks != null && !topBooks.isEmpty()) {
            for (Map.Entry<String, Integer> entry : topBooks.entrySet()) {
    %>
                topBookLabels.push("<%= entry.getKey().replace("\"", "\\\"") %>");
                topBookCounts.push(<%= entry.getValue() %>);
    <%
            }
        } else {
    %>
            topBookLabels.push('No Active Records');
            topBookCounts.push(0);
    <%
        }
    %>

    const topCtx = document.getElementById('topBooksChart').getContext('2d');
    new Chart(topCtx, {
        type: 'bar',
        data: {
            labels: topBookLabels,
            datasets: [{
                data: topBookCounts,
                backgroundColor: '#4f46e5',
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f1f5f9' } },
                x: { grid: { display: false } }
            }
        }
    });

    // --- CHART 3: DISTRIBUTION PROFILE (Inventory vs. Borrowed) ---
    <%
        // Dynamic calculations from scope variables matching your card count configurations
        Object displayBorrowedAttr = request.getAttribute("displayBorrowedCount");
        int totalBorrowedCount = 0;
        if (displayBorrowedAttr instanceof Number) {
            totalBorrowedCount = ((Number) displayBorrowedAttr).intValue();
        } else if (displayBorrowedAttr instanceof String) {
            try { totalBorrowedCount = Integer.parseInt((String) displayBorrowedAttr); } catch(Exception e){}
        }

        List<app.model.Book> dynamicBooks = (List<app.model.Book>) request.getAttribute("allBooks");
        int totalAvailableInventory = 0;
        if (dynamicBooks != null) {
            for (app.model.Book b : dynamicBooks) {
                totalAvailableInventory += b.getAvailableCopies();
            }
        }
    %>

    const userCtx = document.getElementById('userActivityChart').getContext('2d');
    new Chart(userCtx, {
        type: 'doughnut',
        data: {
            labels: ['Active Loans', 'Inventory Stocked'],
            datasets: [{
                data: [<%= totalBorrowedCount %>, <%= totalAvailableInventory %>],
                backgroundColor: ['#2563eb', '#10b981'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { boxWidth: 12, padding: 20 }
                }
            }
        }
    });
</script>
<script>
    let notificationWs;
    let globalUnreadCount = 0;

    function initNotificationEngine() {
        const loc = window.location;
        const protocol = (loc.protocol === "https:") ? "wss://" : "ws://";

        // Connects directly to your existing WebSocket endpoint with the Librarian role
        const wsUrl = protocol + loc.host + "${pageContext.request.contextPath}/websocket/chat?role=librarian";

        notificationWs = new WebSocket(wsUrl);

        notificationWs.onmessage = (event) => {
            if (event.data === "ping" || event.data === "pong") return;

            try {
                const data = JSON.parse(event.data);

                // Ignore core internal status messages or broadcasts
                if (data.system === "userList" || data.system === "disconnect") {
                    return;
                }

                // Increment counts only for incoming client text updates
                if (data.sender !== "Librarian") {
                    globalUnreadCount++;
                    updateDashboardBellUI();
                }
            } catch (err) {
                console.error("Notification parsing engine failure:", err);
            }
        };

        notificationWs.onclose = () => {
            // Silently retry background handshakes if dropped
            setTimeout(initNotificationEngine, 5000);
        };
    }

    function updateDashboardBellUI() {
        const badgeElement = document.getElementById("global-bell-badge");
        if (!badgeElement) return;

        if (globalUnreadCount > 0) {
            badgeElement.innerText = globalUnreadCount > 99 ? "99+" : globalUnreadCount;
            badgeElement.style.display = "block";
        } else {
            badgeElement.style.display = "none";
        }
    }

    // Fire background loop safe initialization alongside existing document hooks
    document.addEventListener("DOMContentLoaded", () => {
        initNotificationEngine();

        // Simple live running workspace clock logic
        setInterval(() => {
            const now = new Date();
            const clockEl = document.getElementById("clock");
            if(clockEl) {
                clockEl.innerText = now.toLocaleTimeString();
            }
        }, 1000);
    });
</script>
</body>
</html>