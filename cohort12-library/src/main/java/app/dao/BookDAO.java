package app.dao;

import app.Book;
import app.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // ✅ ALWAYS USE THIS METHOD
    private Connection getCon() {
        return DBConnection.getInstance().getConnection();
    }

    // =========================
    // ADD BOOK
    // =========================
    public boolean addBook(Book book) {
        try {
            String sql = "INSERT INTO books(title) VALUES(?)";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, book.getTitle());

            int rows = ps.executeUpdate();

            System.out.println("✅ BOOK INSERTED: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println("❌ ADD BOOK FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET ALL BOOKS
    // =========================
    public List<Book> getAllBooks() {

        List<Book> books = new ArrayList<>();

        try {
            String sql = "SELECT * FROM books ORDER BY id DESC";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title")
                ));
            }

        } catch (Exception e) {
            System.out.println("❌ FETCH BOOKS FAILED");
            e.printStackTrace();
        }

        return books;
    }

    // =========================
    // DELETE BOOK
    // =========================
    public boolean deleteBook(int id) {
        try {
            String sql = "DELETE FROM books WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            System.out.println("🗑 DELETE RESULT: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println("❌ DELETE FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET BOOK BY ID
    // =========================
    public Book getBookById(int id) {

        try {
            String sql = "SELECT * FROM books WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Book(
                        rs.getInt("id"),
                        rs.getString("title")
                );
            }

        } catch (Exception e) {
            System.out.println("❌ GET BOOK FAILED");
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // UPDATE BOOK
    // =========================
    public boolean updateBook(Book book) {

        try {
            String sql = "UPDATE books SET title=? WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getId());

            int rows = ps.executeUpdate();

            System.out.println("✏ UPDATE RESULT: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println("❌ UPDATE FAILED");
            e.printStackTrace();
            return false;
        }
    }
}