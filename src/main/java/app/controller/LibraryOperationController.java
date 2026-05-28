package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.ModelAndView;
import app.model.Book;
import app.ejbs.BorrowingBean;
import app.ejbs.BookBean;
import app.ejbs.FineBean;
import app.ejbs.UserBean; // Imported for email collection

import jakarta.annotation.Resource; // Imported for Mail session injection
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import app.framework.Controller;

@Controller
public class LibraryOperationController {

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private BookBean bookBean;

    @Inject
    private FineBean fineBean;

    // Integrated logic dependencies
    @Inject
    private UserBean userBean;

    @Resource(lookup = "java:jboss/mail/LibraryMail")
    private Session mailSession;



    // INTEGRATED BROADCAST ROUTE

    @ActionPostMethod("/library/broadcast")
    public ModelAndView processBroadcast(HttpServletRequest req) {
        String subject = req.getParameter("subject");
        String messageBody = req.getParameter("message");

        // 1. Delegate user email collection entirely to the enterprise session bean
        List<String> memberEmails = userBean.getBroadcastRecipientEmails();

        boolean isSuccess;
        String statusMessage;

        // Verify that the WildFly mail subsystem binding and target records exist
        if (mailSession != null && !memberEmails.isEmpty()) {

            // 2. Asynchronously dispatch notifications in a background execution loop
            new Thread(() -> {
                try {
                    for (String recipient : memberEmails) {
                        Message message = new MimeMessage(mailSession);

                        // From header values are pulled from standalone.xml, but configured explicitly here for system visibility
                        message.setFrom(new InternetAddress("no-reply@library.ac.ke", "Campus Library Admin"));
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                        message.setSubject(subject);

                        // Premium styled responsive structural HTML announcement template wrapper
                        String htmlContent = "<div style='font-family:sans-serif; max-width:600px; border:1px solid #e2e8f0; border-radius:12px; padding:24px; color:#1e293b;'>"
                                + "<h2 style='color:#4f46e5; margin-top:0;'>Library System Announcement</h2>"
                                + "<p style='font-size:15px; line-height:1.6;'>" + messageBody + "</p>"
                                + "<hr style='border:0; border-top:1px solid #f1f5f9; margin:20px 0;'>"
                                + "<p style='font-size:12px; color:#94a3b8;'>This is an automated operational system to all library members.</p>"
                                + "</div>";

                        message.setContent(htmlContent, "text/html; charset=utf-8");
                        Transport.send(message);
                    }
                } catch (Exception e) {
                    // Prints any failed delivery trace paths smoothly onto your JBoss / WildFly terminal console logs
                    e.printStackTrace();
                }
            }).start();

            statusMessage = "Broadcast dispatched successfully! " + memberEmails.size() + " messages queued via container mail subsystem.";
            isSuccess = true;
        } else {
            statusMessage = "System Alert: Configuration mismatch. Ensure WildFly JNDI bindings are active and emails are registered.";
            isSuccess = false;
        }

        // 3. Forward cleanly using your architecture's response view canvas model
        return new ModelAndView("/broadcast_success.jsp")
                .addObject("broadcastSuccess", statusMessage)
                .addObject("isSuccess", isSuccess);
    }



    // ASYNCHRONOUS USER VERIFICATION ENDPOINT FOR WALK-IN BORROWING (JSON API)

    @ActionGetMethod("/library/verify-student")
    public void verifyStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if (!"ADMIN".equals(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Unauthorized workspace access\"}");
            return;
        }

        String searchTarget = req.getParameter("target");
        if (searchTarget == null || searchTarget.trim().isEmpty()) {
            out.print("{\"found\":false}");
            return;
        }

        boolean isRegistered = borrowingBean.isUserRegistered(searchTarget.trim());

