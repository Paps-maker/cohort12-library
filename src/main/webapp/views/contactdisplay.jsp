<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>
    <title>Message Received</title>

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
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.05);
            text-align: center;
            max-width: 500px;
            width: 90%;
        }

        .icon-check {
            font-size: 50px;
            color: #10b981;
            margin-bottom: 20px;
        }

        h2 { color: #1e3c72; margin-bottom: 10px; }
        p { color: #64748b; line-height: 1.6; }

        .details-container {
            text-align: left;
            background: #f8fafc;
            padding: 20px;
            margin: 20px 0;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
        }

        .label {
            font-weight: bold;
            color: #475569;
            font-size: 0.9rem;
            display: block;
            margin-top: 10px;
        }

        .value {
            color: #1e293b;
            word-wrap: break-word;
        }

        .msg-content {
            border-left: 3px solid #3b82f6;
            padding-left: 12px;
            margin-top: 5px;
            font-style: italic;
        }

        a {
            display: inline-block;
            margin-top: 10px;
            padding: 12px 24px;
            background: #2a5298;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        a:hover {
            background: #1e3c72;
            transform: translateY(-2px);
        }
    </style>
</head>

<body>

<div class="box">
    <div class="icon-check">✓</div>
    <h2>Message Received</h2>
    <p>Thank you, <strong>${name}</strong>. Your inquiry has been sent successfully. A confirmation email was sent to your inbox.</p>

    <div class="details-container">
        <span class="label">Email Address:</span>
        <span class="value">${email}</span>

        <span class="label">Subject:</span>
        <span class="value">${subject}</span>

        <span class="label">Your Message:</span>
        <div class="value msg-content">${message}</div>
    </div>

    <p>Our library team will communicate with you shortly.</p>

    <a href="index.jsp">Back to Home</a>
</div>

</body>
</html>