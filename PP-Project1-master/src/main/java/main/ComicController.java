package main;

import Comic.Comic;
import Comic.ComicDAO;
import Comic.ComicDAOImpl;
import I18n.I18nManager;
import auth.RoleGuard;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import util.DatabaseException;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Controller for the comic inventory screen.
 * It handles table display, search filters, form validation, and admin-only delete behavior.
 */
public class ComicController {
    private final I18nManager i18n = I18nManager.getInstance();

    @FXML
    private TextField idField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField issueField;
    @FXML
    private TextField publisherField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField searchIdField;
    @FXML
    private TextField searchTitleField;
    @FXML
    private TextField searchIssueField;
    @FXML
    private TextField searchPublisherField;
    @FXML
    private Button searchButton;
    @FXML
    private Button showAllButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Comic> comicTable;
    @FXML
    private TableColumn<Comic, String> idColumn;
    @FXML
    private TableColumn<Comic, String> titleColumn;
    @FXML
    private TableColumn<Comic, String> issueColumn;
    @FXML
    private TableColumn<Comic, String> publisherColumn;
    @FXML
    private TableColumn<Comic, Double> priceColumn;
    @FXML
    private TableColumn<Comic, Integer> stockColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label totalComicsLabel;

    private final ComicDAO dao = new ComicDAOImpl();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
    private ObservableList<Comic> allComics;
    private FilteredList<Comic> filteredComics;

