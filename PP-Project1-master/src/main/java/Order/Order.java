package Order;


public class Order
{
    private int orderID;
    private String orderDate;

    private int comicId;
    private int customerID;
    private String status;

    public Order(int orderID, String orderDate, int comicId, int customerID, String status) {
        this.orderID = orderID;
        this.orderDate = orderDate;
        this.comicId = comicId;
        this.customerID = customerID;
        this.status = status;
    }
    public int getPaidID() {return orderID;}
    public void setPaidID(int paidID) {this.orderID = paidID;}
    public String getPaidDate() {return orderDate;}
    public void setPaidDate(String PaidDate) {this.orderDate = PaidDate;}
    public int getComicId() {return comicId;}
    public void setComicId(int comicId) {this.comicId = comicId;}
    public int getCustomerID() {return customerID;}
    public void setCustomerID(int customerID) {this.customerID = customerID;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

  @Override
    public String toString(){
       return "Paid.Paid ID: " + orderID +
               "\n" + "order Date: " + orderDate +
               "\n" + "Comic ID: " + comicId +
               "\n" + "Customer ID: " + customerID +
               "\n" + "Status: " + status;
    }
}