<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Contact Us - Library</title>

    <style>
        body {
            margin: 0;
            font-family: 'Segoe UI', sans-serif;
            background: #f4f6f8;
        }

        header {
            background: linear-gradient(135deg, #1e3c72, #2a5298);
            color: white;
            text-align: center;
            padding: 20px;
        }

        .container {
            max-width: 900px;
            margin: 40px auto;
            padding: 20px;
        }

        .card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
            margin-bottom: 20px;
        }

        h2 {
            color: #2c3e50;
        }

        p {
            color: #555;
            line-height: 1.6;
        }

        input, textarea {
            width: 100%;
            padding: 10px;
            margin: 10px 0;
            border-radius: 6px;
            border: 1px solid #ccc;
        }

        textarea {
            height: 120px;
            resize: none;
        }

        button {
            background: #2a5298;
            color: white;
            border: none;
            padding: 12px;
            width: 100%;
            border-radius: 6px;
            cursor: pointer;
            font-weight: bold;
        }

        button:hover {
            background: #1e3c72;
        }

        .back-link {
            display: inline-block;
            margin-top: 15px;
            text-decoration: none;
            color: #2a5298;
            font-weight: bold;
        }

        footer {
            background: #2c3e50;
            color: white;
            text-align: center;
            padding: 15px;
            margin-top: 40px;
        }
    </style>
</head>

<body>

<header>
    <h1> Contact Our Library</h1>
</header>

<div class="container">

    <!-- CONTACT INFO -->
    <div class="card">
        <h2>Get in Touch</h2>
        <p>We are here to help you with book access, registration, and inquiries.</p>

        <p><strong>Email:</strong> library@school.com</p>
        <p><strong>Phone:</strong> +254 700 000 000</p>
        <p><strong>Location:</strong> Nairobi, Kenya</p>
    </div>

    <!-- CONTACT FORM -->
    <div class="card">
        <h2>Send a Message</h2>

        <!--  UPDATED ACTION -->
        <form method="post" action="contact-submit.jsp">

            <input type="text" name="name" placeholder="Your Name" required />

            <input type="email" name="email" placeholder="Your Email" required />

            <textarea name="message" placeholder="Write your message..." required></textarea>

            <button type="submit">Send Message</button>

        </form>

        <a href="index.jsp" class="back-link">← Back to Home</a>
    </div>

</div>

<footer>
    <p>© 2026 School Library</p>
</footer>

</body>
</html>