package app.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.xml.bind.annotation.XmlTransient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrowedbook")
public class BorrowedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Explicitly naming the ID column
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 🌟 ADDED: JsonbTransient breaks serialization loops down into the fine records
    @JsonbTransient
    @XmlTransient
    @OneToMany(mappedBy = "borrowedBook", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Fine> fines = new ArrayList<>();

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // Constructors
    public BorrowedBook() {
        this.borrowDate = LocalDateTime.now();
    }

    public BorrowedBook(User user, Book book, int daysToBorrow) {
        this.user = user;
        this.book = book;
        this.borrowDate = LocalDateTime.now();
        this.dueDate = this.borrowDate.plusDays(daysToBorrow);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public List<Fine> getFines() { return fines; }
    public void setFines(List<Fine> fines) { this.fines = fines; }
}