package com.ecom.controllers;

import com.ecom.services.UserService;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import com.ecom.models.User;
import java.util.regex.Pattern;


public class SignUpController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button SignUpButton;
    
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            NavigationUtils.navigate("login");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
    
    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        User user = new User();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String phone = phoneField.getText().trim();
        String username = usernameField.getText().trim();
        
        // Validate all fields are filled
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address");
            return;
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 8 characters long");
            return;
        }
        
        // Set user properties
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password); // Will be hashed in UserService
        
        // Attempt to sign up
        if (UserService.signUp(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "User creation successful!");
            try {
                NavigationUtils.navigate("product");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Signup failed. Email may already be registered.");
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
