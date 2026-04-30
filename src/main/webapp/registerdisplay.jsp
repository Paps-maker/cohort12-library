<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registration Status</title>

    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f4f6f8;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }

        .box {
            background: white;
            padding: 35px;
            border-radius: 12px;
            text-align: center;
            width: 420px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.05);
        }

        h2.success { color: #27ae60; margin-top: 0; }
        h2.error { color: #e74c3c; margin-top: 0; }

        p { color: #4b5563; line-height: 1.6; font-size: 15px; }

        .info-box {
            background: #f8fafc;
            padding: 15px;
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            margin: 15px 0;
            text-align: left;
        }

        a {
            display: inline-block;
            margin-top: 20px;
            padding: 12px 25px;
            background: #2a5298;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            transition: background 0.3s;
        }

        a:hover {
            background: #1e3c72;
        }
    </style>
</head>

<body>

<div class="box">

    <%
        String status = (String) request.getAttribute("status");
        String email = (String) request.getAttribute("email");
        if (email == null) email = request.getParameter("email");

        if ("success".equals(status)) {
    %>
        <h2 class="success">✅ Registration Successful</h2>

                <p>Account created successfully! Please login with your credentials below:</p>

                <div class="info-box">
                    <p style="margin: 5px 0;"><strong>Username:</strong> <%= request.getAttribute("username") %></p>
                    <p style="margin: 5px 0;"><strong>Password:</strong> <%= request.getParameter("password") %></p>
                </div>

                <p style="font-size: 13px; color: #64748b;">
                    Your account has been verified against the official school records using
                    <strong><%= email %></strong>.
                </p>

                <a href="index.jsp">Go to Login</a>

    <% } else if ("not_authorized".equals(status)) { %>
        <h2 class="error">❌ Access Denied</h2>
        <p>The email <strong><%= email %></strong> could not be verified.</p>
        <div class="info-box" style="border-left: 4px solid #e74c3c;">
            <p style="margin: 0; font-size: 13px;"><strong>Reason:</strong> Please ensure you are using your assigned institutional email that has been officially registered with the school. If you believe this is an error, please contact the Registrar's office.</p>
        </div>
        <a href="register.jsp" style="background: #64748b;">Try Different Email</a>

    <% } else if ("email_taken".equals(status)) { %>
        <h2 class="error">❌ Account Exists</h2>
        <p>An account is already linked to <strong><%= email %></strong>.</p>
        <p>If you've forgotten your password, please contact the admin.</p>
        <a href="index.jsp" style="background: #2a5298;">Login Now</a>
        <br>
        <a href="register.jsp" style="background: transparent; color: #64748b; font-size: 13px; padding: 5px;">Back to Register</a>

    <% } else { %>
        <h2 class="error">❌ System Error</h2>
        <p>We encountered a problem processing your request. Please ensure all fields are correct.</p>
        <a href="register.jsp" style="background: #64748b;">Back to Register</a>
    <% } %>

</div>

</body>
</html>