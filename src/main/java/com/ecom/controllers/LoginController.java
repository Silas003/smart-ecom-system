package com.ecom.controllers;

import com.ecom.models.User;
import com.ecom.services.UserService;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import com.ecom.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import com.ecom.exceptions.*;

/**
 * Controller for the login screen. Handles user authentication and navigation after login.
 */
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

        try {

            ValidationUtils.requireNonEmpty(email, "email");
            ValidationUtils.requireNonEmpty(password, "password");
            ValidationUtils.requireEmail(email, "email");
        } catch (ValidationException ve) {
            showAlert(Alert.AlertType.ERROR, "Validation error", ve.getMessage());
            return;
        }


        try {
            User user = UserService.login(email, password);
            if(user != null){

                SessionService.getInstance().setCurrentUser(user);
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Login successful!\nEmail: " + email);
                try {

                    SessionService session = SessionService.getInstance();
                    String pending = session.getPendingFxml();
                    if (pending != null && !pending.isBlank()) {
                        if (com.ecom.utils.NavigationUtils.fxmlExists(pending)) {
                            session.clearPendingFxml();
                            NavigationUtils.navigate(pending);
                            return;
                        } else {
                            session.clearPendingFxml();
                            showAlert(Alert.AlertType.WARNING, "Navigation Error", "The requested page is unavailable: " + pending);
                        }
                    }

                    if (user.getRole().equalsIgnoreCase("admin")) {
                        NavigationUtils.navigate("adminDashboard");
                    } else {
                        NavigationUtils.navigate("product");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                     showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());

                }
            }
        } catch (InvalidInputException iie) {
            showAlert(Alert.AlertType.ERROR, "Validation error", iie.getMessage());
        } catch (AuthenticationException ae) {
            showAlert(Alert.AlertType.ERROR, "Authentication failed", ae.getMessage());
        } catch (DaoException de) {
            showAlert(Alert.AlertType.ERROR, "Error", "Login failed due to a server error.");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
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
