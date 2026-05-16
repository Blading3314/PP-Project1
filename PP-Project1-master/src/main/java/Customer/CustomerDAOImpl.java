package Customer;

import java.util.List;
import java.util.Optional;
import java.sql.*;
import java.util.ArrayList;
import util.DBConnectionUtility;
import util.DatabaseException;

/**
 * SQLite implementation of customer storage.
 * This class owns the SQL for loading, searching, saving, updating, and deleting customers.
 */
public class CustomerDAOImpl implements CustomerDAO{

    /**
     * Converts the current database row into a Customer object used by the UI.
     */
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException{
        int customerID = rs.getInt("customerID");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String phoneNumber = rs.getString("phoneNumber");
        String email = rs.getString("Email");
        return new Customer(customerID, firstName, lastName, phoneNumber, email);
    }
    @Override
    /**
     * Finds one customer by primary key, usually for order validation or row lookup.
     */
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
            throw new DatabaseException("load customer", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Loads every customer for the table and search filters.
     */
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
            throw new DatabaseException("load customers", e);
        }
        return customers;
    }

    @Override
    /**
     * Searches customers by exact first name.
     */
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
            throw new DatabaseException("search customers", e);
        }
        return customers;
    }

    @Override
    /**
     * Searches customers by exact last name.
     */
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
            throw new DatabaseException("search customers", e);
        }
        return customers;
    }


    @Override
    /**
     * Looks up a customer by email so duplicate contacts can be prevented.
     */
    public Optional<Customer> getCustomersByEmail(String email) {
        String sql = "SELECT * FROM Customer WHERE Email = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractCustomerFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            throw new DatabaseException("check customer email", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Looks up a customer by phone number so duplicate contacts can be prevented.
     */
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
            throw new DatabaseException("check customer phone number", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Deletes a customer and reports a database error if no row was removed.
     */
    public void deleteCustomerByID(int customerID) {
        String sql = "DELETE FROM Customer WHERE customerID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, customerID);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("delete customer", DatabaseException.Kind.NO_CHANGE);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException("delete customer", e);
        }
    }

    @Override
    /**
     * Saves form changes back to an existing customer row.
     */
    public void updateCustomer(Customer customer)
    {
        String sql = "UPDATE Customer SET firstName=?, lastName=?, phoneNumber=?, Email=? WHERE customerID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getEmail());
            ps.setInt(5, customer.getCustomerID());
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("update customer", DatabaseException.Kind.NO_CHANGE);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException("update customer", e);
        }

    }

    @Override
    /**
     * Inserts a new customer row using the values from the customer form.
     */
    public void saveCustomer(Customer customer) {
        String sql = "INSERT INTO Customer (firstName, lastName, phoneNumber, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, customer.getFirstName());
            ps.setString(2, customer.getLastName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getEmail());
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("save customer", DatabaseException.Kind.NO_CHANGE);
            }
        }
        catch (SQLException e) {
            throw new DatabaseException("save customer", e);
        }
    }
}
