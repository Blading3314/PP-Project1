package Customer;

import java.util.List;
import java.util.Optional;
import java.sql.*;
import java.util.ArrayList;
import util.DBConnectionUtility;

public class CustomerDAOImpl implements CustomerDAO{

    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException{
        int customerID = rs.getInt("customerID");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String phoneNumber = rs.getString("phoneNumber");
        String email = rs.getString("email");
        return new Customer(customerID, firstName, lastName, phoneNumber, email);
    }
    @Override
    public Optional<Customer> getCustomerById(int customerID) {
        String sql = "SELECT * FROM Customer WHERE customerID = ?";
        try (Connection conn =DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, customerID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractCustomerFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer";
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return customers;
    }

    @Override
    public List<Customer> getCustomersByFirstName(String firstName) {
        List <Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE firstName = ?";
        try(Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return customers;
    }

    @Override
    public List<Customer> getCustomersByLastName(String lastName) {
        List <Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE lastName = ?";
        try(Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lastName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return customers;
    }


    @Override
    public Optional<Customer> getCustomersByEmail(String email) {
        String sql = "SELECT * FROM Customer WHERE email = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractCustomerFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> getCustomersByPhoneNumber(String phoneNumber) {
        String sql = "SELECT * FROM Customer WHERE phoneNumber = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractCustomerFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void deleteCustomerByID(int customerID) {
        String sql = "DELETE FROM Customer WHERE customerID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, customerID);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateCustomer(Customer customer)
    {
        String sql = "UPDATE Customer SET PhoneNumber=? WHERE customerID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, customer.getPhoneNumber());
            ps.setInt(2, customer.getCustomerID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void saveCustomer(Customer customer) {
        String sql = "INSERT INTO Customer (firstName, lastName, phoneNumber, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getEmail());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
