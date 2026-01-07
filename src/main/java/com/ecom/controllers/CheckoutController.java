package com.ecom.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CheckoutController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextField zipField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private Label orderTotalLabel;

    @FXML
    public void initialize() {
        paymentMethodCombo.getItems().addAll("Credit Card", "Debit Card", "PayPal");
        paymentMethodCombo.setValue("Credit Card");
    }

    @FXML
    private void handlePlaceOrder() {
        // Validate all fields
        if (fullNameField.getText().isEmpty() || emailField.getText().isEmpty() ||
                addressField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields");
            return;
        }

        // Process order
        showAlert(Alert.AlertType.INFORMATION, "Success", "Order placed successfully!");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
