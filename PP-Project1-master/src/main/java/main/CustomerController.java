package main;

import Customer.Customer;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CustomerController {

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> idColumn;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private final CustomerDAO dao = new CustomerDAOImpl();
    private ObservableList<Customer> allCustomers;
    private FilteredList<Customer> filteredCustomers;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomerID()));
        nameColumn.setCellValueFactory(cd -> {
            Customer c = cd.getValue();
            return new ReadOnlyStringWrapper(c.getFirstName() + " " + c.getLastName());
        });
        emailColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhoneNumber()));

        reloadFromDatabase();

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c != null) {
                idField.setText(Integer.toString(c.getCustomerID()));
                nameField.setText(c.getFirstName() + " " + c.getLastName());
                emailField.setText(c.getEmail());
                phoneField.setText(c.getPhoneNumber());
            }
        });

        searchButton.setOnAction(e -> applySearchFilter());
        clearButton.setOnAction(e -> {
            searchField.clear();
            applySearchFilter();
        });
        searchField.textProperty().addListener((o, a, b) -> applySearchFilter());

        addButton.setOnAction(e -> onAdd());
        updateButton.setOnAction(e -> onUpdate());
        deleteButton.setOnAction(e -> onDelete());
        refreshButton.setOnAction(e -> reloadFromDatabase());
    }

    private void reloadFromDatabase() {
        List<Customer> customers = dao.getAllCustomers();
        allCustomers = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(allCustomers, p -> true);
        customerTable.setItems(filteredCustomers);
        applySearchFilter();
    }

    private void applySearchFilter() {
        if (filteredCustomers == null) {
            return;
        }
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filteredCustomers.setPredicate(customer -> true);
            return;
        }
        Predicate<Customer> match = c -> {
            String blob = (c.getFirstName() + " " + c.getLastName() + " " + c.getEmail() + " " + c.getPhoneNumber()).toLowerCase(Locale.ROOT);
            return blob.contains(q);
        };
        filteredCustomers.setPredicate(match);
    }

    private void onAdd() {
        String[] names = splitName(nameField.getText());
        Customer c = new Customer(0, names[0], names[1], phoneField.getText() == null ? "" : phoneField.getText().trim(),
                emailField.getText() == null ? "" : emailField.getText().trim());
        dao.saveCustomer(c);
        reloadFromDatabase();
        clearForm();
    }

    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            return;
        }
        String[] names = splitName(nameField.getText());
        Customer c = new Customer(id, names[0], names[1], phoneField.getText() == null ? "" : phoneField.getText().trim(),
                emailField.getText() == null ? "" : emailField.getText().trim());
        dao.updateCustomer(c);
        reloadFromDatabase();
    }

    private void onDelete() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            return;
        }
        dao.deleteCustomerByID(id);
        reloadFromDatabase();
        clearForm();
    }

    private static int parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Splits raw name into trimmed first and last parts
     */
    private static String[] splitName(String raw) {
        if (raw == null || raw.isBlank()) {
            return new String[] { "", "" };
        }
        String t = raw.trim();
        int sp = t.indexOf(' ');
        if (sp < 0) {
            return new String[] { t, "" };
        }
        return new String[] { t.substring(0, sp).trim(), t.substring(sp + 1).trim() };
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        customerTable.getSelectionModel().clearSelection();
    }
}
