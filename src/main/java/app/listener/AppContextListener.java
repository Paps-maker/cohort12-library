package app.listener;

import app.db.DBConnection;
import app.util.EntityScanner;
import app.util.TableCreator;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.Arrays;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("🚀 DUAL-DATABASE APPLICATION STARTING...");

        try {
            // 1. Initialize BOTH DB connections (Triggers 'CREATE DATABASE' logic)
            // This ensures Library_2 exists before we try to create tables inside it
            DBConnection.getMySQLConnection();
            DBConnection.getPostgresConnection();

            // 2. Scan entities dynamically from the model package
            // This will find Book.class, User.class, and any others added later
            Class<?>[] entities = EntityScanner.getEntities("app.model");

            if (entities == null || entities.length == 0) {
                System.out.println("⚠️ No entities found in app.model. Check your package folder structure.");
                return;
            }

            System.out.println("🔍 Entities found: " + Arrays.toString(entities));

            // 3. Create tables dynamically for BOTH databases
            for (Class<?> entity : entities) {
                try {
                    // We call the individual DB types to ensure the dialect (SERIAL vs AUTO_INCREMENT) is applied
                    TableCreator.createTable(entity, "MYSQL");
                    TableCreator.createTable(entity, "POSTGRES");
                } catch (Exception e) {
                    System.err.println("❌ Failed to sync tables for entity: " + entity.getSimpleName());
                    e.printStackTrace();
                }
            }

            System.out.println("🎊 DUAL-DATABASE INITIALIZATION COMPLETE");

        } catch (Exception e) {
            System.err.println("🚨 CRITICAL SYSTEM STARTUP FAILED");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("🛑 APPLICATION STOPPED");
    }
}