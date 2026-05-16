package main;

import I18n.I18nManager;
import auth.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Controller for the main application shell.
 * It switches between modules, reloads screens when the language changes, and handles logout.
 */
public class MainController {

    private static final String PREFS_FILE = ".pp-project1-workspace.properties";
    private static final String KEY_LAST_MODULE = "lastModule";

    @FXML
    private StackPane contentStack;

    @FXML
    private ToggleGroup moduleGroup;

    @FXML
    private ToggleButton customersToggle;

    @FXML
    private ToggleButton employeesToggle;

    @FXML
    private ToggleButton comicsToggle;

    @FXML
    private ToggleButton ordersToggle;

    @FXML
    private ComboBox<String> languageSelector;
    @FXML
    private Button logoutButton;

    private final Map<Module, Node> moduleRoots = new EnumMap<>(Module.class);
    private final Path prefsPath = Path.of(System.getProperty("user.home"), PREFS_FILE);
    private I18nManager i18n = I18nManager.getInstance();

    private enum Module {
        CUSTOMERS("customers", "/customer-view.fxml"),
        EMPLOYEES("employees", "/employee-view.fxml"),
        COMICS("comics", "/comic-view.fxml"),
        ORDERS("orders", "/order-view.fxml");

        final String prefsKey;
        final String resource;

        Module(String prefsKey, String resource) {
            this.prefsKey = prefsKey;
            this.resource = resource;
        }
    }

    @FXML
    /**
     * Wires the navigation, language selector, logout, and last opened module.
     */
    private void initialize() {
        initializeLanguageSelector();
        logoutButton.setOnAction(e -> logout());
        
        bindToggle(Module.CUSTOMERS, customersToggle);
        bindToggle(Module.EMPLOYEES, employeesToggle);
        bindToggle(Module.COMICS, comicsToggle);
        bindToggle(Module.ORDERS, ordersToggle);

        moduleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                if (oldT != null) {
                    Platform.runLater(() -> moduleGroup.selectToggle(oldT));
                }
                return;
            }
            Module mod = (Module) newT.getUserData();
            showModule(mod);
            saveLastModule(mod);
        });

        Module initial = readLastModule();
        Toggle initialToggle = switch (initial) {
            case EMPLOYEES -> employeesToggle;
            case COMICS -> comicsToggle;
            case ORDERS -> ordersToggle;
            default -> customersToggle;
        };
        Platform.runLater(() -> {
            moduleGroup.selectToggle(initialToggle);
            if (!moduleRoots.containsKey(initial)) {
                showModule(initial);
            }
        });
    }

    /**
     * Connects the language dropdown to the shared I18n manager.
     */
    private void initializeLanguageSelector() {
        languageSelector.getItems().addAll("English", "Français", "Español");
        
        // Set current locale
        Locale current = i18n.getCurrentLocale();
        if (current.equals(Locale.ENGLISH)) {
            languageSelector.getSelectionModel().select("English");
        } else if (current.equals(Locale.FRENCH)) {
            languageSelector.getSelectionModel().select("Français");
        } else if (current.getLanguage().equals("es")) {
            languageSelector.getSelectionModel().select("Español");
        }
        
        languageSelector.setOnAction(event -> {
            String selected = languageSelector.getSelectionModel().getSelectedItem();
            switch (selected) {
                case "English" -> i18n.setEnglish();
                case "Français" -> i18n.setFrench();
                case "Español" -> i18n.setSpanish();
            }
            refreshAllModules();
        });
    }

    /**
     * Reloads the visible modules so all labels use the newly selected language.
     */
    private void refreshAllModules() {
        moduleRoots.clear();
        contentStack.getChildren().clear();
        
        // Update toggle button text
        updateToggleButtonText();
        
        // Reload current module
        Toggle selectedToggle = moduleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            Module mod = (Module) selectedToggle.getUserData();
            showModule(mod);
        }
    }

    /**
     * Updates sidebar text after a language change.
     */
    private void updateToggleButtonText() {
        customersToggle.setText(i18n.getString("main.customers"));
        employeesToggle.setText(i18n.getString("main.employees"));
        comicsToggle.setText(i18n.getString("main.comics"));
        ordersToggle.setText(i18n.getString("main.orders"));
        logoutButton.setText(i18n.getString("main.logout"));
    }

    /**
     * Ends the current session and returns to the login screen.
     */
    private void logout() {
        UserSession.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"), i18n.getResourceBundle());
            Scene scene = new Scene(loader.load(), 720, 520);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to return to login view", e);
        }
    }

    /**
     * Attaches a module value to one sidebar toggle button.
     */
    private void bindToggle(Module mod, ToggleButton btn) {
        btn.setUserData(mod);
        btn.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Shows the selected module and hides the other loaded modules.
     */
    private void showModule(Module mod) {
        Node root = moduleRoots.computeIfAbsent(mod, this::loadModuleFxml);
        if (!contentStack.getChildren().contains(root)) {
            contentStack.getChildren().add(root);
        }
        for (Node child : contentStack.getChildren()) {
            boolean active = child == root;
            child.setVisible(active);
            child.setManaged(active);
        }
    }

    /**
     * Loads a module FXML file using the current language bundle.
     */
    private Node loadModuleFxml(Module mod) {
        try {
            I18nManager i18n = I18nManager.getInstance();
            ResourceBundle bundle = i18n.getResourceBundle();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(mod.resource), bundle);
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + mod.resource, e);
        }
    }

    /**
     * Restores the last module the user opened, when that preference exists.
     */
    private Module readLastModule() {
        Properties p = new Properties();
        if (Files.isRegularFile(prefsPath)) {
            try (InputStream in = Files.newInputStream(prefsPath)) {
                p.load(in);
                String key = p.getProperty(KEY_LAST_MODULE, Module.CUSTOMERS.prefsKey);
                for (Module m : Module.values()) {
                    if (m.prefsKey.equals(key)) {
                        return m;
                    }
                }
            } catch (IOException ignored) {
                // fall through
            }
        }
        return Module.CUSTOMERS;
    }

    /**
     * Saves the selected module so the app can reopen there next time.
     */
    private void saveLastModule(Module mod) {
        Properties p = new Properties();
        if (Files.isRegularFile(prefsPath)) {
            try (InputStream in = Files.newInputStream(prefsPath)) {
                p.load(in);
            } catch (IOException ignored) {
                // overwrite
            }
        }
        p.setProperty(KEY_LAST_MODULE, mod.prefsKey);
        try (OutputStream out = Files.newOutputStream(prefsPath)) {
            p.store(out, "PP Project 1 — last opened space (Stage Manager)");
        } catch (IOException ignored) {
            // non-fatal
        }
    }
}
