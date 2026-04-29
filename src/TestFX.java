import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class TestFX extends Application{
  @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Button("OK"), 300, 200));
        stage.show();
    }



    public static void main(String[] args) {
        launch();
        System.out.println(System.getProperty("java.version"));
    }
}