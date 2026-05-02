package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Matches the VARCHAR(255) 'book' column in your DB
    @Column(name = "book", nullable = false)
    private String bookTitle;

    @Column(length = 255)
    private String status = "AVAILABLE";

    @Column(name = "conditionNote")
    private String conditionNote;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public BookCopy() {}

    public BookCopy(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public BookCopy(String bookTitle, String status, String conditionNote) {
        this.bookTitle = bookTitle;
        this.status = status;
        this.conditionNote = conditionNote;
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConditionNote() { return conditionNote; }
    public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }
}