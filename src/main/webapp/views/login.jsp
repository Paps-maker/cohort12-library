<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String contextPath = request.getContextPath();

    // FIX: Retrieve the error from the URL parameter, not the request attribute
    String errorParam = request.getParameter("error");
    String authError = null;

    if (errorParam != null && !errorParam.isEmpty()) {
        try {
            // Decode the URL-encoded message from the Controller
            authError = java.net.URLDecoder.decode(errorParam, java.nio.charset.StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            authError = "Invalid username or password";
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Library System | Authorization Workspace</title>

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
            --rose-600: #e11d48;
            --rose-50: #fff1f2;
            --bg-main: #f8fafc;
            --card-shadow: 0 10px 25px -5px rgba(2, 6, 23, 0.05), 0 8px 10px -6px rgba(2, 6, 23, 0.05);
        }

        body {
            font-family: 'Plus Jakarta Sans', sans-serif;
            margin: 0;
            padding: 0;
            background-color: var(--bg-main);
            color: var(--slate-800);
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
        }

        .auth-container { width: 100%; max-width: 440px; padding: 20px; box-sizing: border-box; }
        .auth-card { background-color: #ffffff; border-radius: 24px; border: 1px solid #e2e8f0; padding: 40px 36px; box-shadow: var(--card-shadow); }
        .auth-brand { display: flex; align-items: center; justify-content: center; gap: 12px; font-size: 22px; font-weight: 800; color: var(--slate-950); margin-bottom: 12px; }
        .auth-brand i { color: var(--indigo-600); font-size: 24px; }
        .auth-heading { font-size: 24px; font-weight: 800; color: var(--slate-900); text-align: center; margin: 0 0 6px 0; }
        .auth-subheading { font-size: 14px; color: var(--slate-400); text-align: center; margin-bottom: 32px; font-weight: 500; }
        .form-group { margin-bottom: 20px; display: flex; flex-direction: column; gap: 6px; }
        .form-group label { font-size: 13px; font-weight: 700; color: var(--slate-700); }
        .input-wrapper { position: relative; display: flex; align-items: center; }
        .input-wrapper i { position: absolute; left: 16px; color: var(--slate-400); font-size: 16px; transition: color 0.2s ease; }
        .form-control { font-family: 'Inter', sans-serif; width: 100%; padding: 14px 16px 14px 44px; box-sizing: border-box; border-radius: 12px; border: 1px solid #e2e8f0; font-size: 14px; outline: none; transition: all 0.2s; }
        .form-control:focus { border-color: var(--indigo-600); box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1); }
        .error-banner { background-color: var(--rose-50); border: 1px solid #fecaca; border-left: 4px solid var(--rose-600); border-radius: 12px; padding: 14px 16px; margin-bottom: 24px; display: flex; align-items: flex-start; gap: 12px; color: #991b1b; font-size: 13px; font-weight: 600; }
        .error-banner i { color: var(--rose-600); }
        .btn-submit { width: 100%; background-color: var(--indigo-600); color: white; padding: 14px 24px; border-radius: 12px; font-weight: 700; border: none; cursor: pointer; transition: all 0.2s; margin-top: 10px; }
        .btn-submit:hover { background-color: var(--indigo-700); }
        .auth-footer { margin-top: 28px; text-align: center; font-size: 13px; color: var(--slate-400); }
        .auth-footer a { color: var(--indigo-600); text-decoration: none; font-weight: 700; }
    </style>
</head>
<body>

    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-brand"><i class="fa-solid fa-graduation-cap"></i><span>School Library</span></div>
            <h2 class="auth-heading">Login</h2>
            <p class="auth-subheading">Enter your administrative workspace credentials</p>

            <% if (authError != null) { %>
                <div class="error-banner">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <div><%= authError %></div>
                </div>
            <% } %>

            <form action="<%= contextPath %>/authenticate" method="POST">
                <div class="form-group">
                    <label for="username">Username Account</label>
                    <div class="input-wrapper">
                        <input type="text" id="username" name="username" class="form-control" placeholder="Enter your username" required autocomplete="username">
                        <i class="fa-regular fa-user"></i>
                    </div>
                </div>
                <div class="form-group" style="margin-bottom: 24px;">
                    <label for="password">Security Password</label>
                    <div class="input-wrapper">
                        <input type="password" id="password" name="password" class="form-control" placeholder="••••••••••••" required autocomplete="current-password">
                        <i class="fa-solid fa-lock"></i>
                    </div>
                </div>
                <button type="submit" class="btn-submit">Login <i class="fa-solid fa-arrow-right"></i></button>
            </form>

            <div class="auth-footer">
                Don't have an account? <a href="<%= contextPath %>/register.jsp">Register</a>
            </div>
        </div>
    </div>
</body>
</html>