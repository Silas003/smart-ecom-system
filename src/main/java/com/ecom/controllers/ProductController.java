package com.ecom.controllers;

import com.ecom.models.Product;
import com.ecom.models.Category;
import com.ecom.services.ProductService;
import com.ecom.services.CartService;
import com.ecom.dao.CategoryDao;
import com.ecom.utils.NavigationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private GridPane productGrid;
    @FXML private Label cartCountLabel;

    private ProductService productService;
    private CategoryDao categoryDao;
    private CartService cartService;
    private List<Product> allProducts;
    private Map<Integer, String> categoryMap = new HashMap<>();

    @FXML
    public void initialize() {
        productService = new ProductService();
        categoryDao = new CategoryDao();
        cartService = CartService.getInstance();
        
        loadCategories();
        initializeSortOptions();
        loadProducts();
        updateCartCount();
    }
    
    private void loadCategories() {
        try {
            List<Category> categories = categoryDao.findAll();
            ObservableList<String> categoryNames = FXCollections.observableArrayList("All Categories");
            for (Category category : categories) {
                categoryMap.put(category.getCategoryId(), category.getName());
                categoryNames.add(category.getName());
            }
            categoryComboBox.setItems(categoryNames);
            categoryComboBox.setValue("All Categories");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void initializeSortOptions() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Featured", "Price: Low to High", "Price: High to Low", "Name: A-Z"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.setValue("Featured");
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            displayProducts(allProducts);
        } else {
            try {
                List<Product> searchResults = productService.searchProductsByName(query);
                displayProducts(searchResults);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCategoryChange() {
        filterAndDisplayProducts();
    }

    @FXML
    private void handleSortChange() {
        filterAndDisplayProducts();
    }

    @FXML
    private void handleViewCart() {
        try {
            NavigationUtils.navigate("cart");
        } catch (IOException e) {
            e.printStackTrace();
            // showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate to cart: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddToCart(Product product) {
        if (product.getStockQuantity() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Out of Stock", "This product is currently out of stock.");
            return;
        }
        
        int currentQty = cartService.getQuantity(product.getProductId());
        if (currentQty >= product.getStockQuantity()) {
            showAlert(Alert.AlertType.WARNING, "Stock Limit", "You cannot add more than available stock.");
            return;
        }
        
        cartService.addToCart(product.getProductId(), 1);
        updateCartCount();
        showAlert(Alert.AlertType.INFORMATION, "Added to Cart", product.getName() + " added to cart!");
    }

    private void loadProducts() {
        try {
            allProducts = productService.getAllProducts();
            filterAndDisplayProducts();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void filterAndDisplayProducts() {
        List<Product> filtered = allProducts;
        
        // Filter by category
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            int categoryId = getCategoryIdByName(selectedCategory);
            if (categoryId > 0) {
                filtered = filtered.stream()
                    .filter(p -> p.getCategoryId() == categoryId)
                    .collect(java.util.stream.Collectors.toList());
            }
        }
        
        // Sort products
        String sortOption = sortComboBox.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Price: Low to High":
                    filtered = productService.sortProductsByPrice(filtered, true);
                    break;
                case "Price: High to Low":
                    filtered = productService.sortProductsByPrice(filtered, false);
                    break;
                case "Name: A-Z":
                    filtered = filtered.stream()
                        .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                        .collect(java.util.stream.Collectors.toList());
                    break;
                default: // Featured - no sorting
                    break;
            }
        }
        
        displayProducts(filtered);
    }
    
    private int getCategoryIdByName(String categoryName) {
        for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
            if (entry.getValue().equals(categoryName)) {
                return entry.getKey();
            }
        }
        return 0;
    }
    
    private void displayProducts(List<Product> products) {
        productGrid.getChildren().clear();
        productGrid.setHgap(20);
        productGrid.setVgap(20);
        productGrid.setPadding(new Insets(20));
        
        int column = 0;
        int row = 0;
        int columnsPerRow = 3;
        
        for (Product product : products) {
            VBox productCard = createProductCard(product);
            productGrid.add(productCard, column, row);
            
            column++;
            if (column >= columnsPerRow) {
                column = 0;
                row++;
            }
        }
    }
    
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setAlignment(Pos.TOP_CENTER);
        
        // Product image placeholder
        Label imagePlaceholder = new Label("[Product Image]");
        imagePlaceholder.setPrefHeight(200);
        imagePlaceholder.setPrefWidth(250);
        imagePlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-alignment: center;");
        
        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setWrapText(true);
        
        // Product price
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #007bff; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Stock info
        Label stockLabel = new Label(product.getStockQuantity() > 0 ? 
            "In Stock" : "Out of Stock");
        stockLabel.setStyle(product.getStockQuantity() > 0 ? 
            "-fx-text-fill: #28a745;" : "-fx-text-fill: #dc3545;");
        
        // Add to cart button
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.setPrefWidth(250);
        addToCartBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                             "-fx-cursor: hand; -fx-background-radius: 5;");
        addToCartBtn.setDisable(product.getStockQuantity() <= 0);
        addToCartBtn.setOnAction(e -> handleAddToCart(product));
        
        card.getChildren().addAll(imagePlaceholder, nameLabel, priceLabel, stockLabel, addToCartBtn);
        return card;
    }
    
    private void updateCartCount() {
        int totalItems = cartService.getTotalItems();
        cartCountLabel.setText(String.valueOf(totalItems));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
