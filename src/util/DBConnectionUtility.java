package util;

import java.sql.*;

public class DBConnectionUtility {
    private static final String dbUrl = "jdbc:sqlite:test.db";


    private DBConnectionUtility() {

    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
