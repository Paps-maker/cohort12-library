package app.dao;

import app.model.User;
import app.db.DBConnection;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserDAO {

    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource mysqlDataSource;

    private Connection getMySQLCon() {
        try {
            return mysqlDataSource.getConnection();
        } catch (SQLException e) {
            System.err.println(" UserDAO: MySQL Pool Error");
            return null;
        }
    }

    private Connection getPostgresCon() {
        return DBConnection.getPostgresConnection();
    }

    // =========================================================================
    // SECTION 1: AUTHENTICATION & LOOKUP
    // =========================================================================

    public User findUser(String username, String password) {
        String sql = "SELECT * FROM `user` WHERE username=? AND password=?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM `user` WHERE username=?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    /**
     * ✅ NEW: Optimized for FineScheduler.
     * Directly retrieves the email string associated with a username.
     */
    public String getEmailByUsername(String username) {
        String sql = "SELECT email FROM `user` WHERE username = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // =========================================================================
    // SECTION 2: USER MANAGEMENT (Dual-Write Logic)
    // =========================================================================

    public boolean createUser(User user) {
        String mysqlSql = "INSERT INTO `user`(username, email, password, role) VALUES(?,?,?,?)";
        String pgSql = "INSERT INTO \"user\"(username, email, password, role) VALUES(?,?,?,?)";

        boolean mysqlSaved = executeWrite(getMySQLCon(), mysqlSql, user.getUsername(), user.getEmail(), user.getPassword(), user.getRole());
        boolean postgresSaved = executeWrite(getPostgresCon(), pgSql, user.getUsername(), user.getEmail(), user.getPassword(), user.getRole());

        return mysqlSaved || postgresSaved;
    }

    public boolean updateUser(User user) {
        String mysqlSql = "UPDATE `user` SET username=?, email=?, role=? WHERE id=?";
        String pgSql = "UPDATE \"user\" SET username=?, email=?, role=? WHERE id=?";

        boolean mysqlUpd = executeUpdate(getMySQLCon(), mysqlSql, user);
        boolean postgresUpd = executeUpdate(getPostgresCon(), pgSql, user);

        return mysqlUpd || postgresUpd;
    }

    public boolean deleteUser(int id) {
        String mysqlSql = "DELETE FROM `user` WHERE id=?";
        String pgSql = "DELETE FROM \"user\" WHERE id=?";

        boolean mysqlDel = executeDelete(getMySQLCon(), mysqlSql, id);
        boolean postgresDel = executeDelete(getPostgresCon(), pgSql, id);

        return mysqlDel || postgresDel;
    }

    // =========================================================================
    // SECTION 3: UTILITY METHODS
    // =========================================================================

    private boolean executeWrite(Connection con, String sql, String... params) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    private boolean executeUpdate(Connection con, String sql, User user) {
        if (con == null) return false;
        try (con; PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());
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

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role")
        );
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM `user`";
        try (Connection con = getMySQLCon();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { users.add(mapUser(rs)); }
        } catch (Exception e) { }
        return users;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE email = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String getWhitelistedRole(String email) {
        String sql = "SELECT assigned_role FROM authorized_emails WHERE email = ?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("assigned_role");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM `user` WHERE id=?";
        try (Connection con = getMySQLCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (Exception e) { }
        return null;
    }
}