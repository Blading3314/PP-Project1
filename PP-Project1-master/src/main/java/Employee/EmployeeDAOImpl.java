package Employee;

import util.DBConnectionUtility;
import util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of employee storage.
 * It currently exposes only the fields used by the employee screen.
 */
public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String SELECT_PUBLIC = """
            SELECT employeeID, firstName, lastName
            FROM Employee
            """;

    /**
     * Builds the public employee object from the columns shown in the employee screen.
     */
    private Employee extractPublic(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employeeID"),
                nullToEmpty(rs.getString("firstName")),
                nullToEmpty(rs.getString("lastName")),
                "",
                "");
    }

    /**
     * Keeps empty database values from turning into visible "null" text in the UI.
     */
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
    /**
     * Finds one employee by ID.
     */
    public Optional<Employee> getEmployeeById(int employeeID) {
        String sql = SELECT_PUBLIC + " WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(extractPublic(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("load employee", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Loads every employee for the employee table.
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_PUBLIC)) {
            while (rs.next()) {
                employees.add(extractPublic(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("load employees", e);
        }
        return employees;
    }

    @Override
    /**
     * Searches employees by exact first name.
     */
    public List<Employee> getEmployeesByFirstName(String firstName) {
        List<Employee> employees = new ArrayList<>();
        String sql = SELECT_PUBLIC + " WHERE firstName = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                employees.add(extractPublic(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("search employees", e);
        }
        return employees;
    }

    @Override
    /**
     * Searches employees by exact last name.
     */
    public List<Employee> getEmployeesByLastName(String lastName) {
        List<Employee> employees = new ArrayList<>();
        String sql = SELECT_PUBLIC + " WHERE lastName = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lastName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                employees.add(extractPublic(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("search employees", e);
        }
        return employees;
    }

    @Override
    /**
     * Deletes an employee row and reports if nothing was removed.
     */
    public void deleteEmployeeByID(int employeeID) {
        String sql = "DELETE FROM Employee WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("delete employee", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("delete employee", e);
        }
    }

    @Override
    /**
     * Updates the name fields for an existing employee.
     */
    public void updateEmployee(Employee employee) {
        String sql = "UPDATE Employee SET firstName = ?, lastName = ? WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullToEmpty(employee.getFirstName()));
            ps.setString(2, nullToEmpty(employee.getLastName()));
            ps.setInt(3, employee.getEmployeeID());
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("update employee", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("update employee", e);
        }
    }

    @Override
    /**
     * Adds a new employee to the database.
     */
    public void saveEmployee(Employee employee) {
        String sql = "INSERT INTO Employee (firstName, lastName) VALUES (?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullToEmpty(employee.getFirstName()));
            ps.setString(2, nullToEmpty(employee.getLastName()));
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("save employee", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("save employee", e);
        }
    }
}
