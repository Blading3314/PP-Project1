package main;

import Employee.Employee;
import Employee.EmployeeDAO;
import Employee.EmployeeDAOImpl;
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
    private TableColumn<Employee, Integer> idColumn;
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
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getEmployeeID()));
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

        searchButton.setOnAction(e -> applySearchFilter());
        showAllButton.setOnAction(e -> showAllEmployees());
        clearButton.setOnAction(e -> {
            clearSearchFields();
            applySearchFilter();
        });
        searchIdField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchFirstNameField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchLastNameField.textProperty().addListener((o, a, b) -> applySearchFilter());

        addButton.setOnAction(e -> onAdd());
        updateButton.setOnAction(e -> onUpdate());
        deleteButton.setOnAction(e -> onDelete());
        refreshButton.setOnAction(e -> reloadFromDatabase());
    }

    private void reloadFromDatabase() {
        showAllEmployees();
    }

    private void showAllEmployees() {
        List<Employee> rows = dao.getAllEmployees();
        allEmployees = FXCollections.observableArrayList(rows);
        filteredEmployees = new FilteredList<>(allEmployees, p -> true);
        employeeTable.setItems(filteredEmployees);
    }

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

    private void clearSearchFields() {
        searchIdField.clear();
        searchFirstNameField.clear();
        searchLastNameField.clear();
    }

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

    private void onDelete() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("alert.employee.select.title", "alert.employee.select.remove");
            return;
        }
        dao.deleteEmployeeByID(id);
        reloadFromDatabase();
        clearForm();
    }

    private boolean validateNameFields() {
        String fn = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String ln = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        if (fn.isEmpty() || ln.isEmpty()) {
            showInfo("alert.employee.name.title", "alert.employee.name.required");
            return false;
        }
        return true;
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

    private void clearForm() {
        idField.clear();
        firstNameField.clear();
        lastNameField.clear();
        employeeTable.getSelectionModel().clearSelection();
    }

    private static String normalized(TextField field) {
        return field.getText() == null ? "" : field.getText().trim().toLowerCase(Locale.ROOT);
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean containsIfPresent(String value, String filter) {
        return filter.isEmpty() || value.contains(filter);
    }

    private void showInfo(String titleKey, String messageKey, Object... args) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(i18n.getString(titleKey));
        a.setHeaderText(null);
        a.setContentText(i18n.getString(messageKey, args));
        a.showAndWait();
    }
}
