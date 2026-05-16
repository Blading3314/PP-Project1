package util;

import java.sql.*;

/**
 * Shared SQLite connection helper.
 * DAOs use this class so the database location is defined in one place.
 */
public class DBConnectionUtility {
    private static final String dbUrl = "jdbc:sqlite:test.db";


    private DBConnectionUtility() {

    }
    /**
     * Opens a new SQLite connection for a DAO operation.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
