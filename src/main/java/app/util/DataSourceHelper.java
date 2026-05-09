package app.util;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
public class DataSourceHelper {

    @Resource(lookup = "java:jboss/datasources/LibraryDS")
    private DataSource dataSource;

    /**
     * Standard getter for the DataSource object
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Direct helper to get a connection. 
     * Using 'throws SQLException' is good practice so the DAO 
     * can decide how to handle the error (log it, retry, etc.).
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource could not be injected via JNDI.");
        }
        return dataSource.getConnection();
    }
}