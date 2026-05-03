package main;

import Customer.Customer;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class CustomerController {
    @FXML
    private ListView<Customer> customerList;

    private final CustomerDAO dao = new CustomerDAOImpl();

    @FXML
    public void initialize() {


        customerList.setCellFactory(listView -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);

                if (empty || c == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createCard(c));
                }
            }
        });

        loadCustomers();
    }
    private void loadCustomers() {
        List<Customer> customers = dao.getAllCustomers();
        ObservableList<Customer> list = FXCollections.observableArrayList(customers);
        customerList.setItems(list);
    }
    private HBox createCard(Customer c) {
        HBox card = new HBox(20);
        card.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-background-color: white;");

        Label name = new Label(c.getFirstName() + " " + c.getLastName());
        Label email = new Label(c.getEmail());

        VBox left = new VBox(5, name, email);

        Label phone = new Label(c.getPhoneNumber());
        Label id = new Label("ID: " + c.getCustomerID());

        VBox right = new VBox(5, phone, id);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(left, spacer, right);

        return card;
    }

    


}