        if (isRegistered) {
            out.print("{\"found\":true, \"username\":\"" + searchTarget.trim().toLowerCase() + "\"}");
        } else {
            out.print("{\"found\":false}");
        }
    }



    // 1. BORROW INTERFACE

    @ActionGetMethod("/library/borrow")
    public ModelAndView showBorrowForm(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return new ModelAndView("redirect:/login.jsp");
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        List<Book> books = bookBean.getAllBooks();
        double unpaidFines = "USER".equals(role) ? fineBean.getUnpaidFines(username) : 0;
        boolean isAdmin = "ADMIN".equals(role);

        return new ModelAndView("/views/borrow.jsp")
                .addObject("username", username)
                .addObject("role", role)
                .addObject("books", books)
                .addObject("unpaidFines", unpaidFines)
                .addObject("isAdmin", isAdmin);
    }

    @ActionPostMethod("/library/borrow")
    public ModelAndView processBorrow(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String loggedUser = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        String targetBorrower = loggedUser;

        if ("ADMIN".equals(role)) {
            String walkInUser = req.getParameter("targetStudent");
            if (walkInUser == null || walkInUser.trim().isEmpty()) {
                return new ModelAndView("/views/response.jsp")
                        .addObject("title", "Error")
                        .addObject("message", "A valid target student parameter context must be supplied.")
                        .addObject("isSuccess", false);
            }
            targetBorrower = walkInUser.trim();
        }

        String result = borrowingBean.attemptBorrow(targetBorrower, req.getParameter("bookId"), req.getParameter("days"));
        boolean success = result.contains("Success");

        return new ModelAndView("/views/response.jsp")
                .addObject("title", success ? "Success" : "Blocked")
                .addObject("message", result)
                .addObject("isSuccess", success);
    }



    // 2. VIEW LOANS

    @ActionGetMethod("/library/loans")
    public ModelAndView viewLoans(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            return new ModelAndView("redirect:/login.jsp");
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String infoMessage = req.getParameter("message");

        List<String> borrowedList = "ADMIN".equals(role)
                ? borrowingBean.getAdminBorrowedRecords()
                : borrowingBean.getMemberActiveLoans(username);

        return new ModelAndView("/views/loans.jsp")
                .addObject("username", username)
                .addObject("role", role)
                .addObject("infoMessage", infoMessage)
                .addObject("borrowedList", borrowedList);
    }



    // 3. ADD BOOK INTERFACE

    @ActionGetMethod("/library/addbook")
    public ModelAndView showAddBookForm(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            return new ModelAndView("redirect:/login.jsp");
        }

        return new ModelAndView("/views/addbook.jsp");
    }

    @ActionPostMethod("/library/addbook")
    public ModelAndView processAddBook(HttpServletRequest req) {
        String title = req.getParameter("title");
        String author = req.getParameter("author");
        String isbn = req.getParameter("isbn");
        String imageUrl = req.getParameter("imageUrl");
        String description = req.getParameter("description");
        int copies = 1;

        try {
            copies = Integer.parseInt(req.getParameter("copies"));
        } catch (Exception ignored) {}

        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setIsbn(isbn);
        newBook.setImageUrl(imageUrl);
        newBook.setDescription(description);
        newBook.setTotalQuantity(copies);
        newBook.setAvailableCopies(copies);

        String result = bookBean.addBook(newBook);
        boolean isSuccess = (result == null);

        return new ModelAndView("/views/response.jsp")
                .addObject("title", isSuccess ? "✅ Success" : "⚠️ Validation Failed")
                .addObject("message", isSuccess ? "Title initialized! Stock is now available for borrowing." : result)
                .addObject("isSuccess", isSuccess);
    }



    // 4. RETURN PROCESSING

    @ActionPostMethod("/library/return")
    public ModelAndView processReturn(HttpServletRequest req) {
        String role = (String) req.getSession().getAttribute("role");
        String id = req.getParameter("borrowId");
        if (id != null) id = id.replaceAll("[^0-9]", "").trim();

        String result = borrowingBean.processReturnRequest(role, id);
        return new ModelAndView("redirect:/library/loans?message=" + URLEncoder.encode(result, StandardCharsets.UTF_8));
    }
}