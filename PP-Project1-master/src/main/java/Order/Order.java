package Order;

/**
 * Simple data object for an order.
 * Orders connect a customer to a comic and keep the date, status, and quantity together.
 */
public class Order {
    private int orderID;
    private String orderDate;
    private int comicId;
    private int customerID;
    private String status;
    private int quantity;

    /**
     * Creates an order with the default quantity of one.
     */
    public Order(int orderID, String orderDate, int comicId, int customerID, String status) {
        this(orderID, orderDate, comicId, customerID, status, 1);
    }

    /**
     * Creates a full order object from form input or a database row.
     */
    public Order(int orderID, String orderDate, int comicId, int customerID, String status, int quantity) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.comicId = comicId;
        this.customerID = customerID;
        this.status = status;
        this.quantity = quantity;
    }

    public int getOrderID() {
        return orderID;
    }

    /**
     * Older name kept so existing code still compiles.
     *
     * @deprecated use {@link #getOrderID()}
     */
    public int getPaidID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getOrderDate() {
        return orderDate;
    }

    /**
     * Older name kept so existing code still compiles.
     *
     * @deprecated use {@link #getOrderDate()}
     */
    public String getPaidDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getComicId() {
        return comicId;
    }

    public void setComicId(int comicId) {
        this.comicId = comicId;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Produces a readable order summary for debugging.
     */
    @Override
    public String toString() {
        return "Order #" + orderID
                + "\nDate: " + orderDate
                + "\nCustomer ID: " + customerID
                + "\nComic ID: " + comicId
                + "\nQty: " + quantity
                + "\nStatus: " + status;
    }
}
