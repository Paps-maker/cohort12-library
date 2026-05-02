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

    /**
     * Required by JPA/Hibernate.
     */
    public Book() {}

    /**
     * ✅ BACKWARD COMPATIBILITY:
     * Used by legacy servlets that only provide basic details.
     */
    public Book(String title, String imageUrl, String description) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.totalQuantity = 1;
        this.availableCopies = 1;
    }

    /**
     * ✅ NEW: For creating books with specific initial stock levels.
     */
    public Book(String title, String imageUrl, String description, int totalQuantity) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.availableCopies = totalQuantity;
    }

    /**
     * ✅ FULL: Used by DAO (mapBook) for fetching complete existing records.
     */
    public Book(int id, String title, String imageUrl, String description, int totalQuantity, int availableCopies) {
        this.id = id;
        this.title = title;
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

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    /**
     * ✅ SERVLET HELPER:
     * Resolves 'Cannot resolve method getQuantity' in BookServlet.
     * Maps 'quantity' to the internal 'totalQuantity' field.
     */
    public int getQuantity() {
        return totalQuantity;
    }
}