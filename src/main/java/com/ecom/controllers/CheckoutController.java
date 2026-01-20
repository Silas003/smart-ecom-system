package com.ecom.controllers;

import com.ecom.models.Product;
import com.ecom.services.CartService;
import com.ecom.services.OrderService;
import com.ecom.services.ProductService;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import com.ecom.dao.InventoryDao;
import com.ecom.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CheckoutController {

    @FXML private TextField cityField;
    @FXML private TextField stateField;
    @FXML private TextArea addressField;
    @FXML private TextField zipField;
    @FXML private Label orderTotalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private VBox orderItemsBox;
    @FXML private Button placeOrderBtn;

    private CartService cartService;
    private OrderService orderService;
    private ProductService productService;
    private SessionService sessionService;
    private InventoryDao inventoryDao;

    @FXML
    public void initialize() {
        cartService = CartService.getInstance();
        orderService = new OrderService();
        productService = new ProductService();
        sessionService = SessionService.getInstance();
        inventoryDao = new InventoryDao();

        if (!sessionService.isLoggedIn()) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "Please log in to checkout.");
            try {
                NavigationUtils.navigate("login");
            } catch (IOException e) {

            }
            return;
        }
        updateOrderSummary();
    }

    @FXML
    private void handlePlaceOrder() {
        if (
            cityField.getText().trim().isEmpty() ||
            stateField.getText().trim().isEmpty() ||
            zipField.getText().trim().isEmpty() ||
            addressField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields");
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
            String city = cityField.getText().trim();
            String region = stateField.getText().trim();
            String zip = zipField.getText().trim();
            String address = addressField.getText().trim();
            ValidationUtils.validateAdress(address,region ,city,zip);
            boolean success = orderService.checkout(userId, cartItems, city, region, zip,address);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order placed successfully!");
                cartService.clearCart();
                NavigationUtils.navigate("product");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to place order. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            if (NavigationUtils.canGoBack()) {
                NavigationUtils.goBack();
            } else {
                handleBackToCart();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void updateOrderSummary() {
        if (subtotalLabel == null || taxLabel == null || orderTotalLabel == null || orderItemsBox == null || placeOrderBtn == null) {
            return;
        }

        orderItemsBox.getChildren().clear();

        Map<Integer, Integer> cart = cartService.getCart();
        double subtotal = 0.0;
        boolean allAvailable = true;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            try {
                Product product = productService.getProductById(entry.getKey());
                if (product != null) {
                    int qty = entry.getValue();
                    double lineTotal = product.getPrice() * qty;
                    subtotal += lineTotal;

                    int available = inventoryDao.getStock(product.getProductId());

                    HBox line = new HBox(8);
                    Label name = new Label(product.getName());
                    name.setFont(Font.font(13));
                    HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

                    Label qtyLabel = new Label("x" + qty);
                    Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
                    Label lineTotalLabel = new Label(String.format("$%.2f", lineTotal));

                    Label availLabel = new Label();
                    if (available >= qty) {
                        availLabel.setText("In stock");
                    } else if (available > 0) {
                        availLabel.setText("Only " + available + " left");
                        allAvailable = false;
                    } else {
                        availLabel.setText("Out of stock");
                        allAvailable = false;
                    }

                    line.getChildren().addAll(name, qtyLabel, priceLabel, lineTotalLabel, availLabel);
                    orderItemsBox.getChildren().add(line);
                }
            } catch (SQLException e) {
                System.err.println("Error building order summary: " + e.getMessage());
            }
        }

        double tax = subtotal * 0.08;
        double total = subtotal + tax ;



        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        orderTotalLabel.setText(String.format("$%.2f", total));

        placeOrderBtn.setDisable(!allAvailable || cart.isEmpty());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
