<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<jsp:useBean id="contactUs" class="app.model.ContactUs" />
<jsp:setProperty name="contactUs" property="*" />
<html>
<head>
    <title>Message Received</title>

    <style>
        body {
            font-family: 'Segoe UI';
            background: #f4f6f8;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .box {
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            text-align: center;
            max-width: 450px;
        }

        h2 { color: #2c3e50; }
        p { color: #555; }

        .msg {
            background: #f9fbff;
            padding: 10px;
            margin-top: 10px;
            border-radius: 6px;
            border-left: 4px solid #2a5298;
        }

        a {
            display: inline-block;
            margin-top: 15px;
            padding: 10px 15px;
            background: #2a5298;
            color: white;
            text-decoration: none;
            border-radius: 6px;
        }

        a:hover {
            background: #1e3c72;
        }
    </style>
</head>

<body>

<div class="box">

    <h2> Message Received</h2>

    <p>Thank you,</p>

    Name: <jsp:getProperty name="contactUs" property="name" /><br/>
           Email: ${param.email}<br/>
           Subject: ${param.subject}<br/>
           Message: ${param.message}<br/>

    <p>We will contact you shortly.</p>

    <a href="index.jsp"> Back to Home</a>

</div>

</body>
</html>