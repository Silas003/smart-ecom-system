package com.ecom.controllers;

// CartController.java
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    @FXML
    public void initialize() {
        updateCartSummary();
    }

    @FXML
    private void handleContinueShopping() {
        // Navigate back to catalog
    }

    @FXML
    private void handleCheckout() {
        // Navigate to checkout page
    }

    private void updateCartSummary() {
        // Calculate totals
        double subtotal = 0.0;
        double tax = subtotal * 0.08; // 8% tax
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
    }
}
