package app.dao;

import app.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    private Connection getCon() {
        return DBConnection.getInstance().getConnection();
    }

    // =========================
    // BORROW BOOK
    // =========================
    public boolean borrowBook(String username, int bookId) {
        try {
            String sql = "INSERT INTO borrowed_books(username, book_id) VALUES(?,?)";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, username);
            ps.setInt(2, bookId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println(" BORROW FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // GET ALL BORROWED (ADMIN)
    // =========================
    public List<String> getAllBorrowed() {

        List<String> list = new ArrayList<>();

        try {
            String sql = "SELECT b.username, bk.title FROM borrowed_books b " +
                    "JOIN books bk ON b.book_id = bk.id";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString("username") +
                        " borrowed \"" + rs.getString("title") + "\"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // GET USER BORROWED
    // =========================
    public List<String> getUserBorrowed(String username) {

        List<String> list = new ArrayList<>();

        try {
            String sql = "SELECT bk.title FROM borrowed_books b " +
                    "JOIN books bk ON b.book_id = bk.id " +
                    "WHERE b.username=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add("You borrowed \"" + rs.getString("title") + "\"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    // =========================
// CHECK IF BOOK IS BORROWED
// =========================
    public boolean isBookBorrowed(int bookId) {

        try {
            String sql = "SELECT COUNT(*) FROM borrowed_books WHERE book_id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, bookId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            System.out.println("ERROR CHECKING BOOK STATUS");
            e.printStackTrace();
        }

        return false;
    }
}