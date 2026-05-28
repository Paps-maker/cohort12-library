package app.controller;

import app.framework.ActionGetMethod;
import app.framework.ActionPostMethod;
import app.framework.ModelAndView;
import app.model.Book;
import app.ejbs.BookBean;
import app.ejbs.FineBean;
import app.ejbs.BorrowingBean;
import app.ejbs.CatalogBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import app.framework.Controller;

@Controller
public class BookController {

    @Inject
    private BookBean bookBean;

    @Inject
    private FineBean fineBean;

    @Inject
    private BorrowingBean borrowingBean;

    @Inject
    private CatalogBean catalogBean;

    // --- 1. LIST BOOKS (DASHBOARD) ---
    @ActionGetMethod("/books")
    public ModelAndView listBooks(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            return new ModelAndView("redirect:/login");
        }

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        // Server-side Date Calculation
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy");
        String formattedDate = now.format(formatter);

        // Data Fetching Logic
        List<Book> allBooks = bookBean.getAllBooks();
        int totalUniqueTitles = allBooks.size();
        int availableCount = bookBean.getAvailableCount();
        int userBorrowedCount = borrowingBean.getMemberBorrowedCount(username);

        int displayBorrowedCount = "ADMIN".equals(role) ? bookBean.getGlobalBorrowedCount() : userBorrowedCount;
        String borrowedLabel = "ADMIN".equals(role) ? "Total Copies Borrowed" : "My Borrowed Books";

        // Fine Calculation Logic
        double totalOwed;
        String fineLabel;
        if ("ADMIN".equals(role)) {
            totalOwed = fineBean.getTotalSystemRiskDebt();
            fineLabel = "Outstanding FINES: KSH ";
        } else {
            totalOwed = fineBean.getUnpaidFines(username) + fineBean.getProjectedLateFees(username);
            fineLabel = "UNPAID FINES: KSH ";
        }

        // --- PIPELINE FORWARD FOR ADMINS ---
        if ("ADMIN".equals(role)) {
            int overdueCount = 0;
            for (Book b : allBooks) {
                if (b.getAvailableCopies() == 0) {
                    overdueCount++;
                }
            }
            int unpaidCount = (totalOwed > 0) ? 1 : 0;

            ModelAndView mv = new ModelAndView("/admin_dashboard.jsp")
                    .addObject("username", username)
                    .addObject("formattedDate", formattedDate)
                    .addObject("totalUniqueTitles", totalUniqueTitles)
                    .addObject("displayBorrowedCount", displayBorrowedCount)
                    .addObject("totalOwed", totalOwed)
                    .addObject("allBooks", allBooks)
                    .addObject("overdueCount", overdueCount)
                    .addObject("unpaidCount", unpaidCount);

            if (fineBean != null) {
                mv.addObject("userHistory", fineBean.getAllUserFinesMap());
            }
            if (borrowingBean != null) {
                mv.addObject("userLoans", borrowingBean.getAllUserLoansMap());
            }
            return mv;
        }

        // --- PIPELINE FOR REGULAR MEMBERS ---
        return new ModelAndView("/views/books_dashboard.jsp")
                .addObject("username", username)
                .addObject("role", role)
                .addObject("formattedDate", formattedDate)
                .addObject("totalUniqueTitles", totalUniqueTitles)
                .addObject("displayBorrowedCount", displayBorrowedCount)
                .addObject("borrowedLabel", borrowedLabel)
                .addObject("availableCount", availableCount)
                .addObject("totalOwed", totalOwed)
                .addObject("fineLabel", fineLabel)
                .addObject("allBooks", allBooks)
                .addObject("bookBean", bookBean); // Shared to parse structural daysLeft loops safely
    }

    // --- 2. DELETE BOOK ---
    @ActionGetMethod("/delete-book")
    public ModelAndView deleteBook(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (session == null || !"ADMIN".equals(role)) {
            System.out.println("DEBUG: Delete blocked - Session null or not ADMIN");
            return new ModelAndView("redirect:/login");
        }

        try {
            String idParam = request.getParameter("id");
            if (idParam != null) {
                int bookId = Integer.parseInt(idParam);
                System.out.println("DEBUG: Attempting to delete book ID: " + bookId);
                catalogBean.deleteBook(bookId, role);
                System.out.println("DEBUG: Delete success for ID: " + bookId);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Delete failed with error:");
            e.printStackTrace();
        }

        return new ModelAndView("redirect:/books");
    }

    // --- 3. SHOW EDIT FORM ---
    @ActionGetMethod("/edit-book")
    public ModelAndView showEditForm(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            return new ModelAndView("redirect:/books");
        }

        int id = Integer.parseInt(request.getParameter("id"));
        Book book = bookBean.getBookById(id);

        return new ModelAndView("/views/edit-book.jsp")
                .addObject("book", book);
    }

    // --- 4. UPDATE BOOK (POST) ---
    @ActionPostMethod("/update-book")
    public ModelAndView updateBook(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null && "ADMIN".equals(session.getAttribute("role"))) {
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                int addCopies = Integer.parseInt(request.getParameter("addCopies"));

                Book book = bookBean.getBookById(id);
                book.setTitle(request.getParameter("title"));
                book.setIsbn(request.getParameter("isbn"));
                book.setImageUrl(request.getParameter("imageUrl"));
                book.setDescription(request.getParameter("description"));

                bookBean.updateBook(book, addCopies);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ModelAndView("redirect:/books");
    }
}