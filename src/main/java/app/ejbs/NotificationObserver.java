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
     * Now handles automated overdue fine updates and friendly reminders.
     */
    @Asynchronous
    public void onLibraryAction(@Observes LibraryEvent event) {
        String recipient = event.getEmail();

        // Customizing subjects based on event type for better UX
        String subject;
        switch (event.getType()) {
            case "REGISTER": subject = "Welcome to our Library!"; break;
            case "OVERDUE_UPDATE": subject = "⚠️ Overdue Fine Notice"; break;
            case "REMINDER": subject = "📅 Upcoming Return Reminder"; break; // ✅ New Case
            default: subject = "Library Notification: " + event.getType(); break;
        }

        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(recipient).append(",\n\n");

        switch (event.getType()) {
            case "REGISTER":
                body.append("Welcome, Your account is now active, LOGIN with your USERNAME & PASSWORD.\n")
                        .append("Registration Status: ").append(event.getBookTitle());
                break;

            case "BORROW":
                body.append("You have successfully borrowed: ").append(event.getBookTitle())
                        .append(".\nPlease ensure you return it by the due date to avoid fines.");
                break;

            case "RETURN":
                body.append("Record of Return: ").append(event.getBookTitle())
                        .append("\n").append(event.getStatus());
                break;

            case "OVERDUE_UPDATE":
                body.append("This is a notification regarding your overdue borrowed book.\n")
                        .append("Current Status: ").append(event.getBookTitle())
                        .append("\n\nPlease return the book at your earliest convenience to stop further charges.");
                break;

            case "REMINDER":
                //  Handles the 8:00 AM friendly "Due Tomorrow" reminders
                body.append("This is a friendly reminder regarding your borrowed book.\n")
                        .append(event.getBookTitle()) // Carries the "Due Tomorrow" message from FineScheduler
                        .append("\n\nReturning your book on time helps keep the library accessible for everyone and to avoid been fined");
                break;

            default:
                body.append("An update has been made to your library account.");
                break;
        }

        body.append("\n\nBest Regards,\nLibrary Management System");

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