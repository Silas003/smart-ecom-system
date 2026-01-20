package com.ecom.controllers;

import com.ecom.models.Product;
import com.ecom.services.CartService;
import com.ecom.services.ProductService;
import com.ecom.services.SessionService;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Controller for the shopping cart screen; displays cart items and allows checkout navigation.
 */
public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private CartService cartService;
    private ProductService productService;

    @FXML
    public void initialize() {
        cartService = CartService.getInstance();
        productService = new ProductService();
        loadCartItems();
        updateCartSummary();
    }

    @FXML
    private void handleContinueShopping() {
        try {
            NavigationUtils.navigate("product");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartService.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Your cart is empty. Add items before checkout.");
            return;
        }
        try {

            SessionService session = SessionService.getInstance();
            if (!session.isLoggedIn()) {
                System.out.println("CartController: user not logged in, setting pendingFxml=checkout and navigating to login");
                session.setPendingFxml("checkout");
                NavigationUtils.navigate("login");
                return;
            } else {
                session.clearPendingFxml();
            }

            NavigationUtils.navigate("checkout");
        } catch (IOException e) {
             showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveItem(int productId) {
        cartService.removeFromCart(productId);
        loadCartItems();
        updateCartSummary();
    }

    @FXML
    private void handleUpdateQuantity(int productId, int newQuantity) {
        if (newQuantity <= 0) {
            handleRemoveItem(productId);
        } else {
            try {
                Product product = productService.getProductById(productId);
                if (product != null && newQuantity > product.getStockQuantity()) {
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                        "Only " + product.getStockQuantity() + " items available in stock.");
                    return;
                }
                cartService.updateQuantity(productId, newQuantity);
                loadCartItems();
                updateCartSummary();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update quantity: " + e.getMessage());
            }
        }
    }

    private void loadCartItems() {
        cartItemsContainer.getChildren().clear();
        Map<Integer, Integer> cart = cartService.getCart();

        if (cart.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666;");
            cartItemsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            try {
                Product product = productService.getProductById(entry.getKey());
                if (product != null) {
                    HBox cartItem = createCartItemCard(product, entry.getValue());
                    cartItemsContainer.getChildren().add(cartItem);
                }
            } catch (SQLException e) {
                System.err.println("Error loading product: " + e.getMessage());
            }
        }
    }

    private HBox createCartItemCard(Product product, int quantity) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setAlignment(Pos.CENTER_LEFT);

        Label imagePlaceholder = new Label("[Image]");
        imagePlaceholder.setPrefWidth(100);
        imagePlaceholder.setPrefHeight(100);
        imagePlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-alignment: center;");


        VBox details = new VBox(5);
        details.setPrefWidth(300);
        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #007bff; -fx-font-size: 14px;");
        
        Label stockLabel = new Label("Stock: " + product.getStockQuantity());
        stockLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        details.getChildren().addAll(nameLabel, priceLabel, stockLabel);


        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER);
        
        Button decreaseBtn = new Button("-");
        decreaseBtn.setPrefWidth(30);
        decreaseBtn.setPrefHeight(30);
        decreaseBtn.setOnAction(e -> handleUpdateQuantity(product.getProductId(), quantity - 1));
        
        Label quantityLabel = new Label(String.valueOf(quantity));
        quantityLabel.setPrefWidth(50);
        quantityLabel.setAlignment(Pos.CENTER);
        quantityLabel.setStyle("-fx-alignment: center; -fx-font-size: 16px;");
        
        Button increaseBtn = new Button("+");
        increaseBtn.setPrefWidth(30);
        increaseBtn.setPrefHeight(30);
        increaseBtn.setDisable(quantity >= product.getStockQuantity());
        increaseBtn.setOnAction(e -> handleUpdateQuantity(product.getProductId(), quantity + 1));

        quantityBox.getChildren().addAll(decreaseBtn, quantityLabel, increaseBtn);


        Label totalPriceLabel = new Label(String.format("$%.2f", product.getPrice() * quantity));
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        totalPriceLabel.setPrefWidth(100);

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> handleRemoveItem(product.getProductId()));

        card.getChildren().addAll(imagePlaceholder, details, quantityBox, totalPriceLabel, removeBtn);
        return card;
    }

    private void updateCartSummary() {
        if (subtotalLabel == null || taxLabel == null || totalLabel == null) {
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

        double tax = subtotal * 0.08;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            if (com.ecom.utils.NavigationUtils.canGoBack()) {
                com.ecom.utils.NavigationUtils.goBack();
            } else {
                NavigationUtils.navigate("product");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }
}
