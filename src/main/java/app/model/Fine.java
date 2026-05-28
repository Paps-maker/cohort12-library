package app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fine")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 🌟 DATABASE CONFIG: Maps the plain text column required by your schema
    @Column(name = "username", nullable = false, length = 255)
    private String username;

    // 🔗 Many penalty fine entries reference backward to 1 targeted System User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔗 Many fine line records point backward to 1 root cause BorrowedBook invoice instance context
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    private BorrowedBook borrowedBook;

    // 🔗 Many dynamic financial records cross-reference 1 Book entity definition container
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private double amount;

    // 🌟 FIX: Explicitly forces camelCase to match the exact database column 'daysOverdue'
    @Column(name = "daysOverdue", nullable = false)
    private int daysOverdue;

    @Column(nullable = false)
    private String status; // "UNPAID" or "PAID"

    // 🌟 FIX: Explicitly maps to the lowercase snake_case timestamp field
    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==========================================
    // CONSTRUCTORS
    // ==========================================
    public Fine() {}

    public Fine(User user, double amount, int daysOverdue, BorrowedBook borrowedBook, Book book) {
        this.user = user;
        this.amount = amount;
        this.daysOverdue = daysOverdue;
        this.borrowedBook = borrowedBook;
        this.book = book;
        this.status = "UNPAID";
        this.createdAt = LocalDateTime.now();

        // 🌟 SAFETY GAP FIX: Explicitly populates the text field on model instantiation
        if (user != null) {
            this.username = user.getUsername();
        } else {
            this.username = "Unknown Member";
        }
    }

    // ==========================================
    // GETTERS, SETTERS & BRIDGING LAYER FALLBACKS
    // ==========================================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() {
        if (this.username == null && user != null) {
            return user.getUsername();
        }
        return this.username;
    }
    public void setUsername(String username) { this.username = username; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BorrowedBook getBorrowedBook() { return borrowedBook; }
    public void setBorrowedBook(BorrowedBook borrowedBook) { this.borrowedBook = borrowedBook; }

    public int getBorrowId() { return (borrowedBook != null) ? borrowedBook.getId() : 0; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public int getBookId() { return (book != null) ? book.getId() : 0; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public int getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(int daysOverdue) { this.daysOverdue = daysOverdue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}