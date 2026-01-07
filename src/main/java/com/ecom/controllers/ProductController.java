package com.ecom.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
public class ProductController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private GridPane productGrid;
    @FXML private Label cartCountLabel;

    @FXML
    public void initialize() {
        // Initialize categories
        ObservableList<String> categories = FXCollections.observableArrayList(
                "All Categories", "Electronics", "Clothing", "Books", "Home & Garden"
        );
        categoryComboBox.setItems(categories);
        categoryComboBox.setValue("All Categories");

        // Initialize sort options
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Featured", "Price: Low to High", "Price: High to Low", "Newest"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.setValue("Featured");

        loadProducts();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        // Implement search logic
    }

    @FXML
    private void handleCategoryChange() {
        // Filter products by category
        loadProducts();
    }

    @FXML
    private void handleSortChange() {
        // Sort products
        loadProducts();
    }

    @FXML
    private void handleViewCart() {
        // Navigate to cart page
    }

    private void loadProducts() {
        // Load products from database and populate grid
        // This is a placeholder - you'll need to implement actual product loading
    }
}
