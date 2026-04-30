<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="app.pages.About, app.pages.Books, java.util.List, java.util.Date" %>

<%-- Taglib directives for the JSTL "Big Three" --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html>
<head>
    <title>Library Management System</title>
    <style>
        body { margin: 0; font-family: 'Segoe UI', Arial, sans-serif; background: #f4f6f8; }
        header { background: #2c3e50; color: white; padding: 20px; text-align: center; }
        nav { background: #34495e; padding: 10px; text-align: center; }
        nav a { color: white; margin: 0 15px; text-decoration: none; font-weight: bold; }

        /* EL-driven highlight class */
        .active { color: #f1c40f !important; }

        .hero { padding: 60px; text-align: center; background: linear-gradient(to right, #3498db, #2c3e50); color: white; }
        .hero h1 { font-size: 40px; }
        .btn { background: #e67e22; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px; transition: 0.3s; }
        .btn:hover { background: #d35400; }

        .section { padding: 40px; text-align: center; }
        .cards { display: flex; flex-direction: row; gap: 20px; justify-content: center; flex-wrap: wrap; margin-top: 20px; }
        .card { background: white; padding: 20px; border-radius: 10px; min-width: 200px; max-width: 220px; box-shadow: 0 0 10px rgba(0,0,0,0.1); transition: 0.3s; }
        .card:hover { transform: translateY(-5px); }
        .card h3 { color: #2c3e50; margin-bottom: 5px; }
        .step-number { font-weight: bold; color: #3498db; margin-bottom: 10px; }

        .time-card { background: linear-gradient(135deg, #2c3e50, #3498db); color: white; padding: 20px; border-radius: 12px; width: fit-content; margin: 10px auto; text-align: center; box-shadow: 0 8px 20px rgba(0,0,0,0.2); }
        .time-value { font-size: 18px; font-weight: bold; }
/* FOOTER STYLES */
footer {
    background: #2c3e50;
    color: white;
    margin-top: 40px;
    padding: 40px 0;
    text-align: center;
}

.footer-container {
    max-width: 1100px;
    margin: 0 auto;
    padding: 0 20px;
}

.footer-title {
    margin-bottom: 20px;
    font-size: 14px;
    color: #bdc3c7;
    text-transform: uppercase;
    letter-spacing: 2px;
    font-weight: 600;
}

/* Container for the link buttons */
.footer-links {
    display: flex;
    justify-content: center;
    gap: 15px;
    flex-wrap: wrap;
    margin-bottom: 25px;
}

/* Styling links as "Ghost Buttons" */
.footer-links a {
    text-decoration: none;
    color: #f1c40f; /* Gold color */
    font-weight: bold;
    padding: 10px 20px;
    border: 2px solid #f1c40f;
    border-radius: 6px;
    transition: all 0.3s ease;
    font-size: 14px;
}

/* Hover effect for buttons */
.footer-links a:hover {
    background: #f1c40f;
    color: #2c3e50;
    transform: translateY(-3px);
    box-shadow: 0 4px 10px rgba(241, 196, 15, 0.3);
}

/* Specialized color for the Register button in footer */
.footer-links a[href*="register"] {
    color: #e67e22;
    border-color: #e67e22;
}

.footer-links a[href*="register"]:hover {
    background: #e67e22;
    color: white;
    box-shadow: 0 4px 10px rgba(230, 126, 34, 0.3);
}

.copyright-text {
    margin-top: 20px;
    font-size: 12px;
    color: #95a5a6;
    border-top: 1px solid rgba(255,255,255,0.1);
    padding-top: 20px;
}

        .badge { background: #e67e22; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; margin-left: 10px; vertical-align: middle; }
    </style>
</head>

<body>

<header>
    <h2> School Library </h2>
    <div class="time-card">
        <%-- Using FMT to show the Server-Side Date --%>
        <c:set var="now" value="<%= new java.util.Date() %>" />
        <div class="time-title"> Date</div>
        <div class="time-value">
            <fmt:formatDate value="${now}" pattern="EEEE, MMM dd, yyyy" />
        </div>
        <%-- JavaScript clock stays for real-time updates --%>
        <div id="clock" style="font-size: 14px; opacity: 0.8; margin-top: 5px;"></div>
    </div>
</header>

<nav>
    <a href="index.jsp" class="${empty param.view or param.view eq 'home' ? 'active' : ''}">Home</a>
    <a href="#about">About</a>
    <a href="#books">Books</a>
    <a href="login">Login</a>
    <a href="register.jsp">Register Now</a>
</nav>

<div class="hero">
    <%-- Logic to determine greeting --%>
    <c:set var="hour" value="<%= new java.util.GregorianCalendar().get(java.util.Calendar.HOUR_OF_DAY) %>" />
    <h1>
        ${hour lt 12 ? "Good Morning" : (hour lt 17 ? "Good Afternoon" : "Good Evening")},
        Welcome to Our Digital Library
    </h1>
    <p>Explore, Borrow, and Manage Books Easily</p>
    <a href="login" class="btn">
        ${param.status eq 'guest' ? 'Join Us Now' : 'Login to Continue'}
    </a>
</div>

<div class="section" id="about">
    <h2>About Our Library</h2>
    <div class="card" style="max-width:600px; margin:auto;">
        <p><%= About.getDescription() %></p>
        <p style="margin-top:10px; color:#555;">
             Only registered users can borrow books. Admin users manage members and system activities.
        </p>
    </div>

    <h3 style="margin-top:30px;">How to Borrow</h3>
    <div class="cards">
        <c:forEach var="step" items="<%= About.getSteps() %>" varStatus="status">
            <div class="card">
                <div class="step-number">Step ${status.count}</div>
                <p>${step}</p>
            </div>
        </c:forEach>
    </div>
</div>

<div class="section" id="books">
    <c:set var="cats" value="<%= Books.getCategories() %>" />

    <h2>
        Our Book Categories
        <%-- Using fn:length to count total items in the list --%>
        <c:if test="${fn:length(cats) ge 5}">
            <span class="badge">WIDE VARIETY: ${fn:length(cats)} SECTIONS</span>
        </c:if>
    </h2>

    <div class="cards">
        <c:choose>
            <c:when test="${not empty cats}">
                <c:forEach var="category" items="${cats}">
                    <div class="card" style="border-top: 4px solid
                        ${category eq 'Fiction' ? '#e67e22' :
                          category eq 'Technology' ? '#3498db' :
                          category eq 'Education' ? '#27ae60' :
                          category eq 'Kids' ? '#9b59b6' :
                          category eq 'Story' ? '#f1c40f' :
                          category eq 'History' ? '#e74c3c' : '#7f8c8d'}">

                        <%-- Using fn:toUpperCase to make titles bold and clean --%>
                        <h3>${fn:toUpperCase(category)}</h3>
                        <p>Explore books under ${category} category</p>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="card" style="width: 100%; max-width: 400px; border-top: 4px solid #ccc;">
                    <h3>Notice</h3>
                    <p>No categories found. Please check back later!</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>


<jsp:include page="footer.jsp"/>

<script>
    function updateClock() {
        const now = new Date();
        const options = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true };
        document.getElementById("clock").innerHTML = now.toLocaleTimeString('en-US', options);
    }
    setInterval(updateClock, 1000);
    updateClock();
</script>

</body>
</html>