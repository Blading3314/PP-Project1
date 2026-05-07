package main;

import I18n.I18nManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;

public class TestFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        I18nManager i18n = I18nManager.getInstance();
        ResourceBundle bundle = i18n.getResourceBundle();
        
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/main-view.fxml")), bundle);
        Scene scene = new Scene(loader.load(), 1100, 720);

        stage.setTitle(i18n.getString("main.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
