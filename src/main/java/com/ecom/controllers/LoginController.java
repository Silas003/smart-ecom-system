package com.ecom.controllers;

import com.ecom.models.User;
import com.ecom.services.UserService;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.IOException;


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
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }
        
        // Validate email format
        if (!email.contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address");
            return;
        }

        // Authenticate user
        User user = UserService.login(email, password);
        if(user != null){
            // Store user in session
            SessionService.getInstance().setCurrentUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Login successful!\nEmail: " + email);
            try {
                if (user.getRole().equalsIgnoreCase("admin")) {
                    NavigationUtils.navigate("adminDashboard");
                } else {
                    NavigationUtils.navigate("product");
                }   
            } catch (IOException e) {
                // showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleSignup(ActionEvent event) {
       try {
        NavigationUtils.navigate("signup");
       } catch (IOException e) {
        System.out.println(e.getMessage());
       }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
