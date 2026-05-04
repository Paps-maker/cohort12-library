package app.dao;

import app.model.Book;
import app.db.DBConnection;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class BookDAO {

    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource mysqlDataSource;

    public Connection getMySQLCon() {
        try {
            return mysqlDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println(" BookDAO: MySQL Connection Error");
            return null;
        }
    }

    private Connection getPostgresCon() {
        try {
            return DBConnection.getPostgresConnection();
        } catch (Exception e) {
            System.err.println(" BookDAO: PostgreSQL Connection Failed (Backup DB)");
            return null;
        }
    }

    // =========================================================================
    // SECTION 1: INVENTORY & STOCK MANAGEMENT
    // =========================================================================

    /**
     * ✅ FIXES: "cannot find symbol: method getBookTitleById(int)"
     * Fetches only the title string for efficient email notification generation.
     */
    public String getBookTitleById(int bookId) {
        String sql = "SELECT title FROM book WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return "Unknown Book";
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Book";
    }

    public boolean addBookWithCopies(Book book, int copies) {
        String sql = "INSERT INTO book(title, imageUrl, description, total_quantity, available_copies) VALUES(?, ?, ?, ?, ?)";
        try (Connection con = getMySQLCon()) {
            if (con == null) return false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getImageUrl());
                ps.setString(3, book.getDescription());
                ps.setInt(4, copies);
                ps.setInt(5, copies);
                int result = ps.executeUpdate();
                executeMirrorWrite(getPostgresCon(), sql, book, copies, copies);
                return result > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getAvailableCopiesCount(int bookId) {
        String sql = "SELECT available_copies FROM book WHERE id = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("available_copies");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateInventory(int bookId, int change, boolean isAdminUpdate) {
        String sql;
        if (isAdminUpdate) {
            sql = "UPDATE book SET total_quantity = total_quantity + ?, available_copies = available_copies + ? WHERE id = ?";
        } else {
            sql = "UPDATE book SET available_copies = available_copies + ? WHERE id = ?";
        }

        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;

            if (isAdminUpdate) {
                ps.setInt(1, change);
                ps.setInt(2, change);
                ps.setInt(3, bookId);
            } else {
                ps.setInt(1, change);
                ps.setInt(2, bookId);
            }

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCopiesToExistingBook(int bookId, int amount) {
        boolean isAdmin = (amount > 1 || amount < -1);
        return updateInventory(bookId, amount, isAdmin);
    }

    // =========================================================================
    // SECTION 2: CRUD OPERATIONS
    // =========================================================================

    public boolean updateBook(Book book) {
        String sql = "UPDATE book SET title=?, imageUrl=?, description=?, total_quantity=?, available_copies=? WHERE id=?";
        boolean m = executeFullUpdate(getMySQLCon(), sql, book);
        boolean p = executeFullUpdate(getPostgresCon(), sql, book);
        return m || p;
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM book WHERE id=?";
        boolean m = executeDelete(getMySQLCon(), sql, id);
        boolean p = executeDelete(getPostgresCon(), sql, id);
        return m || p;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book ORDER BY id DESC";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return books;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM book WHERE id=?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBook(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =========================================================================
    // SECTION 3: UTILITIES & MIRRORING
    // =========================================================================

    private void executeMirrorWrite(Connection con, String sql, Book book, int total, int avail) {
        if (con == null) return;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getImageUrl());
            ps.setString(3, book.getDescription());
            ps.setInt(4, total);
            ps.setInt(5, avail);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println(" BookDAO: Mirroring failed.");
        }
    }

    private boolean executeFullUpdate(Connection con, String sql, Book book) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getImageUrl());
            ps.setString(3, book.getDescription());
            ps.setInt(4, book.getTotalQuantity());
            ps.setInt(5, book.getAvailableCopies());
            ps.setInt(6, book.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    private boolean executeDelete(Connection con, String sql, int id) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setImageUrl(rs.getString("imageUrl"));
        book.setDescription(rs.getString("description"));
        book.setTotalQuantity(rs.getInt("total_quantity"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        return book;
    }
}