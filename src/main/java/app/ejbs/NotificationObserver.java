package app.ejbs;

import app.events.LibraryEvent;
import jakarta.annotation.Resource;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Observes;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class NotificationObserver {

    @Resource(lookup = "java:jboss/mail/LibraryMail")
    private Session mailSession;

    /**
     * ✅ Listens for events and sends real emails in the background.
     * Now handles automated overdue fine updates.
     */
    @Asynchronous
    public void onLibraryAction(@Observes LibraryEvent event) {
        String recipient = event.getEmail();

        // Customizing subjects based on event type for better UX
        String subject;
        switch (event.getType()) {
            case "REGISTER": subject = "Welcome to our Library!"; break;
            case "OVERDUE_UPDATE": subject = "⚠️ Overdue Fine Notice"; break;
            default: subject = "Library Notification: " + event.getType(); break;
        }

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(recipient).append(",\n\n");

        switch (event.getType()) {
            case "REGISTER":
                body.append("Welcome, Your account is now active, LOGIN with your USERNAME & PASSWORD.\n")
                        .append("Registration Status: ").append(event.getBookTitle()); // Contains the role info
                break;

            case "BORROW":
                body.append("You have successfully borrowed: ").append(event.getBookTitle())
                        .append(".\nPlease ensure you return it by the due date to avoid fines.");
                break;

            case "RETURN":
                body.append("Record of Return: ").append(event.getBookTitle())
                        .append("\n").append(event.getStatus()); // Shows the final fine status
                break;

            case "OVERDUE_UPDATE":
                //  Handles the automated daily fine increase emails
                body.append("This is a daily reminder that your borrowed book is overdue.\n")
                        .append("Current Status: ").append(event.getBookTitle()) // Carries the "KSH X (Days: Y)" string
                        .append("\n\nPlease return the book at your earliest convenience to stop further charges.");
                break;

            default:
                body.append("An update has been made to your library account.");
                break;
        }

        body.append("\n\nBest Regards,\nLibrary Management");

        sendActualEmail(recipient, subject, body.toString());
    }

    private void sendActualEmail(String to, String sub, String msg) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(sub);
            message.setText(msg);

            Transport.send(message);
            System.out.println(" SUCCESS: [" + sub + "] sent to " + to);

        } catch (Exception e) {
            System.err.println(" MAIL ERROR: Failed to send email to " + to);
            e.printStackTrace();
        }
    }
}