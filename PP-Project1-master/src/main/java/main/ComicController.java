package main;

import Comic.Comic;
import Comic.ComicDAO;
import Comic.ComicDAOImpl;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class ComicController {

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
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearButton;
    @FXML
    private TableView<Comic> comicTable;
    @FXML
    private TableColumn<Comic, Integer> idColumn;
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
    private void initialize() {
        idColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getComicID()));
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

        reloadFromDatabase();

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

    private static String formatPriceForField(double p) {
        if (Math.abs(p - Math.rint(p)) < 1e-6) {
            return String.valueOf((int) Math.rint(p));
        }
        return String.format(Locale.US, "%.2f", p);
    }

    private void reloadFromDatabase() {
        List<Comic> rows = dao.getAllComics();
        allComics = FXCollections.observableArrayList(rows);
        filteredComics = new FilteredList<>(allComics, x -> true);
        comicTable.setItems(filteredComics);
        applySearchFilter();
        totalComicsLabel.setText(Integer.toString(allComics.size()));
    }

    private void applySearchFilter() {
        if (filteredComics == null) {
            return;
        }
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filteredComics.setPredicate(c -> true);
            totalComicsLabel.setText(Integer.toString(allComics.size()));
            return;
        }
        Predicate<Comic> match = c -> {
            String blob = (c.getName() + " " + c.getIssue() + " " + c.getPublisher() + " " + c.getComicID())
                    .toLowerCase(Locale.ROOT);
            return blob.contains(q);
        };
        filteredComics.setPredicate(match);
        totalComicsLabel.setText(Integer.toString(filteredComics.size()));
    }

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

    private void onUpdate() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("Select a comic", "Choose a row in the table to update.");
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

    private void onDelete() {
        int id = parseId(idField.getText());
        if (id <= 0) {
            showInfo("Select a comic", "Pick a row or enter a valid comic ID to remove.");
            return;
        }
        dao.deleteComicByID(id);
        reloadFromDatabase();
        clearForm();
    }

    private boolean validateCoreFields() {
        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        String iss = issueField.getText() == null ? "" : issueField.getText().trim();
        String pub = publisherField.getText() == null ? "" : publisherField.getText().trim();
        if (t.isEmpty()) {
            showInfo("Title required", "Every comic needs a title.");
            return false;
        }
        if (iss.isEmpty()) {
            showInfo("Issue required", "Enter an issue label such as #1, Vol 2, or GN.");
            return false;
        }
        if (pub.isEmpty()) {
            showInfo("Publisher required", "Who printed this? (e.g. Marvel, Image, Viz.)");
            return false;
        }
        return true;
    }

    private Double parsePrice() {
        String raw = priceField.getText() == null ? "" : priceField.getText().trim().replace(',', '.');
        if (raw.isEmpty()) {
            showInfo("Price", "Enter a list price (0 is allowed for bundles or promos).");
            return null;
        }
        try {
            double p = Double.parseDouble(raw);
            if (p < 0 || p > 99999) {
                showInfo("Price", "Use a realistic shelf price (0 – 99,999).");
                return null;
            }
            return p;
        } catch (NumberFormatException e) {
            showInfo("Price", "Use numbers only, e.g. 4.99 or 19");
            return null;
        }
    }

    private Integer parseStock() {
        String raw = stockField.getText() == null ? "" : stockField.getText().trim();
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            int n = Integer.parseInt(raw);
            if (n < 0 || n > 1_000_000) {
                showInfo("Stock", "Stock should be between 0 and 1,000,000.");
                return null;
            }
            return n;
        } catch (NumberFormatException e) {
            showInfo("Stock", "Enter a whole number for copies on hand.");
            return null;
        }
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
        titleField.clear();
        issueField.clear();
        publisherField.clear();
        priceField.clear();
        stockField.clear();
        comicTable.getSelectionModel().clearSelection();
    }

    private static void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
