package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import Customer.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import util.DBConnectionUtility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class MainController {

    @FXML
    private TableView<Customer> Table;
    @FXML
    private TableColumn<Customer, Integer> coID;
    @FXML
    private TableColumn<Customer, String> coFN;
    @FXML
    private TableColumn<Customer, String> coLN;
    @FXML
    private TableColumn<Customer, String> coPN;
    @FXML
    private TableColumn<Customer, String> coE;

    private CustomerDAO dao;

    @FXML
    public void initialize() {
        coID.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getCustomerID()).asObject());
        coFN.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFirstName()));
        coLN.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLastName()));
        coPN.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhoneNumber()));
        coE.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        dao = new CustomerDAOImpl();

    }

    @FXML
    private void Display() {
        List<Customer> customers = dao.getAllCustomers();
        Table.setItems(FXCollections.observableArrayList(customers));
    }
}