package app.model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.json.bind.annotation.JsonbTransient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "available_copies")
    private int availableCopies;

    // Field-level declarations kept clean for JPA state configurations
    @JsonbTransient
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookCopy> copies = new ArrayList<>();

    @JsonbTransient
    @OneToMany(mappedBy = "book", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<BorrowedBook> borrowRecords = new ArrayList<>();

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public Book() {}

    public Book(String title, String author, String isbn, int totalQuantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.availableCopies = totalQuantity;
    }

    public Book(int id, String title, String author, String isbn, String imageUrl, String description, int totalQuantity, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.imageUrl = imageUrl;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.availableCopies = availableCopies;
    }

    // ==========================================
    // GETTERS, SETTERS & RELATIONAL HELPERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public int getQuantity() { return totalQuantity; }
    public void setQuantity(int quantity) { this.totalQuantity = quantity; }

    /**
     * 🌟 UPDATED: Moved @XmlTransient to the getter.
     * This forces the JAX-WS runtime marshaller to completely skip book copy loops.
     */
    @XmlTransient
    public List<BookCopy> getCopies() { return copies; }
    public void setCopies(List<BookCopy> copies) { this.copies = copies; }

    /**
     * 🌟 UPDATED: Moved @XmlTransient to the getter.
     * This prevents relational deep nesting exceptions when generating SOAP XML payloads.
     */
    @XmlTransient
    public List<BorrowedBook> getBorrowRecords() { return borrowRecords; }
    public void setBorrowRecords(List<BorrowedBook> borrowRecords) { this.borrowRecords = borrowRecords; }

    // ==========================================
    // BI-DIRECTIONAL SYNCHRONIZATION HELPERS
    // ==========================================

    public void addCopy(BookCopy copy) {
        this.copies.add(copy);
        copy.setBook(this);
    }

    public void removeCopy(BookCopy copy) {
        this.copies.remove(copy);
        copy.setBook(null);
    }

    public void addBorrowRecord(BorrowedBook record) {
        this.borrowRecords.add(record);
        record.setBook(this);
    }
}