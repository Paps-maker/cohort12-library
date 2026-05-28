package app.events;

public class LibraryEvent {
    private String type; // REGISTER, BORROW, RETURN
    private String email;
    private String bookTitle;
    private String status; // "Paid", "No Fine", "Fine Issued"

    public LibraryEvent(String type, String email, String bookTitle, String status) {
        this.type = type;
        this.email = email;
        this.bookTitle = bookTitle;
        this.status = status;
    }
    // Getters
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getBookTitle() { return bookTitle; }
    public String getStatus() { return status; }
}