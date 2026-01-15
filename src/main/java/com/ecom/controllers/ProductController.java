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
import javafx.concurrent.Task;
import javafx.application.Platform;

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
    @FXML private Pagination pagination;
    @FXML private ProgressIndicator progress;

    private ProductService productService;
    private CategoryDao categoryDao;
    private CartService cartService;
    private List<Product> allProducts;
    private Map<Integer, String> categoryMap = new HashMap<>();

    private final int pageSize = 9;

    @FXML
    public void initialize() {
        productService = ProductService.getInstance();
        categoryDao = new CategoryDao();
        cartService = CartService.getInstance();

        loadCategories();
        initializeSortOptions();
        initializePagination();
        updateCartCount();
    }

    private void initializePagination() {
        if (pagination != null) {
            pagination.setPageFactory(this::createPage);
        }
    }

    private VBox createPage(int pageIndex) {
        loadProductsAsync(pageIndex);
        return new VBox();
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDao.findAll();
            ObservableList<String> categoryNames = FXCollections.observableArrayList("All Categories");
            categories.stream().forEach(cat-> {
                 categoryMap.put(cat.getCategoryId(), cat.getName());
                categoryNames.add(cat.getName());
            });
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
        pagination.setCurrentPageIndex(0);
        loadProductsAsync(0);
    }

    @FXML
    private void handleCategoryChange() throws SQLException {

        pagination.setCurrentPageIndex(0);
        loadProductsAsync(0);
    }

    @FXML
    private void handleSortChange() {
        pagination.setCurrentPageIndex(0);
        loadProductsAsync(0);
    }

    @FXML
    private void handleViewCart() {
        try {
            NavigationUtils.navigate("cart");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyAccount() {
        // Navigate to account page; require login
        com.ecom.services.SessionService session = com.ecom.services.SessionService.getInstance();
        if (!session.isLoggedIn()) {
            try {
                NavigationUtils.navigate("login");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open login: " + e.getMessage());
            }
            return;
        }
        try {
            NavigationUtils.navigate("account");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open account: " + e.getMessage());
        }
    }

    @FXML
    private void handleOrders() {
        // Navigate to user's orders; require login
        com.ecom.services.SessionService session = com.ecom.services.SessionService.getInstance();
        if (!session.isLoggedIn()) {
            try {
                NavigationUtils.navigate("login");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open login: " + e.getMessage());
            }
            return;
        }
        try {
            NavigationUtils.navigate("myorders");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to open orders: " + e.getMessage());
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

    private void loadProductsAsync(int pageIndex) {
        String q = (searchField != null && searchField.getText() != null) ? searchField.getText().trim() : "";
        String sortBy = null;
        boolean asc = true;
        switch (sortComboBox.getValue()) {
            case "Price: Low to High": sortBy = "price"; asc = true; break;
            case "Price: High to Low": sortBy = "price"; asc = false; break;
            case "Name: A-Z": sortBy = "name"; asc = true; break;
            default: sortBy = ""; break;
        }


        final String sSortBy = sortBy;
        final boolean sAsc = asc;

        Integer selectedCategory = getSelectedCategoryId();

        progress.setVisible(true);
        productGrid.setDisable(true);

        Task<Void> task = new Task<>() {
            List<Product> results;
            int total;

            @Override
            protected Void call() {
                try {
                    results = productService.search(q, selectedCategory, pageIndex, pageSize, sSortBy, sAsc, true);
                    total = productService.count(q, selectedCategory);
                } catch (SQLException ex) {
                    results = List.of();
                    total = 0;
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage()));
                }
                return null;
            }

            @Override
            protected void succeeded() {
                displayProducts(results);
                int pageCount = Math.max(1, (int) Math.ceil((double) total / pageSize));
                pagination.setPageCount(pageCount);
                pagination.setCurrentPageIndex(pageIndex);
                progress.setVisible(false);
                productGrid.setDisable(false);
            }

            @Override
            protected void failed() {
                progress.setVisible(false);
                productGrid.setDisable(false);
                showAlert(Alert.AlertType.ERROR, "Failed", getException().getMessage());
            }
        };
        Thread th = new Thread(task, "product-load");
        th.setDaemon(true);
        th.start();
    }

    private void filterAndDisplayProducts() {

        loadProductsAsync(0);
    }

    private void displayProducts(List<Product> products) {
        productGrid.getChildren().clear();
        productGrid.setHgap(20);
        productGrid.setVgap(20);
        productGrid.setPadding(new Insets(20));

        int column = 0;
        int row = 0;
        int columnsPerRow = 4;

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


        Label imagePlaceholder = new Label("[Product Image]");
        imagePlaceholder.setPrefHeight(200);
        imagePlaceholder.setPrefWidth(250);
        imagePlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-alignment: center;");


        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setWrapText(true);


        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #007bff; -fx-font-size: 18px; -fx-font-weight: bold;");


        Label stockLabel = new Label(product.getStockQuantity() > 0 ? 
            "In Stock" : "Out of Stock");
        stockLabel.setStyle(product.getStockQuantity() > 0 ? 
            "-fx-text-fill: #28a745;" : "-fx-text-fill: #dc3545;");


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

    @FXML
    private void handleBack() {
        try {
            if (com.ecom.utils.NavigationUtils.canGoBack()) {
                com.ecom.utils.NavigationUtils.goBack();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Back", "No previous screen to go back to.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private Integer getSelectedCategoryId() {
        String selected = (categoryComboBox != null) ? categoryComboBox.getValue() : null;

        if (selected == null || selected.equals("All Categories")) {
            return null;
        }

        for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
            if (entry.getValue().equals(selected)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
