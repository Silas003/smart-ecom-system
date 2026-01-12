package com.ecom.controllers;

import com.ecom.models.Product;
import com.ecom.services.CartService;
import com.ecom.services.OrderService;
import com.ecom.services.ProductService;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;

    private CartService cartService;
    private OrderService orderService;
    private ProductService productService;
    private SessionService sessionService;

    @FXML
    public void initialize() {
        cartService = CartService.getInstance();
        orderService = new OrderService();
        productService = new ProductService();
        sessionService = SessionService.getInstance();
        

        if (!sessionService.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to checkout.");
            try {
                NavigationUtils.navigate("login");
            } catch (IOException e) {

            }
            return;
        }
        
        paymentMethodCombo.getItems().addAll("Credit Card", "Debit Card", "PayPal");
        paymentMethodCombo.setValue("Credit Card");
        
        updateOrderSummary();
    }

    @FXML
    private void handlePlaceOrder() {
        if (fullNameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty() ||
            cityField.getText().trim().isEmpty() ||
            stateField.getText().trim().isEmpty() ||
            zipField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields");
            return;
        }


        if (paymentMethodCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a payment method");
            return;
        }

        // Validate card details if credit/debit card
        if (paymentMethodCombo.getValue().contains("Card")) {
            if (cardNumberField.getText().trim().isEmpty() ||
                expiryField.getText().trim().isEmpty() ||
                cvvField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all payment details");
                return;
            }
        }

        if (cartService.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty. Add items before placing an order.");
            return;
        }

        Map<Product, Integer> cartItems = new HashMap<>();
        Map<Integer, Integer> cart = cartService.getCart();
        
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            try {
                Product product = productService.getProductById(entry.getKey());
                if (product != null) {
                    cartItems.put(product, entry.getValue());
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load product: " + e.getMessage());
                return;
            }
        }


        try {
            int userId = sessionService.getCurrentUserId();
            if (userId == 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "User session expired. Please log in again.");
                try {
                    NavigationUtils.navigate("login");
                } catch (IOException e) {

                }
                return;
            }
            boolean success = orderService.checkout(userId, cartItems);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order placed successfully!");
                cartService.clearCart();
                NavigationUtils.navigate("product");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to place order. Please try again.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Order placement failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToCart() {
        try {
            NavigationUtils.navigate("cart");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (com.ecom.utils.NavigationUtils.canGoBack()) {
                com.ecom.utils.NavigationUtils.goBack();
            } else {
                handleBackToCart();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void updateOrderSummary() {
        if (subtotalLabel == null || taxLabel == null || orderTotalLabel == null) {
            return;
        }
        
        Map<Integer, Integer> cart = cartService.getCart();
        double subtotal = 0.0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            try {
                Product product = productService.getProductById(entry.getKey());
                if (product != null) {
                    subtotal += product.getPrice() * entry.getValue();
                }
            } catch (SQLException e) {
                System.err.println("Error calculating subtotal: " + e.getMessage());
            }
        }

        double tax = subtotal * 0.08; // 8% tax
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        orderTotalLabel.setText(String.format("$%.2f", total));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
