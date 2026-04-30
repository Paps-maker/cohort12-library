package app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static Connection mysqlConnection;
    private static Connection postgresConnection;

    // --- MySQL CONFIG ---
    private static final String MYSQL_BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String MYSQL_DB_NAME = "Library_2"; // Unified Name
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "@Stone001";

    // --- POSTGRES CONFIG ---
    private static final String PG_DB_NAME = "Library_2"; // Unified Name
    private static final String PG_USER = "postgres";
    private static final String PG_PASS = "@Stone001";

    // We start with 5432, but the code below will test 5433 if needed
    private static String pgBaseUrl = "jdbc:postgresql://localhost:5432/";

    // ==========================================
    // GET MYSQL CONNECTION
    // ==========================================
    public static Connection getMySQLConnection() {
        try {
            if (mysqlConnection == null || mysqlConnection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                // 1. Create DB if not exists (MySQL)
                try (Connection temp = DriverManager.getConnection(MYSQL_BASE_URL, MYSQL_USER, MYSQL_PASS)) {
                    Statement s = temp.createStatement();
                    s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + MYSQL_DB_NAME);
                }

                // 2. Connect to actual DB
                mysqlConnection = DriverManager.getConnection(
                        MYSQL_BASE_URL + MYSQL_DB_NAME + "?useSSL=false&serverTimezone=UTC",
                        MYSQL_USER,
                        MYSQL_PASS
                );
                System.out.println("✅ MYSQL CONNECTED: " + MYSQL_DB_NAME);
            }
        } catch (Exception e) {
            System.err.println("❌ MYSQL CONNECTION FAILED");
            e.printStackTrace();
        }
        return mysqlConnection;
    }

    // ==========================================
    // GET POSTGRES CONNECTION
    // ==========================================
    public static Connection getPostgresConnection() {
        try {
            if (postgresConnection == null || postgresConnection.isClosed()) {
                Class.forName("org.postgresql.Driver");

                // --- DYNAMIC PORT CHECK ---
                // Try port 5432 first, if fails, try 5433
                try {
                    try (Connection test = DriverManager.getConnection(pgBaseUrl + "postgres", PG_USER, PG_PASS)) {
                        // Port 5432 works
                    }
                } catch (SQLException e) {
                    System.out.println("⚠️ Postgres Port 5432 refused, trying 5433...");
                    pgBaseUrl = "jdbc:postgresql://localhost:5433/";
                }

                // 1. Create DB if not exists (Postgres)
                try (Connection temp = DriverManager.getConnection(pgBaseUrl + "postgres", PG_USER, PG_PASS)) {
                    Statement s = temp.createStatement();
                    var rs = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + PG_DB_NAME + "'");
                    if (!rs.next()) {
                        s.executeUpdate("CREATE DATABASE " + PG_DB_NAME);
                        System.out.println("🛠 Created Postgres Database: " + PG_DB_NAME);
                    }
                }

                // 2. Connect to actual DB
                postgresConnection = DriverManager.getConnection(pgBaseUrl + PG_DB_NAME, PG_USER, PG_PASS);
                System.out.println("✅ POSTGRESQL CONNECTED ON: " + pgBaseUrl + PG_DB_NAME);
            }
        } catch (Exception e) {
            System.err.println("❌ POSTGRESQL CONNECTION FAILED. Is the Service started in services.msc?");
            e.printStackTrace();
        }
        return postgresConnection;
    }

    // Legacy support
    public static Connection getConnection() {
        return getMySQLConnection();
    }
}