package app.dao;

import app.User;
import app.db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // =========================
    // GET CONNECTION
    // =========================
    private Connection getCon() {
        return DBConnection.getInstance().getConnection();
    }

    // =========================
    // CREATE USER
    // =========================
    public boolean createUser(User user) {

        try {
            String sql = "INSERT INTO users(username,email,password,role) VALUES(?,?,?,?)";

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());

            int rows = ps.executeUpdate();

            System.out.println("INSERT RESULT: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println(" INSERT FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // LOGIN USER
    // =========================
    public User findUser(String username, String password) {

        try {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }

        } catch (Exception e) {
            System.out.println(" LOGIN QUERY FAILED");
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // GET ALL USERS
    // =========================
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        try {
            String sql = "SELECT * FROM users";

            PreparedStatement ps = getCon().prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }

        } catch (Exception e) {
            System.out.println(" FETCH USERS FAILED");
            e.printStackTrace();
        }

        return users;
    }

    // =========================
    // GET USER BY ID (FOR EDIT)
    // =========================
    public User getUserById(int id) {

        try {
            String sql = "SELECT * FROM users WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }

        } catch (Exception e) {
            System.out.println(" GET USER FAILED");
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // UPDATE USER
    // =========================
    public boolean updateUser(User user) {

        try {
            String sql = "UPDATE users SET username=?, email=?, role=? WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());

            int rows = ps.executeUpdate();

            System.out.println("UPDATE RESULT: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println(" UPDATE FAILED");
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DELETE USER
    // =========================
    public boolean deleteUser(int id) {

        try {
            String sql = "DELETE FROM users WHERE id=?";

            PreparedStatement ps = getCon().prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            System.out.println("DELETE RESULT: " + rows);

            return rows > 0;

        } catch (Exception e) {
            System.out.println(" DELETE FAILED");
            e.printStackTrace();
            return false;
        }
    }
}