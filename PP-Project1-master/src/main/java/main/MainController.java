package main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

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

    private final Map<Module, Node> moduleRoots = new EnumMap<>(Module.class);
    private final Path prefsPath = Path.of(System.getProperty("user.home"), PREFS_FILE);

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
    private void initialize() {
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

    private void bindToggle(Module mod, ToggleButton btn) {
        btn.setUserData(mod);
        btn.setMaxWidth(Double.MAX_VALUE);
    }

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

    private Node loadModuleFxml(Module mod) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(mod.resource));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + mod.resource, e);
        }
    }

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
