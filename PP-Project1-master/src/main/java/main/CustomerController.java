package main;

import Customer.Customer;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import I18n.I18nManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CustomerController {
    private final I18nManager i18n = I18nManager.getInstance();

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField searchIdField;
    @FXML
    private TextField searchNameField;
    @FXML
    private TextField searchEmailField;
    @FXML
    private TextField searchPhoneField;
    @FXML
    private Button searchButton;
    @FXML
    private Button showAllButton;
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
        idField.setEditable(false);
        idColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomerID()));
        nameColumn.setCellValueFactory(cd -> {
            Customer c = cd.getValue();
            return new ReadOnlyStringWrapper(c.getFirstName() + " " + c.getLastName());
        });
        emailColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhoneNumber()));

        // Initialize empty table - no data loading until search
        allCustomers = FXCollections.observableArrayList();
        filteredCustomers = new FilteredList<>(allCustomers, p -> false); // Start with no results
        customerTable.setItems(filteredCustomers);

        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c != null) {
                idField.setText(Integer.toString(c.getCustomerID()));
                nameField.setText(c.getFirstName() + " " + c.getLastName());
                emailField.setText(c.getEmail());
                phoneField.setText(c.getPhoneNumber());
            }
        });

        searchButton.setOnAction(e -> applySearchFilter());
        showAllButton.setOnAction(e -> showAllCustomers());
        clearButton.setOnAction(e -> {
            clearSearchFields();
            applySearchFilter();
        });
        searchIdField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchNameField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchEmailField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchPhoneField.textProperty().addListener((o, a, b) -> applySearchFilter());

        addButton.setOnAction(e -> onAdd());
        updateButton.setOnAction(e -> onUpdate());
        deleteButton.setOnAction(e -> onDelete());
        refreshButton.setOnAction(e -> reloadFromDatabase());
    }

    private void reloadFromDatabase() {
        showAllCustomers();
    }

    private void showAllCustomers() {
        List<Customer> customers = dao.getAllCustomers();
        allCustomers = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(allCustomers, p -> true);
        customerTable.setItems(filteredCustomers);
    }

    private void applySearchFilter() {
        String id = normalized(searchIdField);
        String name = normalized(searchNameField);
        String email = normalized(searchEmailField);
        String phone = normalizePhone(textOrEmpty(searchPhoneField));
        if (id.isEmpty() && name.isEmpty() && email.isEmpty() && phone.isEmpty()) {
            allCustomers.clear();
            filteredCustomers.setPredicate(customer -> false);
            return;
        }

        List<Customer> customers = dao.getAllCustomers();
        allCustomers = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(allCustomers, p -> false);
        customerTable.setItems(filteredCustomers);
        
        Predicate<Customer> match = c -> {
            String customerId = Integer.toString(c.getCustomerID());
            String fullName = (c.getFirstName() + " " + c.getLastName()).toLowerCase(Locale.ROOT);
            String customerEmail = normalizeEmail(c.getEmail());
            String customerPhone = normalizePhone(c.getPhoneNumber());
            return containsIfPresent(customerId, id)
                    && containsIfPresent(fullName, name)
                    && containsIfPresent(customerEmail, email)
                    && containsIfPresent(customerPhone, phone);
        };
        filteredCustomers.setPredicate(match);
    }

    private void clearSearchFields() {
        searchIdField.clear();
        searchNameField.clear();
        searchEmailField.clear();
        searchPhoneField.clear();
    }

    private void onAdd() {
        Customer c = buildCustomerFromForm(0);
        if (c == null || hasDuplicateContact(c)) {
            return;
        }
        dao.saveCustomer(c);
        reloadFromDatabase();
        clearForm();
    }

    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            return;
        }
        Customer c = buildCustomerFromForm(id);
        if (c == null || hasDuplicateContact(c)) {
            return;
        }
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

    private static String textOrEmpty(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private static String normalized(TextField field) {
        return textOrEmpty(field).toLowerCase(Locale.ROOT);
    }

    private static boolean containsIfPresent(String value, String filter) {
        return filter.isEmpty() || (value != null && value.contains(filter));
    }

    private Customer buildCustomerFromForm(int id) {
        String rawName = nameField.getText() == null ? "" : nameField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();

        if (rawName.isEmpty()) {
            showInfo("alert.customer.name.title", "alert.customer.name.required");
            return null;
        }
        if (email.isEmpty()) {
            showInfo("alert.customer.email.title", "alert.customer.email.required");
            return null;
        }
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            showInfo("alert.customer.email.title", "alert.customer.email.invalid");
            return null;
        }
        if (phone.isEmpty()) {
            showInfo("alert.customer.phone.title", "alert.customer.phone.required");
            return null;
        }

        String[] names = splitName(rawName);
        return new Customer(id, names[0], names[1], phone, email);
    }

    private boolean hasDuplicateContact(Customer customer) {
        String email = normalizeEmail(customer.getEmail());
        String phone = normalizePhone(customer.getPhoneNumber());

        for (Customer existing : dao.getAllCustomers()) {
            if (existing.getCustomerID() == customer.getCustomerID()) {
                continue;
            }
            if (!email.isEmpty() && email.equals(normalizeEmail(existing.getEmail()))) {
                showInfo("alert.customer.duplicate.email.title", "alert.customer.duplicate.email.message");
                return true;
            }
            if (!phone.isEmpty() && phone.equals(normalizePhone(existing.getPhoneNumber()))) {
                showInfo("alert.customer.duplicate.phone.title", "alert.customer.duplicate.phone.message");
                return true;
            }
        }
        return false;
    }

    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    private void showInfo(String titleKey, String messageKey, Object... args) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(i18n.getString(titleKey));
        a.setHeaderText(null);
        a.setContentText(i18n.getString(messageKey, args));
        a.showAndWait();
    }
}
