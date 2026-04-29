package Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Optional<Customer> getCustomerById(int customerID);
    List<Customer> getAllCustomers();
    List<Customer> getCustomersByFirstName(String firstName);
    List<Customer> getCustomersByLastName(String lastName);
    Optional<Customer> getCustomersByEmail(String email);
    Optional<Customer> getCustomersByPhoneNumber(String phoneNumber);
    void saveCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomerByID(int customerID);
}
