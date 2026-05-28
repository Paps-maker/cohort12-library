package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "copy_id") // Maps to your database primary key
    private int id;

    // 🔗 Many physical book item copies resolve backward to 1 Catalog metadata Book definition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "status", length = 255)
    private String status = "AVAILABLE";

    @Column(name = "conditionNote", length = 255)
    private String conditionNote;

    // Mapping the extra 'book' column found in your DESCRIBE output
    // to prevent "Unknown column" errors during persistence
    @Column(name = "book", length = 255)
    private String bookTitleLegacy;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public BookCopy() {}

    public BookCopy(Book book) {
        this.book = book;
    }

    public BookCopy(Book book, String status, String conditionNote) {
        this.book = book;
        this.status = status;
        this.conditionNote = conditionNote;
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public String getBookTitle() { return (book != null) ? book.getTitle() : null; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConditionNote() { return conditionNote; }
    public void setConditionNote(String conditionNote) { this.conditionNote = conditionNote; }

    public String getBookTitleLegacy() { return bookTitleLegacy; }
    public void setBookTitleLegacy(String bookTitleLegacy) { this.bookTitleLegacy = bookTitleLegacy; }
}