    @FXML
    /**
     * Connects table columns, search listeners, form buttons, and delete permissions.
     */
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(formatFriendlyId("CB", cd.getValue().getComicID())));
        titleColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        issueColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getIssue()));
        publisherColumn.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPublisher()));
        priceColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getPrice()));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(money.format(price));
                }
            }
        });
        stockColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStock()));

        // Initialize empty table - no data loading until search
        allComics = FXCollections.observableArrayList();
        filteredComics = new FilteredList<>(allComics, p -> false); // Start with no results
        comicTable.setItems(filteredComics);
        totalComicsLabel.setText("0");

        comicTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, c) -> {
            if (c != null) {
                idField.setText(Integer.toString(c.getComicID()));
                titleField.setText(c.getName());
                issueField.setText(c.getIssue());
                publisherField.setText(c.getPublisher());
                priceField.setText(formatPriceForField(c.getPrice()));
                stockField.setText(Integer.toString(c.getStock()));
            }
        });

        searchButton.setOnAction(e -> runDatabaseAction(this::applySearchFilter));
        showAllButton.setOnAction(e -> runDatabaseAction(this::showAllComics));
        clearButton.setOnAction(e -> {
            clearSearchFields();
            runDatabaseAction(this::applySearchFilter);
        });
        searchIdField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchTitleField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchIssueField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));
        searchPublisherField.textProperty().addListener((o, a, b) -> runDatabaseAction(this::applySearchFilter));

        addButton.setOnAction(e -> runDatabaseAction(this::onAdd));
        updateButton.setOnAction(e -> runDatabaseAction(this::onUpdate));
        deleteButton.setOnAction(e -> runDatabaseAction(this::onDelete));
        refreshButton.setOnAction(e -> runDatabaseAction(this::reloadFromDatabase));
        RoleGuard.applyDeletePermission(deleteButton);
    }

    /**
     * Keeps price fields readable without unnecessary trailing decimals.
     */
    private static String formatPriceForField(double p) {
        if (Math.abs(p - Math.rint(p)) < 1e-6) {
            return String.valueOf((int) Math.rint(p));
        }
        return String.format(Locale.US, "%.2f", p);
    }

    /**
     * Reloads comics after a write operation.
     */
    private void reloadFromDatabase() {
        showAllComics();
    }

    /**
     * Shows every comic in the table.
     */
    private void showAllComics() {
        List<Comic> rows = dao.getAllComics();
        allComics = FXCollections.observableArrayList(rows);
        filteredComics = new FilteredList<>(allComics, x -> true);
        comicTable.setItems(filteredComics);
        totalComicsLabel.setText(Integer.toString(allComics.size()));
    }

    /**
     * Applies the current comic search fields.
     */
    private void applySearchFilter() {
        String id = normalized(searchIdField);
        String title = normalized(searchTitleField);
        String issue = normalized(searchIssueField);
        String publisher = normalized(searchPublisherField);
        if (id.isEmpty() && title.isEmpty() && issue.isEmpty() && publisher.isEmpty()) {
            allComics.clear();
            filteredComics.setPredicate(comic -> false);
            totalComicsLabel.setText("0");
            return;
        }

        List<Comic> comics = dao.getAllComics();
        allComics = FXCollections.observableArrayList(comics);
        filteredComics = new FilteredList<>(allComics, p -> false);
        comicTable.setItems(filteredComics);
        
        Predicate<Comic> match = c -> {
            return containsIfPresent(Integer.toString(c.getComicID()), id)
                    && containsIfPresent(safeLower(c.getName()), title)
                    && containsIfPresent(safeLower(c.getIssue()), issue)
                    && containsIfPresent(safeLower(c.getPublisher()), publisher);
        };
        filteredComics.setPredicate(match);
        totalComicsLabel.setText(Integer.toString(filteredComics.size()));
    }

    /**
     * Clears the comic search fields.
     */
    private void clearSearchFields() {
        searchIdField.clear();
        searchTitleField.clear();
        searchIssueField.clear();
        searchPublisherField.clear();
    }

    /**
     * Validates the form and inserts a new comic.
     */
    private void onAdd() {
        if (!validateCoreFields()) {
            return;
        }
        Double price = parsePrice();
        if (price == null) {
            return;
        }
        Integer stock = parseStock();
        if (stock == null) {
            return;
        }
        Comic c = new Comic(0,
                titleField.getText().trim(),
                issueField.getText().trim(),
                publisherField.getText().trim(),
                price,
                stock);
        dao.saveComic(c);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Validates the form and updates the selected comic.
     */
    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("alert.comic.select.title", "alert.comic.select.update");
            return;
        }
        if (!validateCoreFields()) {
            return;
        }
        Double price = parsePrice();
        if (price == null) {
            return;
        }
        Integer stock = parseStock();
        if (stock == null) {
            return;
        }
        Comic c = new Comic(id,
                titleField.getText().trim(),
                issueField.getText().trim(),
                publisherField.getText().trim(),
                price,
                stock);
        dao.updateComic(c);
        reloadFromDatabase();
    }

    /**
     * Deletes the selected comic after permission and confirmation checks.
     */
    private void onDelete() {
        if (!RoleGuard.confirmDeleteAllowed(i18n)) {
            return;
        }
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("alert.comic.select.title", "alert.comic.select.remove");
            return;
        }
        if (!RoleGuard.confirmDelete(i18n, i18n.getString("delete.item.comic"))) {
            return;
        }
        dao.deleteComicByID(id);
        reloadFromDatabase();
        clearForm();
    }

    /**
     * Checks required comic text fields before saving.
     */
    private boolean validateCoreFields() {
        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        String iss = issueField.getText() == null ? "" : issueField.getText().trim();
        String pub = publisherField.getText() == null ? "" : publisherField.getText().trim();
        if (t.isEmpty()) {
            showInfo("alert.comic.title.title", "alert.comic.title.required");
            return false;
        }
        if (iss.isEmpty()) {
            showInfo("alert.comic.issue.title", "alert.comic.issue.required");
            return false;
        }
        if (pub.isEmpty()) {
            showInfo("alert.comic.publisher.title", "alert.comic.publisher.required");
            return false;
        }
        return true;
    }

    /**
     * Parses and validates the price field.
     */
    private Double parsePrice() {
        String raw = priceField.getText() == null ? "" : priceField.getText().trim().replace(',', '.');
        if (raw.isEmpty()) {
            showInfo("alert.comic.price.title", "alert.comic.price.required");
            return null;
        }
        try {
            double p = Double.parseDouble(raw);
            if (p < 0 || p > 99999) {
                showInfo("alert.comic.price.title", "alert.comic.price.range");
                return null;
            }
            return p;
        } catch (NumberFormatException e) {
            showInfo("alert.comic.price.title", "alert.comic.price.number");
            return null;
        }
    }

    /**
     * Parses and validates the stock field.
     */
    private Integer parseStock() {
        String raw = stockField.getText() == null ? "" : stockField.getText().trim();
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            int n = Integer.parseInt(raw);
            if (n < 0 || n > 1_000_000) {
                showInfo("alert.comic.stock.title", "alert.comic.stock.range");
                return null;
            }
            return n;
        } catch (NumberFormatException e) {
            showInfo("alert.comic.stock.title", "alert.comic.stock.number");
            return null;
        }
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
     * Formats database IDs for display, such as CB1.
     */
    private static String formatFriendlyId(String prefix, int id) {
        return prefix + id;
    }

    /**
     * Clears the comic edit form and current table selection.
     */
    private void clearForm() {
        idField.clear();
        titleField.clear();
        issueField.clear();
        publisherField.clear();
        priceField.clear();
        stockField.clear();
        comicTable.getSelectionModel().clearSelection();
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
