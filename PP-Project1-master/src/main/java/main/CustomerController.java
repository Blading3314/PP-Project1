package main;

import Customer.Customer;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import I18n.I18nManager;
import auth.RoleGuard;
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
import util.DatabaseException;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Controller for the customer screen.
 * It keeps customer form actions, searching, duplicate checks, and database error alerts in one place.
 */
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
    private TableColumn<Customer, String> idColumn;
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
    /**
     * Connects table columns, search listeners, form buttons, and delete permissions.
     */
    private void initialize() {
        idField.setEditable(false);
        idColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("C", cd.getValue().getCustomerID())));
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

        searchButton.setOnAction(e -> runDatabaseAction(this::applySearchFilter));
        showAllButton.setOnAction(e -> runDatabaseAction(this::showAllCustomers));
        clearButton.setOnAction(e -> {
            clearSearchFields();
            runDatabaseAction(this::applySearchFilter);
        });
        searchIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchNameField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchEmailField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchPhoneField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));

        addButton.setOnAction(e -> runDatabaseAction(this::onAdd));
        updateButton.setOnAction(e -> runDatabaseAction(this::onUpdate));
        deleteButton.setOnAction(e -> runDatabaseAction(this::onDelete));
        refreshButton.setOnAction(e -> runDatabaseAction(this::reloadFromDatabase));
        RoleGuard.applyDeletePermission(deleteButton);
    }

    /**
     * Reloads the full customer list after a save, update, or delete.
     */
    private void reloadFromDatabase() {
        showAllCustomers();
    }

    /**
     * Shows every customer in the table.
     */
    private void showAllCustomers() {
        List<Customer> customers = dao.getAllCustomers();
        allCustomers = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(allCustomers, p -> true);
        customerTable.setItems(filteredCustomers);
    }

    /**
     * Applies the current search fields to the customer list.
     */
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

    /**
     * Clears only the search fields, not the edit form.
     */
    private void clearSearchFields() {
        searchIdField.clear();
        searchNameField.clear();
        searchEmailField.clear();
        searchPhoneField.clear();
    }

    /**
     * Validates the form and creates a new customer.
     */
    private void onAdd() {
        Customer c = buildCustomerFromForm(0);
        if (c == null || hasDuplicateContact(c)) {
            return;
        }
        dao.saveCustomer(c);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Validates the form and updates the selected customer.
     */
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

    /**
     * Deletes the selected customer after permission and confirmation checks.
     */
    private void onDelete() {
        if (!RoleGuard.confirmDeleteAllowed(i18n)) {
            return;
        }
        int id = parseId(idField.getText());
        if (id <= 0) {
            return;
        }
        if (!RoleGuard.confirmDelete(i18n, i18n.getString("delete.item.customer"))) {
            return;
        }
        dao.deleteCustomerByID(id);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Safely turns an ID field into a number, using -1 when it is invalid.
     */
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

    /**
     * Clears the edit form and removes the table selection.
     */
    private void clearForm() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        customerTable.getSelectionModel().clearSelection();
    }

    /**
     * Reads a text field without returning null.
     */
    private static String textOrEmpty(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    /**
     * Normalizes search input for case-insensitive matching.
     */
    private static String normalized(TextField field) {
        return textOrEmpty(field).toLowerCase(Locale.ROOT);
    }

    /**
     * Treats blank filters as matches and nonblank filters as contains checks.
     */
    private static boolean containsIfPresent(String value, String filter) {
        return filter.isEmpty() || (value != null && value.contains(filter));
    }

    /**
     * Formats database IDs for display, such as C1.
     */
    private static String formatFriendlyId(String prefix, int id) {
        return prefix + id;
    }

    /**
     * Builds a Customer object from the form after validating required fields.
     */
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

    /**
     * Checks email and phone uniqueness before saving a customer.
     */
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

    /**
     * Normalizes emails so duplicate checks are consistent.
     */
    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Removes formatting characters from phone numbers before comparing.
     */
    private static String normalizePhone(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    /**
     * Shows a translated informational alert.
     */
    private void showInfo(String titleKey, String messageKey, Object... args) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(i18n.getString(titleKey));
        a.setHeaderText(null);
        a.setContentText(i18n.getString(messageKey, args));
        a.showAndWait();
    }

    /**
     * Runs a database action and turns database exceptions into UI alerts.
     */
    private void runDatabaseAction(Runnable action) {
        try {
            action.run();
        } catch (DatabaseException e) {
            showDatabaseError(e);
        }
    }

    /**
     * Shows a translated database error alert.
     */
    private void showDatabaseError(DatabaseException e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(i18n.getString("alert.database.title"));
        a.setHeaderText(null);
        a.setContentText(i18n.getString(e.getMessageKey(), e.getOperation()));
        a.showAndWait();
    }
}
