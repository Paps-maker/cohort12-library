package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "root";
    private static final String PASS = "@Stone001";

    private DBConnection() {
        connect();
    }

    // CORE CONNECTION METHOD
    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(URL, USER, PASS);

            System.out.println(" DATABASE CONNECTION SUCCESSFUL");

        } catch (ClassNotFoundException e) {
            System.out.println(" MySQL Driver not found");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println(" DATABASE CONNECTION FAILED");
            e.printStackTrace();
        }
    }

    // SINGLETON ACCESS
    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // SAFE CONNECTION GETTER
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println(" Reconnecting to database...");
                connect();
            }
        } catch (SQLException e) {
            System.out.println(" Connection check failed");
            e.printStackTrace();
        }

        return connection;
    }
}