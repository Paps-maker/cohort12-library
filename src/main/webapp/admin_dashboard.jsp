<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Library Intelligence | Admin Dashboard</title>

    <!-- Professional Assets -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        :root {
            --primary: #4f46e5;
            --primary-light: #eef2ff;
            --success: #10b981;
            --success-light: #ecfdf5;
            --danger: #ef4444;
            --danger-light: #fef2f2;
            --warning: #f59e0b;
            --warning-light: #fffbeb;
            --bg: #f9fafb;
            --text-main: #111827;
            --text-muted: #6b7280;
            --card-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg);
            color: var(--text-main);
            letter-spacing: -0.01em;
        }

        .btn-back {
            background: white;
            color: var(--text-main);
            border: 1px solid #e5e7eb;
            padding: 0.6rem 1.2rem;
            border-radius: 8px;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            font-weight: 600;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
        }
        .btn-back:hover {
            background: #f3f4f6;
            transform: translateX(-4px);
            box-shadow: 0 4px 6px rgba(0,0,0,0.05);
        }

        .kpi-card {
            background: white;
            border-radius: 16px;
            padding: 1.5rem;
            box-shadow: var(--card-shadow);
            border: 1px solid #f3f4f6;
            height: 100%;
        }
        .kpi-icon {
            width: 44px; height: 44px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.2rem; margin-bottom: 1.25rem;
        }

        .card {
            border-radius: 20px;
            border: 1px solid #e5e7eb;
            box-shadow: var(--card-shadow);
            overflow: hidden;
        }
        .card-header {
            background-color: white;
            border-bottom: 1px solid #f3f4f6;
            padding: 1.5rem;
            font-weight: 700;
            font-size: 1.1rem;
        }

        .chart-container { position: relative; height: 320px; width: 100%; }

        .status-badge {
            padding: 0.6rem 1.2rem;
            border-radius: 50px;
            font-size: 0.85rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
    </style>
</head>
<body>

<div class="container py-5">
    <!-- Header Section -->
    <div class="row mb-5 align-items-center">
        <div class="col-lg-8">
            <a href="${pageContext.request.contextPath}/books" class="btn-back mb-4">
                <i class="fa-solid fa-chevron-left me-2"></i> Return to Books
            </a>
            <h1 class="display-6 fw-bold mb-2">Library Intelligence Engine</h1>
            <p class="text-muted fs-5">Administrative monitoring and real-time operational analytics.</p>
        </div>
        <div class="col-lg-4 text-lg-end">
            <span class="status-badge bg-success-light text-success border border-success-subtle">
                <i class="fa-solid fa-circle-check me-2"></i> System: Online
            </span>
        </div>
    </div>

    <!-- KPI Cards Row -->
    <div class="row g-4 mb-5">
        <div class="col-md-6 col-lg-3">
            <div class="kpi-card">
                <div class="kpi-icon bg-success-light text-success"><i class="fa-solid fa-receipt"></i></div>
                <div class="text-muted small fw-bold text-uppercase">Total Revenue</div>
                <div class="h3 fw-bold m-0 mt-1">KSH ${totalRevenue}</div>
            </div>
        </div>

        <div class="col-md-6 col-lg-3">
            <div class="kpi-card">
                <div class="kpi-icon bg-primary-light text-primary"><i class="fa-solid fa-vault"></i></div>
                <div class="text-muted small fw-bold text-uppercase">Global Inventory</div>
                <div class="h3 fw-bold m-0 mt-1">${totalBooks} <span class="fs-6 fw-normal text-muted">Copies</span></div>
            </div>
        </div>
        <div class="col-md-6 col-lg-3">
            <div class="kpi-card">
                <div class="kpi-icon bg-warning-light text-warning"><i class="fa-solid fa-hourglass-half"></i></div>
                <div class="text-muted small fw-bold text-uppercase">Active Overdue</div>
                <div class="h3 fw-bold m-0 mt-1">${overdueCount} <span class="fs-6 fw-normal text-muted">Items</span></div>
            </div>
        </div>
    </div>

    <!-- Main Visuals Section -->
    <div class="row g-4">
        <div class="col-12">
            <div class="card p-2">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Revenue & Fine Accumulation (7-Day Trend)</span>
                    <i class="fa-solid fa-chart-line text-muted"></i>
                </div>
                <div class="card-body pt-0">
                    <div class="chart-container">
                        <canvas id="debtTrendChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-7">
            <div class="card h-100 p-2">
                <div class="card-header border-0">Top Borrowed Titles</div>
                <div class="card-body pt-0">
                    <div class="chart-container">
                        <canvas id="topBooksChart"></canvas>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-5">
            <div class="card h-100 p-2">
                <div class="card-header border-0">Most active users</div>
                <div class="card-body pt-0">
                    <div class="chart-container">
                        <canvas id="userActivityChart"></canvas>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = '#9ca3af';

    <%
        Map<String, Double> dt = (Map)request.getAttribute("debtTrend");
        // Fallback: If map is null or empty, provide a "Today" data point so chart renders
        if (dt == null || dt.isEmpty()) {
            dt = new java.util.LinkedHashMap<>();
            dt.put("No Data Today", 0.0);
        }
    %>

    new Chart(document.getElementById('debtTrendChart'), {
        type: 'line',
        data: {
            // ✅ Improved label output
            labels: [<% for(String d : dt.keySet()) { out.print("'" + d + "',"); } %>],
            datasets: [{
                label: 'Fine Activity (KSH)',
                data: [<% for(Double v : dt.values()) { out.print(v + ","); } %>],
                borderColor: '#ef4444', // Red 500
                borderWidth: 3,
                backgroundColor: 'rgba(239, 68, 68, 0.1)', // Slightly more visible fill
                fill: true,
                tension: 0.4,
                pointRadius: 6,
                pointHoverRadius: 8,
                pointBackgroundColor: '#ef4444',
                pointBorderColor: '#fff',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true, position: 'top' },
                tooltip: {
                    backgroundColor: '#1f2937',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { borderDash: [5, 5], color: '#e5e7eb' },
                    ticks: { callback: function(value) { return 'KSH ' + value; } }
                },
                x: { grid: { display: false } }
            }
        }
    });

    // 2. Bar Chart: Top Books
    <%
        Map<String, Integer> tb = (Map)request.getAttribute("topBooks");
        if (tb == null) tb = new java.util.LinkedHashMap<>();
    %>
    new Chart(document.getElementById('topBooksChart'), {
        type: 'bar',
        data: {
            labels: [<% for(String title : tb.keySet()) { out.print("'" + title + "',"); } %>],
            datasets: [{
                label: 'Total Borrows',
                data: [<% for(Integer count : tb.values()) { out.print(count + ","); } %>],
                backgroundColor: '#4f46e5',
                hoverBackgroundColor: '#4338ca',
                borderRadius: 12
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { grid: { borderDash: [5, 5], color: '#f3f4f6' } },
                x: { grid: { display: false } }
            }
        }
    });

    // 3. Doughnut Chart: Active Users
    <%
        Map<String, Integer> au = (Map)request.getAttribute("activeUsers");
        if (au == null) au = new java.util.LinkedHashMap<>();
    %>
    new Chart(document.getElementById('userActivityChart'), {
        type: 'doughnut',
        data: {
            labels: [<% for(String user : au.keySet()) { out.print("'" + user + "',"); } %>],
            datasets: [{
                data: [<% for(Integer total : au.values()) { out.print(total + ","); } %>],
                backgroundColor: ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
                borderWidth: 5,
                borderColor: '#ffffff',
                hoverOffset: 15
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { usePointStyle: true, padding: 30, font: { weight: 600 } } }
            },
            cutout: '75%'
        }
    });
</script>
</body>
</html>