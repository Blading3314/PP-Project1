package Order;

import util.DBConnectionUtility;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private Order extractPaidFromResultSet(ResultSet rs) throws SQLException {
        int paidID = rs.getInt("orderID");
        String PaidDate = rs.getString("orderDate");
        int comicID = rs.getInt("comicID");
        int customerID = rs.getInt("customerID");
        String status = rs.getString("Status");
        return new Order(paidID, PaidDate, comicID, customerID, status);
    }
    @Override
    public Optional<Order> getPaidById(int orderID) {
        String sql = "SELECT * FROM Order WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractPaidFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getAllPaid() {
        List<Order> order = new ArrayList<>();
        String sql = "SELECT * FROM Order";
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                order.add(extractPaidFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return order;
    }

    @Override
    public Optional<Order> getPaidByPaidDate(String OrderDate) {
        String sql = "SELECT * FROM Order WHERE orderDate = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, OrderDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return Optional.of(extractPaidFromResultSet(rs));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void savePaid(Order order) {
        String sql = "INSERT INTO Paid (PaidDate, comicID, customerID, Status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, order.getPaidDate());
            ps.setInt(2, order.getComicId());
            ps.setInt(3, order.getCustomerID());
            ps.setString(4, order.getStatus());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deletePaidByID(int orderID) {
        String sql = "DELETE FROM Customers WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, orderID);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePaid(Order order) {
        String sql = "UPDATE Paid SET Status=? WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, order.getStatus());
            ps.setInt(2, order.getPaidID());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}




