package app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book") // Explicitly names the table
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(columnDefinition = "TEXT") // Uses TEXT type for longer descriptions
    private String description;

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    public Book() {}

    public Book(String title, String imageUrl, String description) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Book(int id, String title, String imageUrl, String description) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}