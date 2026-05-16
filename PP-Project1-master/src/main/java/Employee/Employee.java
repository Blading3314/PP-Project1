package Employee;

/**
 * Simple data object for an employee.
 * Employees can also be linked to login accounts through the auth package.
 */
public class Employee {
    private int employeeID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;

    /**
     * Creates an employee object from form input or a database row.
     */
    public Employee(int employeeID, String firstName, String lastName, String phoneNumber, String email) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
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

    /**
     * Combines first and last name for display-friendly text.
     */
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
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
     * Produces a readable employee summary for debugging.
     */
    @Override
    public String toString() {
        return "Employee ID: " + employeeID
                + "\nFirst Name: " + firstName
                + "\nLast Name: " + lastName
                + "\nPhone: " + phoneNumber
                + "\nEmail: " + email;
    }
}
