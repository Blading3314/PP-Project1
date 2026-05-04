package Employee;

import util.DBConnectionUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAOImpl implements EmployeeDAO {

    private static final String SELECT_PUBLIC = """
            SELECT employeeID, firstName, lastName, phoneNumber, email
            FROM Employee
            """;

    private Employee extractPublic(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employeeID"),
                nullToEmpty(rs.getString("firstName")),
                nullToEmpty(rs.getString("lastName")),
                nullToEmpty(rs.getString("phoneNumber")),
                nullToEmpty(rs.getString("email")));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Override
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
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_PUBLIC)) {
            while (rs.next()) {
                employees.add(extractPublic(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
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
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
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
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
    public void deleteEmployeeByID(int employeeID) {
        String sql = "DELETE FROM Employee WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEmployee(Employee employee) {
        String sql = "UPDATE Employee SET firstName = ?, lastName = ?, phoneNumber = ?, email = ? WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullToEmpty(employee.getFirstName()));
            ps.setString(2, nullToEmpty(employee.getLastName()));
            ps.setString(3, nullToEmpty(employee.getPhoneNumber()));
            ps.setString(4, nullToEmpty(employee.getEmail()));
            ps.setInt(5, employee.getEmployeeID());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void saveEmployee(Employee employee) {
        String sql = "INSERT INTO Employee (firstName, lastName, phoneNumber, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nullToEmpty(employee.getFirstName()));
            ps.setString(2, nullToEmpty(employee.getLastName()));
            ps.setString(3, nullToEmpty(employee.getPhoneNumber()));
            ps.setString(4, nullToEmpty(employee.getEmail()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
