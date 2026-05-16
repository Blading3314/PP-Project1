package auth;

import util.DBConnectionUtility;
import util.DatabaseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.Optional;

/**
 * SQLite-backed login DAO.
 * It prepares the UserAccount table and seeds demo accounts when the table is empty.
 */
public class UserAccountDAOImpl implements UserAccountDAO {
    public UserAccountDAOImpl() {
        initializeTable();
        seedDefaultAccounts();
    }

    /**
     * Checks the entered credentials and returns the user's role when they match.
     */
    @Override
    public Optional<Role> authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                SELECT role
                FROM UserAccount
                WHERE lower(username) = lower(?) AND passwordHash = ?
                """;

        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(Role.valueOf(rs.getString("role")));
            }
        } catch (SQLException | IllegalArgumentException e) {
            if (e instanceof SQLException sqlException) {
                throw new DatabaseException("check login", sqlException);
            }
        }
        return Optional.empty();
    }

    /**
     * Creates the login table if the database does not have it yet.
     */
    private void initializeTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS UserAccount (
                    userID INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    passwordHash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYEE')),
                    employeeID INTEGER,
                    FOREIGN KEY (employeeID) REFERENCES Employee(employeeID)
                )
                """;

        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DatabaseException("prepare login table", e);
        }
    }

    /**
     * Adds demo accounts only when the login table is empty.
     */
    private void seedDefaultAccounts() {
        String countSql = "SELECT COUNT(*) AS total FROM UserAccount";
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt("total") > 0) {
                return;
            }
        } catch (SQLException e) {
            throw new DatabaseException("check login accounts", e);
        }

        createAccount("admin", "admin123", Role.ADMIN);
        createAccount("employee", "employee123", Role.EMPLOYEE);
    }

    /**
     * Stores one account with a hashed password.
     */
    private void createAccount(String username, String password, Role role) {
        String sql = "INSERT INTO UserAccount (username, passwordHash, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.setString(3, role.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("create login account", e);
        }
    }

    /**
     * Hashes passwords before they are saved or compared.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
