package Employee;
import util.DBConnectionUtility;
import java.util.List;
import java.util.Optional;
import java.sql.*;
import java.util.ArrayList;
public class EmployeeDAOImpl implements EmployeeDAO{
    private String dbUrl = "jdbc:sqlite:test.db";
    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException{
        int employeeID = rs.getInt("employeeID");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String phoneNumber = rs.getString("phoneNumber");
        String email = rs.getString("email");
        return new Employee(employeeID, firstName, lastName, phoneNumber, email);
    }

    @Override
    public Optional<Employee> getEmployeeById(int employeeID) {
        String sql = "SELECT * FROM Employee WHERE employeeID = ?";
        try (Connection conn = DriverManager.getConnection(sql);
        PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, employeeID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return Optional.of(extractEmployeeFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                employees.add(extractEmployeeFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByFirstName(String firstName) {
        List <Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE firstName = ?";
        try(Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                employees.add(extractEmployeeFromResultSet(rs));
            }

        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByLastName(String lastName) {
        List <Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employee WHERE lastName = ?";
        try(Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lastName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                employees.add(extractEmployeeFromResultSet(rs));
            }

        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return employees;
    }

    @Override
    public void deleteEmployeeByID(int employeeID) {
        String sql = "DELETE FROM Employee WHERE employeeID = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, employeeID);
            ps.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void updateEmployee(Employee employee) {
        String sql = "UPDATE Employee " +
                "SET firstName = ?, lastName = ?, " +
                "phoneNumber = ?, email = ? WHERE employeeID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
            ps.setString(3, employee.getPhoneNumber());
            ps.setString(4, employee.getEmail());
            ps.setInt(5, employee.getEmployeeID());
            ps.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void saveEmployee(Employee employee) {
        String sql = "INSERT INTO Employee (firstName, lastName, phoneNumber, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, employee.getFirstName());
            ps.setString(2, employee.getLastName());
            ps.setString(3, employee.getPhoneNumber());
            ps.setString(4, employee.getEmail());
            ps.executeUpdate();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
