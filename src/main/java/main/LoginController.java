package main;

import com.sun.tools.javac.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    public Button loginButton;
    public PasswordField passwordField;
    public TextField usernameField;

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> login());

    }
    public void login() {


    }
}
