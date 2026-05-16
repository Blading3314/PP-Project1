package main;

import Employee.Employee;
import Employee.EmployeeDAO;
import Employee.EmployeeDAOImpl;
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
 * Controller for the employee screen.
 * It manages employee searches and form actions while respecting the current user's delete permission.
 */
public class EmployeeController {
    private final I18nManager i18n = I18nManager.getInstance();

    @FXML
    private TextField idField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField searchIdField;
    @FXML
    private TextField searchFirstNameField;
    @FXML
    private TextField searchLastNameField;
    @FXML
    private Button searchButton;
    @FXML
    private Button showAllButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, String> idColumn;
    @FXML
    private TableColumn<Employee, String> fnameColumn;
    @FXML
    private TableColumn<Employee, String> lnameColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private final EmployeeDAO dao = new EmployeeDAOImpl();
    private ObservableList<Employee> allEmployees;
    private FilteredList<Employee> filteredEmployees;

    @FXML
    /**
     * Connects table columns, search listeners, form buttons, and delete permissions.
     */
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("E", cd.getValue().getEmployeeID())));
        fnameColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getFirstName()));
        lnameColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getLastName()));

        // Initialize empty table - no data loading until search
        allEmployees = FXCollections.observableArrayList();
        filteredEmployees = new FilteredList<>(allEmployees, p -> false); // Start with no results
        employeeTable.setItems(filteredEmployees);

        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row != null) {
                idField.setText(Integer.toString(row.getEmployeeID()));
                firstNameField.setText(row.getFirstName());
                lastNameField.setText(row.getLastName());
            }
        });

        searchButton.setOnAction(e -> runDatabaseAction(this::applySearchFilter));
        showAllButton.setOnAction(e -> runDatabaseAction(this::showAllEmployees));
        clearButton.setOnAction(e -> {
            clearSearchFields();
            runDatabaseAction(this::applySearchFilter);
        });
        searchIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchFirstNameField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchLastNameField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));

        addButton.setOnAction(e -> runDatabaseAction(this::onAdd));
        updateButton.setOnAction(e -> runDatabaseAction(this::onUpdate));
        deleteButton.setOnAction(e -> runDatabaseAction(this::onDelete));
        refreshButton.setOnAction(e -> runDatabaseAction(this::reloadFromDatabase));
        RoleGuard.applyDeletePermission(deleteButton);
    }

    /**
     * Reloads employees after a write operation.
     */
    private void reloadFromDatabase() {
        showAllEmployees();
    }

    /**
     * Shows every employee in the table.
     */
    private void showAllEmployees() {
        List<Employee> rows = dao.getAllEmployees();
        allEmployees = FXCollections.observableArrayList(rows);
        filteredEmployees = new FilteredList<>(allEmployees, p -> true);
        employeeTable.setItems(filteredEmployees);
    }

    /**
     * Applies the current employee search fields.
     */
    private void applySearchFilter() {
        String id = normalized(searchIdField);
        String firstName = normalized(searchFirstNameField);
        String lastName = normalized(searchLastNameField);
        if (id.isEmpty() && firstName.isEmpty() && lastName.isEmpty()) {
            allEmployees.clear();
            filteredEmployees.setPredicate(employee -> false);
            return;
        }

        List<Employee> employees = dao.getAllEmployees();
        allEmployees = FXCollections.observableArrayList(employees);
        filteredEmployees = new FilteredList<>(allEmployees, p -> false);
        employeeTable.setItems(filteredEmployees);
        
        Predicate<Employee> match = r -> {
            return containsIfPresent(Integer.toString(r.getEmployeeID()), id)
                    && containsIfPresent(safeLower(r.getFirstName()), firstName)
                    && containsIfPresent(safeLower(r.getLastName()), lastName);
        };
        filteredEmployees.setPredicate(match);
    }

    /**
     * Clears the employee search fields.
     */
    private void clearSearchFields() {
        searchIdField.clear();
        searchFirstNameField.clear();
        searchLastNameField.clear();
    }

    /**
     * Validates the form and adds a new employee.
     */
    private void onAdd() {
        if (!validateNameFields()) {
            return;
        }
        Employee e = new Employee(0,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                "",
                "");
        dao.saveEmployee(e);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Validates the form and updates the selected employee.
     */
    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("alert.employee.select.title", "alert.employee.select.update");
            return;
        }
        if (!validateNameFields()) {
            return;
        }
        Employee e = new Employee(id,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                "",
                "");
        dao.updateEmployee(e);
        reloadFromDatabase();
    }

    /**
     * Deletes the selected employee after permission and confirmation checks.
     */
    private void onDelete() {
        if (!RoleGuard.confirmDeleteAllowed(i18n)) {
            return;
        }
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("alert.employee.select.title", "alert.employee.select.remove");
            return;
        }
        if (!RoleGuard.confirmDelete(i18n, i18n.getString("delete.item.employee"))) {
            return;
        }
        dao.deleteEmployeeByID(id);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Makes sure first and last name are present before saving.
     */
    private boolean validateNameFields() {
        String fn = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String ln = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        if (fn.isEmpty() || ln.isEmpty()) {
            showInfo("alert.employee.name.title", "alert.employee.name.required");
            return false;
        }
        return true;
    }

    /**
     * Safely parses numeric IDs from text fields.
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
     * Clears the employee edit form and current table selection.
     */
    private void clearForm() {
        idField.clear();
        firstNameField.clear();
        lastNameField.clear();
        employeeTable.getSelectionModel().clearSelection();
    }

    /**
     * Normalizes search input for case-insensitive matching.
     */
    private static String normalized(TextField field) {
        return field.getText() == null ? "" : field.getText().trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Lowercases safely even when a database value is null.
     */
    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    /**
     * Treats blank filters as matches and nonblank filters as contains checks.
     */
    private static boolean containsIfPresent(String value, String filter) {
        return filter.isEmpty() || value.contains(filter);
    }

    /**
     * Formats database IDs for display, such as E1.
     */
    private static String formatFriendlyId(String prefix, int id) {
        return prefix + id;
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
