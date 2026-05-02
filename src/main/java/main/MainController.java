package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import Customer.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import java.util.List;
import javafx.scene.control.ListView;

public class MainController {

    @FXML
    private ListView<Customer> customerList;

    private CustomerDAO dao;

    @FXML
    public void initialize() {
        dao = new CustomerDAOImpl();

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