package main;

import I18n.I18nManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;

/**
 * JavaFX entry point for the program.
 * The app starts at the login screen and moves to the main workspace after authentication.
 */
public class TestFX extends Application {
    @Override
    /**
     * Builds the first JavaFX scene and opens the login screen.
     */
    public void start(Stage stage) throws Exception {
        I18nManager i18n = I18nManager.getInstance();
        ResourceBundle bundle = i18n.getResourceBundle();
        
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/login-view.fxml")), bundle);
        Scene scene = new Scene(loader.load(), 720, 520);

        stage.setTitle(i18n.getString("main.title"));
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches JavaFX from a normal Java main method.
     */
    public static void main(String[] args) {
        launch();
    }
}
