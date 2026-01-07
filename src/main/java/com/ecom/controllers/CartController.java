package com.ecom.controllers;

// CartController.java
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;

public class CartController {

    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label item;
    private static int cartItem ;
    @FXML
    public void initialize() {
        updateCartSummary();
    }

    @FXML
    private void handleContinueShopping() {
        try {
            NavigationUtils.navigate("product");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleCheckout() {
        try {
            NavigationUtils.navigate("checkout");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @FXML
//    private void increase(){
//        item.setText(String.format("%d",cartItem++));
//    }
//
//    @FXML
//    private void decrease(){
//        item.setText(String.format("%d",cartItem--));
//    }

    private void updateCartSummary() {
        // Calculate totals
        int items = cartItem = 0;
        double subtotal = 0.0;
        double tax = subtotal * 0.08; // 8% tax
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", tax));
        totalLabel.setText(String.format("$%.2f", total));
        item.setText(String.format("%d",items));
    }
}
