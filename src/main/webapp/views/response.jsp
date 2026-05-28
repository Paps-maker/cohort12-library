<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String title = (String) request.getAttribute("title");
    String message = (String) request.getAttribute("message");
    Boolean isSuccess = (Boolean) request.getAttribute("isSuccess");
    String color = (isSuccess != null && isSuccess) ? "#059669" : "#ef4444";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght=400;600;800&display=swap');
        body{font-family:'Plus Jakarta Sans', sans-serif; display:flex; justify-content:center; align-items:center; height:100vh; background:#f8fafc; margin:0;}
        .card{background:white; padding:40px; border-radius:24px; text-align:center; box-shadow:0 20px 25px -5px rgba(0,0,0,0.1); max-width:400px; border: 1px solid #e2e8f0;}
        h2{color: <%= color %>; margin-top:0; font-weight:800;}
        p{color:#475569; line-height:1.6;}
        .btn{display:inline-block; margin-top:20px; padding:14px 28px; background:#0f172a; color:white; text-decoration:none; border-radius:12px; font-weight:700;}
    </style>
</head>
<body>
<div class="card">
    <h2><%= title %></h2>
    <p><%= message %></p>
    <a href="<%= request.getContextPath() %>/books" class="btn">Continue to Dashboard</a>
</div>
</body>
</html>