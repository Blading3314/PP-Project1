package Order;

import util.DBConnectionUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private static final String ORDER_TABLE = "\"Order\"";

    static {
        ensureQuantityColumn();
    }

    private static void ensureQuantityColumn() {
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement st = conn.createStatement()) {
            try {
                st.execute("ALTER TABLE " + ORDER_TABLE + " ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1");
            } catch (SQLException ignored) {
                // duplicate column, missing Order table, etc.
            }
        } catch (SQLException e) {
            // no database file yet
        }
    }

    private static boolean hasColumn(ResultSet rs, String label) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (label.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("orderID");
        String date = rs.getString("orderDate");
        int comic = rs.getInt("comicID");
        int cust = rs.getInt("customerID");
        String status = rs.getString("Status");
        int qty = hasColumn(rs, "quantity") ? rs.getInt("quantity") : 1;
        return new Order(id, date, comic, cust, status, qty);
    }

    @Override
    public Optional<Order> getPaidById(int orderID) {
        String sql = "SELECT * FROM " + ORDER_TABLE + " WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(extractOrder(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Order> getAllPaid() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM " + ORDER_TABLE;
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(extractOrder(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return orders;
    }

    @Override
    public Optional<Order> getPaidByPaidDate(String paidDate) {
        String sql = "SELECT * FROM " + ORDER_TABLE + " WHERE orderDate = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paidDate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(extractOrder(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void savePaid(Order order) {
        String sql = "INSERT INTO " + ORDER_TABLE + " (orderDate, comicID, customerID, Status, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderDate());
            ps.setInt(2, order.getComicId());
            ps.setInt(3, order.getCustomerID());
            ps.setString(4, order.getStatus());
            ps.setInt(5, Math.max(1, order.getQuantity()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deletePaidByID(int orderID) {
        String sql = "DELETE FROM " + ORDER_TABLE + " WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updatePaid(Order order) {
        String sql = "UPDATE " + ORDER_TABLE + " SET orderDate = ?, comicID = ?, customerID = ?, Status = ?, quantity = ? WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderDate());
            ps.setInt(2, order.getComicId());
            ps.setInt(3, order.getCustomerID());
            ps.setString(4, order.getStatus());
            ps.setInt(5, Math.max(1, order.getQuantity()));
            ps.setInt(6, order.getOrderID());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
