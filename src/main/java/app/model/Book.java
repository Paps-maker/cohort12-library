package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    // Added fields to resolve controller errors
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

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public Book() {}

    /**
     * Updated Constructor for creating books with basic details.
     */
    public Book(String title, String author, String isbn, int totalQuantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.totalQuantity = totalQuantity;
        this.availableCopies = totalQuantity;
    }

    /**
     * FULL: Used by DAO (mapBook) for fetching complete existing records.
     */
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
    // GETTERS AND SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // Added Getters and Setters for Author and ISBN
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

    public int getQuantity() {
        return totalQuantity;
    }
}