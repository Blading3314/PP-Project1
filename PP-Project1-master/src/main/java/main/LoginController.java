package main;

import I18n.I18nManager;
import auth.Role;
import auth.UserAccountDAO;
import auth.UserAccountDAOImpl;
import auth.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.DatabaseException;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for the login screen.
 * It authenticates the user, lets them pick a language before login, and opens the main app on success.
 */
public class LoginController {
    private final I18nManager i18n = I18nManager.getInstance();
    private UserAccountDAO userAccountDao;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label demoAccountsLabel;
    @FXML
    private ComboBox<String> languageSelector;

    @FXML
    /**
     * Prepares login controls, language switching, and the login DAO.
     */
    private void initialize() {
        initializeLanguageSelector();
        demoAccountsLabel.setText(i18n.getString("login.demo.accounts"));
        try {
            userAccountDao = new UserAccountDAOImpl();
        } catch (DatabaseException e) {
            showDatabaseError(e);
            loginButton.setDisable(true);
            return;
        }
        loginButton.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
    }

    /**
     * Lets users switch the language before they sign in.
     */
    private void initializeLanguageSelector() {
        languageSelector.getItems().addAll("English", "Français", "Español");

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
            if (selected == null) {
                return;
            }
            switch (selected) {
                case "English" -> i18n.setEnglish();
                case "Français" -> i18n.setFrench();
                case "Español" -> i18n.setSpanish();
                default -> {
                    return;
                }
            }
            reloadLoginView();
        });
    }

    /**
     * Attempts login and opens the main workspace when the credentials are valid.
     */
    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        Optional<Role> role;
        try {
            role = userAccountDao.authenticate(username, password);
        } catch (DatabaseException e) {
            showDatabaseError(e);
            return;
        }
        if (role.isEmpty()) {
            showLoginError();
            return;
        }

        UserSession.login(username, role.get());
        openMainView();
    }

    /**
     * Replaces the login scene with the main app scene after successful login.
     */
    private void openMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/main-view.fxml")),
                    i18n.getResourceBundle());
            Scene scene = new Scene(loader.load(), 1100, 720);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle(i18n.getString("main.title"));
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open main view", e);
        }
    }

    /**
     * Reloads this screen with the newly selected language bundle.
     */
    private void reloadLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/login-view.fxml")),
                    i18n.getResourceBundle());
            Scene scene = new Scene(loader.load(), 720, 520);
            Stage stage = (Stage) languageSelector.getScene().getWindow();
            stage.setTitle(i18n.getString("main.title"));
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Failed to reload login view", e);
        }
    }

    /**
     * Shows a friendly message when the username or password is wrong.
     */
    private void showLoginError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(i18n.getString("login.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18n.getString("login.error.message"));
        alert.showAndWait();
    }

    /**
     * Shows login-related database failures in the selected language.
     */
    private void showDatabaseError(DatabaseException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(i18n.getString("alert.database.title"));
        alert.setHeaderText(null);
        alert.setContentText(i18n.getString(e.getMessageKey(), e.getOperation()));
        alert.showAndWait();
    }
}
