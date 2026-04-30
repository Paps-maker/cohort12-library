package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representing the Join Table for Borrowing.
 * Includes borrowDate and dueDate to support dynamic return dates.
 */
@Entity
@Table(name = "borrowedbook")
public class BorrowedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private int bookId;

    /**
     * The date and time the book was borrowed.
     */
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    /**
     * ✅ NEW: The custom deadline chosen by the user.
     * This matches the 'due_date' column added via MySQL.
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // =========================
    // CONSTRUCTORS
    // =========================
    public BorrowedBook() {
        this.borrowDate = LocalDateTime.now();
    }

    public BorrowedBook(String username, int bookId, int daysToBorrow) {
        this.username = username;
        this.bookId = bookId;
        this.borrowDate = LocalDateTime.now();
        // Automatically calculate the due date based on input
        this.dueDate = this.borrowDate.plusDays(daysToBorrow);
    }

    // =========================
    // GETTERS AND SETTERS
    // =========================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

    // ✅ Added Getter and Setter for the new column
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
}