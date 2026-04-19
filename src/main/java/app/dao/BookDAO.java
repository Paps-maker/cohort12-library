package app.dao;

import app.Book;
import app.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    //  ALWAYS USE THIS METHOD
    private Connection getCon() {
        return DBConnection.getInstance().getConnection();
    }

    // =========================
    // ADD BOOK (Now includes Description)
    // =========================
    public boolean addBook(Book book) {
        try {
            // Updated SQL to include description
            String sql = "INSERT INTO books(title, image_url, description) VALUES(?, ?, ?)";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getImageUrl());
            ps.setString(3, book.getDescription()); //  Added Description

            int rows = ps.executeUpdate();

            System.out.println(" BOOK INSERTED: " + rows);
            return rows > 0;

        } catch (Exception e) {
            System.out.println(" ADD BOOK FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET ALL BOOKS (Fetches Description)
    // =========================
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        try {
            String sql = "SELECT * FROM books ORDER BY id DESC";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                //  Using the new constructor: (id, title, imageUrl, description)
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("image_url"),
                        rs.getString("description") //  Added Description
                ));
            }

        } catch (Exception e) {
            System.out.println(" FETCH BOOKS FAILED");
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
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET BOOK BY ID (Fetches Description)
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
                        rs.getString("title"),
                        rs.getString("image_url"),
                        rs.getString("description") //  Added Description
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // =========================
    // UPDATE BOOK (Now updates Description)
    // =========================
    public boolean updateBook(Book book) {
        try {
            // Updated SQL to modify all three fields
            String sql = "UPDATE books SET title=?, image_url=?, description=? WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getImageUrl());
            ps.setString(3, book.getDescription()); //  Added Description
            ps.setInt(4, book.getId()); //  ID is now the 4th parameter

            int rows = ps.executeUpdate();

            System.out.println(" UPDATE RESULT: " + rows);
            return rows > 0;

        } catch (Exception e) {
            System.out.println(" UPDATE FAILED");
            e.printStackTrace();
            return false;
        }
    }
}