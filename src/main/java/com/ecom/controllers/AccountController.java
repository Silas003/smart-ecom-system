package com.ecom.controllers;

import com.ecom.models.User;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;

import java.io.IOException;

public class AccountController {

    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;

    private SessionService session = SessionService.getInstance();

    @FXML
    public void initialize() {
        loadUser();
    }

    private void loadUser() {
        User user = session.getCurrentUser();
        if (user == null) return;
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        phoneLabel.setText(user.getPhone() == null ? "" : user.getPhone());
        roleLabel.setText(user.getRole());
    }

    @FXML
    private void handleEditProfile() {
        // For now navigate to signup as a profile editor (reuse form)
        try {
            NavigationUtils.navigate("signup");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open profile editor: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewOrders() {
        try {
            NavigationUtils.navigate("myorders");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open orders: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (NavigationUtils.canGoBack()) NavigationUtils.goBack();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to go back: " + e.getMessage());
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

