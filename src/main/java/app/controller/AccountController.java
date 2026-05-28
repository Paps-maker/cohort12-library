package app.controller;

import app.framework.ActionPostMethod;
import app.framework.ModelAndView;
import app.ejbs.UserBean;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService; // 🔑 Use this
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.http.HttpServletRequest;
import app.framework.Controller;

@Controller
public class AccountController {

    @Inject
    private UserBean userBean;

    @Resource(lookup = "java:jboss/mail/LibraryMail")
    private Session mailSession;

    // Inject the container's managed pool
    @Resource(lookup = "java:comp/DefaultManagedExecutorService")
    private ManagedExecutorService executor;

    // 1. REGISTRATION LOGIC
    @ActionPostMethod("/account/register")
    public ModelAndView processRegistration(HttpServletRequest req) {
        String username = req.getParameter("username");
        String email = (req.getParameter("email") != null) ? req.getParameter("email").toLowerCase().trim() : "";
        String password = req.getParameter("password");

        String status = userBean.registerUser(username, email, password);

        return new ModelAndView("/views/registerdisplay.jsp")
                .addObject("status", status)
                .addObject("username", username)
                .addObject("email", email);
    }

    // 2. CONTACT & EMAIL LOGIC (Refactored for Managed Execution)
    @ActionPostMethod("/account/contact")
    public ModelAndView handleContact(HttpServletRequest req) {
        String name = req.getParameter("name");
        String senderEmail = req.getParameter("email");
        String subject = req.getParameter("subject");
        String messageBody = req.getParameter("message");

        // Use the managed pool instead of 'new Thread()'
        executor.submit(() -> {
            try {
                sendBackgroundEmails(name, senderEmail, subject, messageBody);
            } catch (MessagingException e) {
                // Log via standard Logger for better traceability
                System.err.println("LibraryMail delivery failed: " + e.getMessage());
            }
        });

        return new ModelAndView("/views/contactdisplay.jsp")
                .addObject("name", name)
                .addObject("email", senderEmail)
                .addObject("subject", subject)
                .addObject("message", messageBody);
    }

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