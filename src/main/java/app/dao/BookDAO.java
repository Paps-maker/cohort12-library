package app.dao;

import app.model.Book;
import app.db.DBConnection;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped //  Makes this DAO managed by the CDI container
public class BookDAO {

    //  Injecting the WildFly Managed DataSource for MySQL
    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource mysqlDataSource;

    //  Updated to use the DataSource instead of manual DBConnection
    private Connection getMySQLCon() {
        try {
            return mysqlDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println(" Failed to get MySQL connection from Pool");
            e.printStackTrace();
            return null;
        }
    }

    // Keeping PostgreSQL as is (Logic remains unchanged)
    private Connection getPostgresCon() {
        return DBConnection.getPostgresConnection();
    }

    // =========================
    // ADD BOOK (Writes to Both)
    // =========================
    public boolean addBook(Book book) {
        String sql = "INSERT INTO book(title, imageUrl, description) VALUES(?, ?, ?)";

        // We use try-with-resources inside executeWrite to ensure pool connections return
        boolean mysqlSaved = executeWrite(getMySQLCon(), sql, book.getTitle(), book.getImageUrl(), book.getDescription());
        boolean postgresSaved = executeWrite(getPostgresCon(), sql, book.getTitle(), book.getImageUrl(), book.getDescription());

        System.out.println("📚 DUAL-ADD: MySQL=" + mysqlSaved + " | Postgres=" + postgresSaved);
        return mysqlSaved || postgresSaved;
    }

    // =========================
    // GET ALL BOOKS (Reads from MySQL)
    // =========================
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book ORDER BY id DESC";

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (con == null) return books;

            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (Exception e) {
            System.err.println(" FETCH BOOKS FAILED");
            e.printStackTrace();
        }
        return books;
    }

    // =========================
    // UPDATE BOOK (Updates Both)
    // =========================
    public boolean updateBook(Book book) {
        String sql = "UPDATE book SET title=?, imageUrl=?, description=? WHERE id=?";

        boolean mysqlRes = executeUpdate(getMySQLCon(), sql, book);
        boolean pgRes = executeUpdate(getPostgresCon(), sql, book);

        return mysqlRes || pgRes;
    }

    // =========================
    // DELETE BOOK (Deletes from Both)
    // =========================
    public boolean deleteBook(int id) {
        String sql = "DELETE FROM book WHERE id=?";

        boolean mysqlRes = executeDelete(getMySQLCon(), sql, id);
        boolean pgRes = executeDelete(getPostgresCon(), sql, id);

        return mysqlRes || pgRes;
    }

    // =========================
    // GET BOOK BY ID (Reads from MySQL)
    // =========================
    public Book getBookById(int id) {
        String sql = "SELECT * FROM book WHERE id=?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return null;

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBook(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =========================
    // UTILITY METHODS (Shared Logic)
    // =========================

    private boolean executeWrite(Connection con, String sql, String... params) {
        if (con == null) return false;
        // Using try-with-resources here is CRITICAL to return connection to pool
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println(" DB Write Error: " + e.getMessage());
            return false;
        }
    }

    private boolean executeUpdate(Connection con, String sql, Book book) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getImageUrl());
            ps.setString(3, book.getDescription());
            ps.setInt(4, book.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeDelete(Connection con, String sql, int id) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("imageUrl"),
                rs.getString("description")
        );
    }
}