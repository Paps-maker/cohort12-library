<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="app.pages.About, app.pages.Books, java.util.List" %>
<!DOCTYPE html>

<html>
<head>
    <title>Library Management System</title>

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6f8;
        }

        header {
            background: #2c3e50;
            color: white;
            padding: 20px;
            text-align: center;
        }

        nav {
            background: #34495e;
            padding: 10px;
            text-align: center;
        }

        nav a {
            color: white;
            margin: 0 15px;
            text-decoration: none;
            font-weight: bold;
        }

        .hero {
            padding: 60px;
            text-align: center;
            background: linear-gradient(to right, #3498db, #2c3e50);
            color: white;
        }

        .hero h1 {
            font-size: 40px;
        }

        .btn {
            background: #e67e22;
            color: white;
            padding: 12px 25px;
            text-decoration: none;
            border-radius: 5px;
            display: inline-block;
            margin-top: 20px;
        }

        .section {
            padding: 40px;
            text-align: center;
        }

        .cards {
            display: flex;
            justify-content: center;
            gap: 20px;
            flex-wrap: wrap;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            width: 250px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }

        footer {
            background: #2c3e50;
            color: white;
            text-align: center;
            padding: 15px;
        }
        .card:hover {
            transform: translateY(-5px);
            transition: 0.3s;
        }
        .card h3 {
            color: #2c3e50;
        }

        .cards {
            margin-top: 20px;
        }
        .cards {
            display: flex;
            flex-direction: row;
            gap: 20px;
            justify-content: center;
            flex-wrap: wrap;
        }

        .card {
            min-width: 200px;
            max-width: 220px;
            flex: 0 0 auto;
        }

        .step-number {
            font-weight: bold;
            color: #3498db;
            margin-bottom: 10px;
        }
        .time-card {
            background: linear-gradient(135deg, #2c3e50, #3498db);
            color: white;
            padding: 25px;
            border-radius: 12px;
            width: fit-content;
            margin: 20px auto;
            text-align: center;
            box-shadow: 0 8px 20px rgba(0,0,0,0.2);
        }

        .time-title {
            font-size: 14px;
            margin-bottom: 8px;
            opacity: 0.9;
        }

        .time-value {
            font-size: 20px;
            font-weight: bold;
        }
        footer {
            background: #2c3e50;
            color: white;
            margin-top: 40px;
            padding: 20px 0;
        }

        .footer-container {
            max-width: 1000px;
            margin: auto;
            text-align: center;
        }

        .footer-title {
            margin-bottom: 10px;
            font-size: 14px;
            color: #ccc;
        }

        .footer-links a {
            margin: 0 10px;
            text-decoration: none;
            color: #f1c40f;
            font-weight: bold;
            transition: 0.3s;
        }

        .footer-links a:hover {
            color: white;
        }
    </style>

</head>

<body>
<%-- Header--%>

<header>

    <h2> School Library </h2>

<%@ page import="java.text.SimpleDateFormat, java.util.Date" %>

<%!
    // Declaration
    public String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        return sdf.format(new Date());
    }
%>

<div class="time-card">
    <div class="time-title">Date & Time</div>
    <div id="clock" class="time-value"></div>
</div>
<%-- java script to update time--%>
<script>
function updateClock() {
    const now = new Date();

    const options = {
        year: 'numeric',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    };

    document.getElementById("clock").innerHTML =
        now.toLocaleString('en-US', options);
}

// update every second
setInterval(updateClock, 1000);
updateClock();
</script>

</header>

<nav>
    <a href="#">Home</a>
    <a href="#about">About</a>
    <a href="#books">Books</a>
    <a href="login">Login</a>
     <a href="register.jsp"> Register Now</a>
</nav>

<div class="hero">
    <h1>Welcome to Our Digital Library</h1>
    <p>Explore, Borrow, and Manage Books Easily</p>
    <a href="login" class="btn">Login to Continue</a>
</div>

<!-- ABOUT -->
<div class="section" id="about">
    <h2>About Our Library</h2>

    <!-- Description Card -->
    <div class="card" style="max-width:600px; margin:auto;">
        <p><%= About.getDescription() %></p>

        <p style="margin-top:10px; color:#555;">
             Only registered users can borrow books. Admin users manage members and system activities.
        </p>
    </div>

    <!-- Steps Section -->
    <h3 style="margin-top:30px;"> How to Borrow</h3>

    <div class="cards">
        <%
            List<String> steps = About.getSteps();
            int i = 1;
            for(String step : steps){
        %>
            <div class="card">
                <div class="step-number">Step <%= i++ %></div>
                <p><%= step %></p>
            </div>
        <%
            }
        %>
    </div>
</div>

<!-- BOOK CATEGORIES -->
<div class="section" id="books">
    <h2>Our Book Categories</h2>

    <div class="cards">
        <%
            List<String> categories = Books.getCategories();
            for(String c : categories){
        %>
            <div class="card">
                <h3><%= c %></h3>
                <p>Explore books under <%= c %> category</p>
            </div>
        <%
            }
        %>
    </div>
</div>
<jsp:include page="footer.jsp"/>

</body>
</html>