package com.ecom.controllers;

// LoginController.java
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }

        // Add your authentication logic here
        // Example: authenticate(email, password)
        showAlert(Alert.AlertType.INFORMATION, "Success",
                "Login successful!\nEmail: " + email);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Navigate to forgot password page
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password",
                "Redirect to password reset page");
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        // Navigate to signup page
        showAlert(Alert.AlertType.INFORMATION, "Sign Up",
                "Redirect to registration page");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
