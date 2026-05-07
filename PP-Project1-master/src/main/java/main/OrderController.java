package main;

import I18n.I18nManager;
import Comic.ComicDAO;
import Comic.ComicDAOImpl;
import Customer.CustomerDAO;
import Customer.CustomerDAOImpl;
import Order.Order;
import Order.OrderDAO;
import Order.OrderDAOImpl;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class OrderController {

    private I18nManager i18n = I18nManager.getInstance();
    private DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @FXML
    private TextField orderIdField;
    @FXML
    private TextField orderDateField;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private TextField customerIdField;
    @FXML
    private TextField comicIdField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, Integer> customerIdColumn;
    @FXML
    private TableColumn<Order, Integer> comicIdColumn;
    @FXML
    private TableColumn<Order, Integer> quantityColumn;
    @FXML
    private TableColumn<Order, String> statusColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label totalOrdersLabel;

    private final OrderDAO orderDao = new OrderDAOImpl();
    private final CustomerDAO customerDao = new CustomerDAOImpl();
    private final ComicDAO comicDao = new ComicDAOImpl();
    private ObservableList<Order> allOrders;
    private FilteredList<Order> filteredOrders;

    @FXML
    private void initialize() {
        // Use database-compatible status values with internationalized display
        Map<String, String> statusMap = Map.of(
            "CONFIRM", i18n.getString("status.pending"),
            "CANCELED", i18n.getString("status.cancelled")
        );
        
        List<String> statusOptions = new ArrayList<>(statusMap.values());
        statusCombo.setItems(FXCollections.observableArrayList(statusOptions));
        statusCombo.getSelectionModel().selectFirst();

        orderIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getOrderID()));
        orderDateColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getOrderDate()));
        customerIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomerID()));
        comicIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getComicId()));
        quantityColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getQuantity()));
        statusColumn.setCellValueFactory(cd -> {
            String status = cd.getValue().getStatus();
            String displayText = switch (status) {
                case "CONFIRM" -> i18n.getString("status.pending");
                case "CANCELED" -> i18n.getString("status.cancelled");
                default -> status;
            };
            return new ReadOnlyStringWrapper(displayText);
        });

        // Initialize empty table - no data loading until search
        allOrders = FXCollections.observableArrayList();
        filteredOrders = new FilteredList<>(allOrders, p -> false); // Start with no results
        orderTable.setItems(filteredOrders);
        totalOrdersLabel.setText("0");

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row != null) {
                orderIdField.setText(Integer.toString(row.getOrderID()));
                orderDateField.setText(row.getOrderDate() == null ? "" : row.getOrderDate());
                customerIdField.setText(Integer.toString(row.getCustomerID()));
                comicIdField.setText(Integer.toString(row.getComicId()));
                quantityField.setText(Integer.toString(row.getQuantity()));
                String st = row.getStatus();
                if (st != null) {
                    String displayStatus = switch (st) {
                        case "CONFIRM" -> i18n.getString("status.pending");
                        case "CANCELED" -> i18n.getString("status.cancelled");
                        default -> st;
                    };
                    statusCombo.getSelectionModel().select(displayStatus);
                }
            } else {
                prepareBlankOrderForm();
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

        prepareBlankOrderForm();
    }

    private void prepareBlankOrderForm() {
        orderIdField.clear();
        orderDateField.setText(LocalDate.now().format(ISO));
        statusCombo.getSelectionModel().selectFirst();
        customerIdField.clear();
        comicIdField.clear();
        quantityField.setText("1");
    }

    private void reloadFromDatabase() {
        List<Order> rows = orderDao.getAllPaid();
        allOrders = FXCollections.observableArrayList(rows);
        filteredOrders = new FilteredList<>(allOrders, o -> true);
        orderTable.setItems(filteredOrders);
        applySearchFilter();
        totalOrdersLabel.setText(Integer.toString(allOrders.size()));
    }

    private void applySearchFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            // Clear table when search is empty
            allOrders.clear();
            filteredOrders.setPredicate(order -> false);
            totalOrdersLabel.setText("0");
            return;
        }
        
        // Load data from database only when searching
        List<Order> orders = orderDao.getAllPaid();
        allOrders = FXCollections.observableArrayList(orders);
        filteredOrders = new FilteredList<>(allOrders, p -> false);
        orderTable.setItems(filteredOrders);
        
        Predicate<Order> match = o -> {
            String blob = (o.getOrderID() + " " + o.getOrderDate() + " " + o.getCustomerID() + " " + o.getComicId() + " " + o.getQuantity() + " " + o.getStatus()).toLowerCase(Locale.ROOT);
            return blob.contains(q);
        };
        filteredOrders.setPredicate(match);
        totalOrdersLabel.setText(Integer.toString(filteredOrders.size()));
    }

    private void onAdd() {
        ParsedOrderForm p = parseAndValidateForm();
        if (p == null) {
            return;
        }
        Order o = new Order(0, p.date(), p.comicId(), p.customerId(), p.status(), p.quantity());
        orderDao.savePaid(o);
        reloadFromDatabase();
        prepareBlankOrderForm();
        orderTable.getSelectionModel().clearSelection();
    }

    private void onUpdate() {
        int id = parseId(orderIdField.getText());
        if (id <= 0) {
            showInfo("Select an order", "Choose a row in the table to update an existing order.");
            return;
        }
        ParsedOrderForm p = parseAndValidateForm();
        if (p == null) {
            return;
        }
        Order o = new Order(id, p.date(), p.comicId(), p.customerId(), p.status(), p.quantity());
        orderDao.updatePaid(o);
        reloadFromDatabase();
    }

    private void onDelete() {
        int id = parseId(orderIdField.getText());
        if (id <= 0) {
            showInfo("Select an order", "Pick a row or enter a valid order ID to remove.");
            return;
        }
        orderDao.deletePaidByID(id);
        reloadFromDatabase();
        prepareBlankOrderForm();
        orderTable.getSelectionModel().clearSelection();
    }

    private ParsedOrderForm parseAndValidateForm() {
        String dateStr = orderDateField.getText() == null ? "" : orderDateField.getText().trim();
        if (dateStr.isEmpty()) {
            showInfo("Date", "Enter the order date as YYYY-MM-DD (today is fine).");
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, ISO);
        } catch (DateTimeParseException e) {
            showInfo("Date", "Use ISO format: YYYY-MM-DD, for example " + LocalDate.now().format(ISO));
            return null;
        }
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            showInfo("Date", "Orders are usually not dated in the future. Check the date.");
            return null;
        }

        int custId = parseId(customerIdField.getText());
        if (custId <= 0) {
            showInfo("Customer", "Enter a positive customer ID.");
            return null;
        }
        if (customerDao.getCustomerById(custId).isEmpty()) {
            showInfo("Customer", "No customer with ID " + custId + " exists. Add the customer first or pick another ID.");
            return null;
        }

        int comicId = parseId(comicIdField.getText());
        if (comicId <= 0) {
            showInfo("Comic", "Enter a positive comic ID.");
            return null;
        }
        if (comicDao.getComicById(comicId).isEmpty()) {
            showInfo("Comic", "No comic with ID " + comicId + " exists. Add inventory first or pick another ID.");
            return null;
        }

        int qty = parseId(quantityField.getText());
        if (qty <= 0) {
            showInfo("Quantity", "Use a whole number of at least 1.");
            return null;
        }
        if (qty > 9999) {
            showInfo("Quantity", "That quantity is unusually high. If it is correct, split across multiple orders.");
            return null;
        }

        String displayStatus = statusCombo.getSelectionModel().getSelectedItem();
        if (displayStatus == null || displayStatus.isBlank()) {
            showInfo("Status", "Pick a status from the list.");
            return null;
        }
        
        // Convert display text back to database value
        String status = switch (displayStatus) {
            case String s when s.equals(i18n.getString("status.pending")) -> "CONFIRM";
            case String s when s.equals(i18n.getString("status.cancelled")) -> "CANCELED";
            default -> displayStatus;
        };

        return new ParsedOrderForm(date.format(ISO), custId, comicId, status, qty);
    }

    private record ParsedOrderForm(String date, int customerId, int comicId, String status, int quantity) {
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

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
