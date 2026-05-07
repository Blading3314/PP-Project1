package main;

import Employee.Employee;
import Employee.EmployeeDAO;
import Employee.EmployeeDAOImpl;
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

    @FXML
    private TextField idField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
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
    private TableColumn<Employee, String> phoneColumn;
    @FXML
    private TableColumn<Employee, String> emailColumn;
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
        phoneColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhoneNumber()));
        emailColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));

        // Initialize empty table - no data loading until search
        allEmployees = FXCollections.observableArrayList();
        filteredEmployees = new FilteredList<>(allEmployees, p -> false); // Start with no results
        employeeTable.setItems(filteredEmployees);

        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row != null) {
                idField.setText(Integer.toString(row.getEmployeeID()));
                firstNameField.setText(row.getFirstName());
                lastNameField.setText(row.getLastName());
                phoneField.setText(row.getPhoneNumber());
                emailField.setText(row.getEmail());
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
        List<Employee> rows = dao.getAllEmployees();
        allEmployees = FXCollections.observableArrayList(rows);
        filteredEmployees = new FilteredList<>(allEmployees, p -> true);
        employeeTable.setItems(filteredEmployees);
        applySearchFilter();
    }

    private void applySearchFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            // Clear table when search is empty
            allEmployees.clear();
            filteredEmployees.setPredicate(employee -> false);
            return;
        }
        
        // Load data from database only when searching
        List<Employee> employees = dao.getAllEmployees();
        allEmployees = FXCollections.observableArrayList(employees);
        filteredEmployees = new FilteredList<>(allEmployees, p -> false);
        employeeTable.setItems(filteredEmployees);
        
        Predicate<Employee> match = r -> {
            String blob = (r.getFirstName() + " " + r.getLastName() + " " + r.getPhoneNumber() + " "
                    + r.getEmail() + " " + r.getEmployeeID()).toLowerCase(Locale.ROOT);
            return blob.contains(q);
        };
        filteredEmployees.setPredicate(match);
    }

    private void onAdd() {
        if (!validateNameFields()) {
            return;
        }
        if (!validateEmailOptional()) {
            return;
        }
        Employee e = new Employee(0,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                textOrEmpty(phoneField),
                textOrEmpty(emailField));
        dao.saveEmployee(e);
        reloadFromDatabase();
        clearForm();
    }

    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("Select an employee", "Choose someone in the table (or add a new person first).");
            return;
        }
        if (!validateNameFields()) {
            return;
        }
        if (!validateEmailOptional()) {
            return;
        }
        Employee e = new Employee(id,
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                textOrEmpty(phoneField),
                textOrEmpty(emailField));
        dao.updateEmployee(e);
        reloadFromDatabase();
    }

    private void onDelete() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("Select an employee", "Pick a row to remove, or enter a valid employee ID.");
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
            showInfo("Name required", "Please enter both first and last name.");
            return false;
        }
        return true;
    }

    private boolean validateEmailOptional() {
        String em = emailField.getText() == null ? "" : emailField.getText().trim();
        if (em.isEmpty()) {
            return true;
        }
        if (!em.contains("@") || em.length() < 5) {
            showInfo("Check email", "That email does not look valid. Leave it blank or use something like name@store.com.");
            return false;
        }
        return true;
    }

    private static String textOrEmpty(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
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
        phoneField.clear();
        emailField.clear();
        employeeTable.getSelectionModel().clearSelection();
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
