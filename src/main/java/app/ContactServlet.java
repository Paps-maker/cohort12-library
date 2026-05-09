package app;

import jakarta.annotation.Resource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/submit-contact")
public class ContactServlet extends HttpServlet {

    // Inject the mail session already configured in WildFly
    @Resource(lookup = "java:jboss/mail/LibraryMail")
    private Session mailSession;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Capture form data
        String name = req.getParameter("name");
        String senderEmail = req.getParameter("email");
        String subject = req.getParameter("subject");
        String messageBody = req.getParameter("message");

        // 2. Set attributes for the JSP display
        req.setAttribute("name", name);
        req.setAttribute("email", senderEmail);
        req.setAttribute("subject", subject);
        req.setAttribute("message", messageBody);

        // 3. Start a Background Thread to send the emails
        // This keeps the UI responsive on your ProBook while the emails process
        new Thread(() -> {
            try {
                sendBackgroundEmails(name, senderEmail, subject, messageBody);
            } catch (MessagingException e) {
                System.err.println("Failed to send background emails via LibraryMail: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        // 4. Forward to the confirmation page
        req.getRequestDispatcher("contactdisplay.jsp").forward(req, resp);
    }

    /**
     * Sends emails using the server-managed JNDI mail session.
     */
    private void sendBackgroundEmails(String name, String senderEmail, String subject, String messageBody)
            throws MessagingException {

        if (mailSession == null) {
            throw new MessagingException("Mail Session 'java:jboss/mail/LibraryMail' not found. Check standalone-full.xml.");
        }

        // --- EMAIL 1: Sent to the Library (Notification) ---
        Message msgToLibrary = new MimeMessage(mailSession);
        // The 'From' address is already set in the standalone-full.xml mail-session
        msgToLibrary.setRecipients(Message.RecipientType.TO, InternetAddress.parse("livingstoneoduor21@gmail.com"));
        msgToLibrary.setSubject("New Inquiry: " + subject);
        msgToLibrary.setText("You received a message from: " + name + " (" + senderEmail + ")\n\n" + messageBody);
        Transport.send(msgToLibrary);

        // --- EMAIL 2: Sent to the User (Confirmation) ---
        Message msgToUser = new MimeMessage(mailSession);
        msgToUser.setRecipients(Message.RecipientType.TO, InternetAddress.parse(senderEmail));
        msgToUser.setSubject("Confirmation: We received your message");
        msgToUser.setText("Hello " + name + ",\n\nThank you for contacting the School Library. " +
                "We have received your inquiry regarding '" + subject + "' and will communicate shortly.\n\n" +
                "Best regards,\nLibrary Management");
        Transport.send(msgToUser);

        System.out.println("Background email delivery via LibraryMail completed successfully.");
    }
}