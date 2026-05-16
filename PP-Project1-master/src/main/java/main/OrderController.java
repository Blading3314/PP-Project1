package main;

import I18n.I18nManager;
import auth.RoleGuard;
import Comic.Comic;
import Comic.ComicDAO;
import Comic.ComicDAOImpl;
import Customer.Customer;
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
import util.DatabaseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Controller for the order screen.
 * It validates order data, shows live customer/comic ID lookups, and manages order table filtering.
 */
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
    private Label customerLookupLabel;
    @FXML
    private Label comicLookupLabel;
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
    private TableColumn<Order, String> orderIdColumn;
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    @FXML
    private TableColumn<Order, String> customerIdColumn;
    @FXML
    private TableColumn<Order, String> comicIdColumn;
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
    /**
     * Connects order fields, table columns, lookup helpers, search filters, and buttons.
     */
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

        orderIdColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("O", cd.getValue().getOrderID())));
        orderDateColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getOrderDate()));
        customerIdColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("C", cd.getValue().getCustomerID())));
        comicIdColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("C", cd.getValue().getComicId())));
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

        searchButton.setOnAction(e -> runDatabaseAction(this::applySearchFilter));
        showAllButton.setOnAction(e -> runDatabaseAction(this::showAllOrders));
        clearButton.setOnAction(e -> {
            clearSearchFields();
            runDatabaseAction(this::applySearchFilter);
        });
        searchOrderIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchDateField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchCustomerIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchComicIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        customerIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::updateCustomerLookup));
        comicIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::updateComicLookup));
        filterStatusCombo.valueProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        statusCombo.valueProperty().addListener((o, a, b) -> {
            if (!updatingFormFromSelection && b != null && filterStatusCombo != null) {
                filterStatusCombo.getSelectionModel().select(b);
                runDatabaseAction(this::applySearchFilter);
            }
        });

        addButton.setOnAction(e -> runDatabaseAction(this::onAdd));
        updateButton.setOnAction(e -> runDatabaseAction(this::onUpdate));
        deleteButton.setOnAction(e -> runDatabaseAction(this::onDelete));
        refreshButton.setOnAction(e -> runDatabaseAction(this::reloadFromDatabase));
        RoleGuard.applyDeletePermission(deleteButton);

        prepareBlankOrderForm();
        updateCustomerLookup();
        updateComicLookup();
    }

    /**
     * Resets the order form to a new-order state.
     */
    private void prepareBlankOrderForm() {
        orderIdField.clear();
        orderDateField.setText(LocalDate.now().format(ISO));
        statusCombo.getSelectionModel().selectFirst();
        customerIdField.clear();
        comicIdField.clear();
        quantityField.setText("1");
    }

    /**
     * Shows who the entered customer ID belongs to.
     */
    private void updateCustomerLookup() {
        int customerId = parseId(customerIdField.getText());
        if (customerId <= 0) {
            customerLookupLabel.setText(i18n.getString("order.customer.lookup.empty"));
            return;
        }

        customerDao.getCustomerById(customerId)
                .map(this::formatCustomerLookup)
                .ifPresentOrElse(
                        customerLookupLabel::setText,
                        () -> customerLookupLabel.setText(i18n.getString("order.customer.lookup.missing", customerId)));
    }

    /**
     * Shows which comic the entered comic ID belongs to.
     */
    private void updateComicLookup() {
        int comicId = parseId(comicIdField.getText());
        if (comicId <= 0) {
            comicLookupLabel.setText(i18n.getString("order.comic.lookup.empty"));
            return;
        }

        comicDao.getComicById(comicId)
                .map(this::formatComicLookup)
                .ifPresentOrElse(
                        comicLookupLabel::setText,
                        () -> comicLookupLabel.setText(i18n.getString("order.comic.lookup.missing", comicId)));
    }

    /**
     * Formats the customer lookup helper text.
     */
    private String formatCustomerLookup(Customer customer) {
        String name = (customer.getFirstName() + " " + customer.getLastName()).trim();
        return i18n.getString("order.customer.lookup.found", customer.getCustomerID(), name);
    }

    /**
     * Formats the comic lookup helper text.
     */
    private String formatComicLookup(Comic comic) {
        return i18n.getString("order.comic.lookup.found", comic.getComicID(), comic.getName(), comic.getIssue());
    }

    /**
     * Formats database IDs for display, such as O1 or C1.
     */
    private static String formatFriendlyId(String prefix, int id) {
        return prefix + id;
    }

    /**
     * Reloads orders after a write operation.
     */
    private void reloadFromDatabase() {
        showAllOrders();
    }

    /**
     * Shows every order in the table.
     */
    private void showAllOrders() {
        clearSearchFields();
        List<Order> rows = orderDao.getAllPaid();
        allOrders = FXCollections.observableArrayList(rows);
        filteredOrders = new FilteredList<>(allOrders, o -> true);
        orderTable.setItems(filteredOrders);
        totalOrdersLabel.setText(Integer.toString(allOrders.size()));
    }

    /**
     * Applies the current order search fields and status filter.
     */
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

    /**
     * Clears the order search fields and resets the status filter.
     */
    private void clearSearchFields() {
        searchOrderIdField.clear();
        searchDateField.clear();
        searchCustomerIdField.clear();
        searchComicIdField.clear();
        filterStatusCombo.getSelectionModel().selectFirst();
    }

    /**
     * Validates the form and creates a new order.
     */
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

    /**
     * Validates the form and updates the selected order.
     */
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

    /**
     * Deletes the selected order after permission and confirmation checks.
     */
    private void onDelete() {
        if (!RoleGuard.confirmDeleteAllowed(i18n)) {
            return;
        }
        int id = parseId(orderIdField.getText());
        if (id <= 0) {
            showInfo("alert.order.select.title", "alert.order.select.resolve");
            return;
        }

        if (orderDao.getPaidById(id).isEmpty()) {
            showInfo("alert.order.select.title", "alert.order.not.found");
            return;
        }

        if (!RoleGuard.confirmDelete(i18n, i18n.getString("delete.item.order"))) {
            return;
        }
        orderDao.deletePaidByID(id);
        reloadFromDatabase();
        prepareBlankOrderForm();
        orderTable.getSelectionModel().clearSelection();
    }

    /**
     * Parses the order form and returns a clean value object when everything is valid.
     */
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

    /**
     * Converts database status values into translated labels for the UI.
     */
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

    /**
     * Converts translated status labels back to database values.
     */
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

    /**
     * Clean, validated order form values ready to be saved.
     */
    private record ParsedOrderForm(String date, int customerId, int comicId, String status, int quantity) {
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
