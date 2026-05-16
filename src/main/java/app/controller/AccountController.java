package app.controller;

import app.framework.ActionPostMethod;
import app.services.UserService;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccountController {

    @Inject
    private UserService userService;

    // Inject the mail session configured in WildFly JNDI
    @Resource(lookup = "java:jboss/mail/LibraryMail")
    private Session mailSession;

    // =========================
    // 1. REGISTRATION LOGIC
    // =========================
    @ActionPostMethod("/account/register")
    public void processRegistration(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String username = req.getParameter("username");
        String email = (req.getParameter("email") != null)
                ? req.getParameter("email").toLowerCase().trim()
                : "";
        String password = req.getParameter("password");

        // Delegate to service layer
        String status = userService.registerUser(username, email, password);

        req.setAttribute("status", status);
        req.setAttribute("username", username);
        req.setAttribute("email", email);

        // Forward to the modern display JSP
        req.getRequestDispatcher("/registerdisplay.jsp").forward(req, resp);
    }

    // =========================
    // 2. CONTACT & EMAIL LOGIC
    // =========================
    @ActionPostMethod("/account/contact")
    public void handleContact(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String name = req.getParameter("name");
        String senderEmail = req.getParameter("email");
        String subject = req.getParameter("subject");
        String messageBody = req.getParameter("message");

        req.setAttribute("name", name);
        req.setAttribute("email", senderEmail);
        req.setAttribute("subject", subject);
        req.setAttribute("message", messageBody);

        // Execute background email delivery to prevent UI lag on the ProBook
        new Thread(() -> {
            try {
                sendBackgroundEmails(name, senderEmail, subject, messageBody);
            } catch (MessagingException e) {
                System.err.println("LibraryMail delivery failed: " + e.getMessage());
            }
        }).start();

        req.getRequestDispatcher("/contactdisplay.jsp").forward(req, resp);
    }

    /**
     * Internal helper for background email dispatching
     */
    private void sendBackgroundEmails(String name, String email, String subject, String body)
            throws MessagingException {

        if (mailSession == null) return;

        // Notification to Library Admin
        Message adminMsg = new MimeMessage(mailSession);
        adminMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("livingstoneoduor21@gmail.com"));
        adminMsg.setSubject("Library Inquiry: " + subject);
        adminMsg.setText("From: " + name + " (" + email + ")\n\n" + body);
        Transport.send(adminMsg);

        // Confirmation to the Member
        Message memberMsg = new MimeMessage(mailSession);
        memberMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        memberMsg.setSubject("Inquiry Received: " + subject);
        memberMsg.setText("Hello " + name + ",\n\nWe have received your message regarding " + 
                subject + " and will respond shortly.\n\nBest regards,\nLibrary Management");
        Transport.send(memberMsg);
    }
}