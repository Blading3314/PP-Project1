package Customer;

/**
 * Simple data object for a customer.
 * It mirrors the Customer table and is used by the table views and DAO layer.
 */
public class Customer {
    private int customerID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    /**
     * Creates a customer object from form input or a database row.
     */
    public Customer(int customerID, String firstName, String lastName, String phoneNumber, String email) {
        this.customerID = customerID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * Produces a readable customer summary for debugging.
     */
    @Override
    public String toString(){
        return "Customer.Customer ID: " + customerID +
                "\n" + "First Name: " + firstName +
                "\n" + "Last Name: " + lastName +
                "\n" + "Phone Number: " + phoneNumber +
                "\n" + "Email: " + email;
    }
}
