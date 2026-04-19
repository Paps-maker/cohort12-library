package app;

/**
 * The Book Model represents a book entity in the system.
 * It now includes an image URL and a description.
 */
public class Book {

    private int id;
    private String title;
    private String imageUrl;
    private String description; // ✅ New field for the book summary

    // ==========================================
    // CONSTRUCTORS
    // ==========================================

    // Empty constructor (often useful for frameworks)
    public Book() {}

    // For adding a new book (Insert)
    public Book(String title, String imageUrl, String description) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // For fetching a book from the DB (Fetch)
    public Book(int id, String title, String imageUrl, String description) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Legacy Support: Keep your original constructors so old code doesn't break
    public Book(String title) { this.title = title; }
    public Book(int id, String title) { this.id = id; this.title = title; }
    public Book(int id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
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

    public String getDescription() { return description; } // ✅ New Getter
    public void setDescription(String description) { this.description = description; } // ✅ New Setter
}