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
import javafx.scene.control.ButtonType;
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
import java.util.Optional;
import java.util.function.Predicate;

public class OrderController {

    private I18nManager i18n = I18nManager.getInstance();
    private DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String STATUS_PENDING = "CONFIRM";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELED";

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
    private TextField searchOrderIdField;
    @FXML
    private TextField searchDateField;
    @FXML
    private TextField searchCustomerIdField;
    @FXML
    private TextField searchComicIdField;
    @FXML
    private ComboBox<String> filterStatusCombo;
    @FXML
    private Button searchButton;
    @FXML
    private Button showAllButton;
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
    private boolean updatingFormFromSelection;

    @FXML
    private void initialize() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add(i18n.getString("status.pending"));
        statusOptions.add(i18n.getString("status.completed"));
        statusOptions.add(i18n.getString("status.cancelled"));
        statusCombo.setItems(FXCollections.observableArrayList(statusOptions));
        statusCombo.getSelectionModel().selectFirst();
        filterStatusCombo.setItems(FXCollections.observableArrayList(
                i18n.getString("common.all"),
                i18n.getString("status.pending"),
                i18n.getString("status.completed"),
                i18n.getString("status.cancelled")
        ));
        filterStatusCombo.getSelectionModel().selectFirst();

        orderIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getOrderID()));
        orderDateColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getOrderDate()));
        customerIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getCustomerID()));
        comicIdColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getComicId()));
        quantityColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getQuantity()));
        statusColumn.setCellValueFactory(cd -> {
            return new ReadOnlyStringWrapper(toDisplayStatus(cd.getValue().getStatus()));
        });

        allOrders = FXCollections.observableArrayList();
        filteredOrders = new FilteredList<>(allOrders, p -> false);
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
                    updatingFormFromSelection = true;
                    statusCombo.getSelectionModel().select(toDisplayStatus(st));
                    updatingFormFromSelection = false;
                }
            } else {
                prepareBlankOrderForm();
            }
        });

        searchButton.setOnAction(e -> applySearchFilter());
        showAllButton.setOnAction(e -> showAllOrders());
        clearButton.setOnAction(e -> {
            clearSearchFields();
            applySearchFilter();
        });
        searchOrderIdField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchDateField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchCustomerIdField.textProperty().addListener((o, a, b) -> applySearchFilter());
        searchComicIdField.textProperty().addListener((o, a, b) -> applySearchFilter());
        filterStatusCombo.valueProperty().addListener((o, a, b) -> applySearchFilter());
        statusCombo.valueProperty().addListener((o, a, b) -> {
            if (!updatingFormFromSelection && b != null && filterStatusCombo != null) {
                filterStatusCombo.getSelectionModel().select(b);
                applySearchFilter();
            }
        });

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
        showAllOrders();
    }

    private void showAllOrders() {
        clearSearchFields();
        List<Order> rows = orderDao.getAllPaid();
        allOrders = FXCollections.observableArrayList(rows);
        filteredOrders = new FilteredList<>(allOrders, o -> true);
        orderTable.setItems(filteredOrders);
        totalOrdersLabel.setText(Integer.toString(allOrders.size()));
    }

    private void applySearchFilter() {
        String id = normalized(searchOrderIdField);
        String date = normalized(searchDateField);
        String customerId = normalized(searchCustomerIdField);
        String comicId = normalized(searchComicIdField);
        String selectedStatus = filterStatusCombo.getSelectionModel().getSelectedItem();
        boolean statusFilterActive = selectedStatus != null
                && !selectedStatus.isBlank()
                && !selectedStatus.equals(i18n.getString("common.all"));

        if (id.isEmpty() && date.isEmpty() && customerId.isEmpty() && comicId.isEmpty() && !statusFilterActive) {
            allOrders.clear();
            filteredOrders.setPredicate(order -> false);
            totalOrdersLabel.setText("0");
            return;
        }

        List<Order> orders = orderDao.getAllPaid();
        allOrders = FXCollections.observableArrayList(orders);
        filteredOrders = new FilteredList<>(allOrders, p -> true);
        orderTable.setItems(filteredOrders);
        
        Predicate<Order> match = o -> {
            String displayStatus = toDisplayStatus(o.getStatus());
            boolean statusMatches = !statusFilterActive || selectedStatus.equals(displayStatus);
            return containsIfPresent(Integer.toString(o.getOrderID()), id)
                    && containsIfPresent(safeLower(o.getOrderDate()), date)
                    && containsIfPresent(Integer.toString(o.getCustomerID()), customerId)
                    && containsIfPresent(Integer.toString(o.getComicId()), comicId)
                    && statusMatches;
        };
        filteredOrders.setPredicate(match);
        totalOrdersLabel.setText(Integer.toString(filteredOrders.size()));
    }

    private void clearSearchFields() {
        searchOrderIdField.clear();
        searchDateField.clear();
        searchCustomerIdField.clear();
        searchComicIdField.clear();
        filterStatusCombo.getSelectionModel().selectFirst();
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
            showInfo("alert.order.select.title", "alert.order.select.update");
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
            showInfo("alert.order.select.title", "alert.order.select.resolve");
            return;
        }

        Optional<Order> existingOrder = orderDao.getPaidById(id);
        if (existingOrder.isEmpty()) {
            showInfo("alert.order.select.title", "alert.order.not.found");
            return;
        }

        ButtonType completedButton = new ButtonType(i18n.getString("status.completed"));
        ButtonType cancelledButton = new ButtonType(i18n.getString("status.cancelled"));
        ButtonType keepButton = new ButtonType(i18n.getString("common.cancel"));

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(i18n.getString("alert.order.resolve.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18n.getString("alert.order.resolve.message"));
        alert.getButtonTypes().setAll(completedButton, cancelledButton, keepButton);

        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isEmpty() || choice.get() == keepButton) {
            return;
        }

        Order current = existingOrder.get();
        String nextStatus = choice.get() == completedButton ? STATUS_COMPLETED : STATUS_CANCELLED;
        Order resolvedOrder = new Order(
                current.getOrderID(),
                current.getOrderDate(),
                current.getComicId(),
                current.getCustomerID(),
                nextStatus,
                current.getQuantity());
        orderDao.updatePaid(resolvedOrder);
        reloadFromDatabase();
        prepareBlankOrderForm();
        orderTable.getSelectionModel().clearSelection();
    }

    private ParsedOrderForm parseAndValidateForm() {
        String dateStr = orderDateField.getText() == null ? "" : orderDateField.getText().trim();
        if (dateStr.isEmpty()) {
            showInfo("alert.order.date.title", "alert.order.date.required");
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, ISO);
        } catch (DateTimeParseException e) {
            showInfo("alert.order.date.title", "alert.order.date.format", LocalDate.now().format(ISO));
            return null;
        }
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            showInfo("alert.order.date.title", "alert.order.date.future");
            return null;
        }

        int custId = parseId(customerIdField.getText());
        if (custId <= 0) {
            showInfo("alert.order.customer.title", "alert.order.customer.positive");
            return null;
        }
        if (customerDao.getCustomerById(custId).isEmpty()) {
            showInfo("alert.order.customer.title", "alert.order.customer.missing", custId);
            return null;
        }

        int comicId = parseId(comicIdField.getText());
        if (comicId <= 0) {
            showInfo("alert.order.comic.title", "alert.order.comic.positive");
            return null;
        }
        if (comicDao.getComicById(comicId).isEmpty()) {
            showInfo("alert.order.comic.title", "alert.order.comic.missing", comicId);
            return null;
        }

        int qty = parseId(quantityField.getText());
        if (qty <= 0) {
            showInfo("alert.order.quantity.title", "alert.order.quantity.positive");
            return null;
        }
        if (qty > 9999) {
            showInfo("alert.order.quantity.title", "alert.order.quantity.large");
            return null;
        }

        String displayStatus = statusCombo.getSelectionModel().getSelectedItem();
        if (displayStatus == null || displayStatus.isBlank()) {
            showInfo("alert.order.status.title", "alert.order.status.required");
            return null;
        }
        
        String status = toDatabaseStatus(displayStatus);

        return new ParsedOrderForm(date.format(ISO), custId, comicId, status, qty);
    }

    private String toDisplayStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_PENDING -> i18n.getString("status.pending");
            case STATUS_COMPLETED, "DELIVERED", "RECEIVED" -> i18n.getString("status.completed");
            case STATUS_CANCELLED -> i18n.getString("status.cancelled");
            default -> status;
        };
    }

    private String toDatabaseStatus(String displayStatus) {
        if (displayStatus.equals(i18n.getString("status.pending"))) {
            return STATUS_PENDING;
        }
        if (displayStatus.equals(i18n.getString("status.completed"))) {
            return STATUS_COMPLETED;
        }
        if (displayStatus.equals(i18n.getString("status.cancelled"))) {
            return STATUS_CANCELLED;
        }
        return displayStatus;
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
