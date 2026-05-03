package main;

import Customer.Customer;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.scene.control.ListView;

public class CustomerController {
    private final CustomerDAO customerDAO = new CustomerDAOImpl();

    @FXML
    private ListView<Customer> customerListView;

    @FXML
    public void initialize() {
        customerListView.setItems(FXCollections.observableArrayList(customerDAO.getAllCustomers()));
    }

    @FXML
    public void deleteCustomer(Customer customer) {
        customerDAO.deleteCustomerByID(customer.getCustomerID());
        customerListView.getItems().remove(customer);
    }
    @FXML
    public void updateCustomer(Customer customer) {
        customerDAO.updateCustomer(customer);
        customerListView.refresh();
    }

    


}
