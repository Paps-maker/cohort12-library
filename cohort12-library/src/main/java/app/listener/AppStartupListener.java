package app.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            System.out.println("🚀 SYSTEM STARTING...");

            String url = "jdbc:mysql://localhost:3306/";
            String dbUrl = "jdbc:mysql://localhost:3306/library_db";
            String user = "root";
            String pass = "@Stone001";

            // 1. CREATE DATABASE
            Connection con = DriverManager.getConnection(url, user, pass);
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS library_db");
            con.close();

            System.out.println("✅ DATABASE READY");

            // 2. CONNECT TO DB
            Connection db = DriverManager.getConnection(dbUrl, user, pass);
            Statement s = db.createStatement();

            // 3. USERS TABLE
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "username VARCHAR(100) UNIQUE," +
                            "email VARCHAR(150)," +
                            "password VARCHAR(255)," +
                            "role VARCHAR(20))"
            );

            // 4. BOOKS TABLE
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS books (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "title VARCHAR(255))"
            );

            db.close();

            System.out.println("✅ TABLES CREATED SUCCESSFULLY");

        } catch (Exception e) {
            System.out.println("❌ STARTUP FAILED");
            e.printStackTrace();
        }
    }
}