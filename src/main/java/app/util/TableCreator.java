package app.util;

import app.db.DBConnection;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;

public class TableCreator {

    public static void createTable(Class<?> clazz, String dbType) {
        // 1. Get Table Name from @Table notation, fallback to class name
        String tableName = clazz.getSimpleName().toLowerCase();
        if (clazz.isAnnotationPresent(Table.class)) {
            String annotatedName = clazz.getAnnotation(Table.class).name();
            if (!annotatedName.isEmpty()) tableName = annotatedName;
        }

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];

            // 2. Get Column Name from @Column notation, fallback to field name
            String columnName = field.getName();
            boolean isUnique = false;
            boolean isNullable = true;
            String columnDefinition = "";

            if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                if (!col.name().isEmpty()) columnName = col.name();
                isUnique = col.unique();
                isNullable = col.nullable();
                columnDefinition = col.columnDefinition();
            }

            // 3. Handle Data Type Mapping
            String type = !columnDefinition.isEmpty() ? columnDefinition : mapType(field.getType(), dbType);
            sql.append(columnName).append(" ").append(type);

            // 4. Handle Primary Key via @Id notation
            if (field.isAnnotationPresent(Id.class)) {
                if ("POSTGRES".equalsIgnoreCase(dbType)) {
                    // Replace type for PG Serial if it's the ID
                    sql = new StringBuilder(sql.toString().replace(columnName + " " + type, columnName + " SERIAL"));
                } else {
                    sql.append(" AUTO_INCREMENT");
                }
                sql.append(" PRIMARY KEY");
            } else {
                // Apply constraints to non-ID columns
                if (isUnique) sql.append(" UNIQUE");
                if (!isNullable) sql.append(" NOT NULL");
            }

            if (i < fields.length - 1) sql.append(", ");
        }
        sql.append(")");

        executeSQL(sql.toString(), tableName, dbType);
    }

    private static void executeSQL(String sql, String tableName, String dbType) {
        try (Connection conn = "POSTGRES".equalsIgnoreCase(dbType)
                ? DBConnection.getPostgresConnection()
                : DBConnection.getMySQLConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("✅ [" + dbType + "] Notation-Sync: " + tableName);
        } catch (Exception e) {
            System.err.println("❌ [" + dbType + "] Sync Error on " + tableName + ": " + e.getMessage());
        }
    }

    private static String mapType(Class<?> type, String dbType) {
        if (type == String.class) return "VARCHAR(255)";
        if (type == int.class || type == Integer.class) return "INT";
        if (type == double.class || type == Double.class) {
            return "POSTGRES".equalsIgnoreCase(dbType) ? "DOUBLE PRECISION" : "DOUBLE";
        }
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        return "VARCHAR(255)";
    }
}