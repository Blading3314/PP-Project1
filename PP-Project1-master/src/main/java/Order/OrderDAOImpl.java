package Order;

import util.DBConnectionUtility;
import util.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of order storage.
 * It also contains small migration helpers for older copies of the project database.
 */
public class OrderDAOImpl implements OrderDAO {

    private static final String ORDER_TABLE = "\"Order\"";

    static {
        ensureQuantityColumn();
        ensureCompletedStatusAllowed();
    }

    /**
     * Adds the quantity column for older database copies.
     */
    private static void ensureQuantityColumn() {
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement st = conn.createStatement()) {
            try {
                st.execute("ALTER TABLE " + ORDER_TABLE + " ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1");
            } catch (SQLException ignored) {
                // duplicate column, missing Order table, etc.
            }
        } catch (SQLException e) {
            throw new DatabaseException("prepare order table", e);
        }
    }

    /**
     * Rebuilds the order table when an older CHECK constraint does not allow COMPLETED.
     */
    private static void ensureCompletedStatusAllowed() {
        String schemaSql = "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'Order'";
        try (Connection conn = DBConnectionUtility.getConnection();
             Statement st = conn.createStatement()) {
            String createSql;
            try (ResultSet rs = st.executeQuery(schemaSql)) {
                if (!rs.next()) {
                    return;
                }
                createSql = rs.getString(1);
            }
            if (createSql != null && createSql.contains("'COMPLETED'")) {
                return;
            }

            st.execute("PRAGMA foreign_keys = OFF");
            st.execute("CREATE TABLE IF NOT EXISTS \"Order_new\" ("
                    + "orderID INTEGER, "
                    + "orderDate DATE NOT NULL, "
                    + "comicID INTEGER, "
                    + "customerID INTEGER, "
                    + "Status VARCHAR(50) NOT NULL CHECK (Status IN ('CONFIRM', 'CANCELED', 'COMPLETED')), "
                    + "quantity INTEGER NOT NULL DEFAULT 1, "
                    + "PRIMARY KEY (orderID AUTOINCREMENT), "
                    + "FOREIGN KEY (comicID) REFERENCES Comic (comicID), "
                    + "FOREIGN KEY (customerID) REFERENCES Customer (customerID))");
            st.execute("INSERT INTO \"Order_new\" (orderID, orderDate, comicID, customerID, Status, quantity) "
                    + "SELECT orderID, orderDate, comicID, customerID, Status, quantity FROM " + ORDER_TABLE);
            st.execute("DROP TABLE " + ORDER_TABLE);
            st.execute("ALTER TABLE \"Order_new\" RENAME TO \"Order\"");
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            throw new DatabaseException("prepare order statuses", e);
        }
    }

    /**
     * Checks whether a result set includes a column before reading migration-era fields.
     */
    private static boolean hasColumn(ResultSet rs, String label) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (label.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts one database row into an Order object.
     */
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
    /**
     * Finds a single order by primary key.
     */
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
            throw new DatabaseException("load order", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Loads all orders for the order table and filters.
     */
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
            throw new DatabaseException("load orders", e);
        }
        return orders;
    }

    @Override
    /**
     * Finds one order by exact order date.
     */
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
            throw new DatabaseException("search orders", e);
        }
        return Optional.empty();
    }

    @Override
    /**
     * Inserts a new order row.
     */
    public void savePaid(Order order) {
        String sql = "INSERT INTO " + ORDER_TABLE + " (orderDate, comicID, customerID, Status, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionUtility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getOrderDate());
            ps.setInt(2, order.getComicId());
            ps.setInt(3, order.getCustomerID());
            ps.setString(4, order.getStatus());
            ps.setInt(5, Math.max(1, order.getQuantity()));
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("save order", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("save order", e);
        }
    }

    @Override
    /**
     * Deletes an order and reports if no matching row was removed.
     */
    public void deletePaidByID(int orderID) {
        String sql = "DELETE FROM " + ORDER_TABLE + " WHERE orderID = ?";
        try (Connection conn = DBConnectionUtility.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("delete order", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("delete order", e);
        }
    }

    @Override
    /**
     * Updates an existing order row with the current form values.
     */
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
            if (ps.executeUpdate() == 0) {
                throw new DatabaseException("update order", DatabaseException.Kind.NO_CHANGE);
            }
        } catch (SQLException e) {
            throw new DatabaseException("update order", e);
        }
    }
}
