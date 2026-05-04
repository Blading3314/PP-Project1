package Employee;

public class Employee {
    private int employeeID;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
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
    public String getFullName(){
        return firstName + " " + lastName;
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
    @Override
    public String toString(){
        return "Employee.Employee ID: " + employeeID +
                "\n" + "First Name: " + firstName +
                "\n" + "Last Name: " + lastName +
                "\n" + "Phone Number: " + phoneNumber +
                "\n" + "Email: " + email;
    }
}
