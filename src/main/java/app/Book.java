package app;

public class Book {

    private int id;
    private String title;

    // For insert
    public Book(String title) {
        this.title = title;
    }

    // For fetch
    public Book(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}