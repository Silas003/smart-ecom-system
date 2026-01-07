package com.ecom.controllers;

import com.ecom.dao.*;
import com.ecom.models.*;
import com.ecom.services.*;
import com.ecom.utils.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class DashboardController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Integer> colCategory;

    // Cart UI
    @FXML private TableView<CartEntry> cartTable;
    @FXML private TableColumn<CartEntry, String> colCartName;
    @FXML private TableColumn<CartEntry, Integer> colCartQty;
    @FXML private TableColumn<CartEntry, Double> colCartPrice;
    @FXML private Label totalLabel;

    private ProductService productService;
    private OrderService orderService;
    private CategoryDao categoryDao;
    private ObservableList<Product> productList;
    private ObservableList<CartEntry> cartList;
    private Map<Product, Integer> cartMap;

    public void initialize() {
        productService = new ProductService();
        orderService = new OrderService();
        categoryDao = new CategoryDao();
        productList = FXCollections.observableArrayList();
        cartList = FXCollections.observableArrayList();
        cartMap = new HashMap<>();

        // Product Table Setup
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        productTable.setItems(productList);

        // Cart Table Setup
        colCartName.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colCartQty.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colCartPrice.setCellValueFactory(
                cellData ->
                        new SimpleDoubleProperty(
                                cellData.getValue().getProduct().getPrice() * cellData.getValue().getQuantity())
                                .asObject());
        cartTable.setItems(cartList);

        loadData();
    }

    private void loadData() {
        try {
//            PerformanceTimer timer = new PerformanceTimer();
//            timer.start();
            List<Product> products = productService.getAllProducts();
//            long duration = timer.end();
//            System.out.println("Load Data Time: " + PerformanceTimer.formatDuration(duration));

            productList.setAll(products);
        } catch (SQLException e) {
            showError("Error loading data", e.getMessage());
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to add to cart.");
            return;
        }

        if (selected.getStockQuantity() <= 0) {
            showWarning("Out of Stock", "This product is currently out of stock.");
            return;
        }

        // Check if already in cart and if we have enough stock
        int currentQtyInCart = cartMap.getOrDefault(selected, 0);
        if (currentQtyInCart >= selected.getStockQuantity()) {
            showWarning("Stock Limit", "You cannot add more than available stock.");
            return;
        }

        // Add to Map
        cartMap.put(selected, currentQtyInCart + 1);

        // Update UI List
        updateCartUI();
    }

    private void updateCartUI() {
        cartList.clear();
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cartMap.entrySet()) {
            cartList.add(new CartEntry(entry.getKey(), entry.getValue()));
            total += entry.getKey().getPrice() * entry.getValue();
        }
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleCheckout() {
        if (cartMap.isEmpty()) {
            showWarning("Empty Cart", "Add items to cart before checking out.");
            return;
        }

        // Hardcoded User ID 1 for now (as per requirements/seed data)
        boolean success = orderService.checkout(1, cartMap);

        if (success) {
            showInfo("Success", "Order placed successfully!");
            cartMap.clear();
            updateCartUI();
            loadData(); // Refresh product stock
        } else {
            showError("Checkout Failed", "Could not place order. Check stock or try again.");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        try {
//            PerformanceTimer timer = new PerformanceTimer();
//            timer.start();
            List<Product> results = productService.searchProductsByName(query);
//            long duration = timer.end();
//            System.out.println("Search Time: " + PerformanceTimer.formatDuration(duration));

            productList.setAll(results);
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleSortAsc() {
        List<Product> sorted = productService.sortProductsByPrice(productList, true);
        productList.setAll(sorted);
    }

    @FXML
    private void handleSortDesc() {
        List<Product> sorted = productService.sortProductsByPrice(productList, false);
        productList.setAll(sorted);
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                productService.deleteProduct(selected.getProductId());
                productList.remove(selected);
            } catch (SQLException e) {
                showError("Delete Error", e.getMessage());
            }
        } else {
            showWarning("No Selection", "Please select a product to delete.");
        }
    }

    @FXML
    private void handleAdd() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter Product Details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        // Use Spinner for numeric input
        Spinner<Double> priceSpinner = new Spinner<>(0.01, 100000.0, 100.0, 1.0);
        priceSpinner.setEditable(true);

        Spinner<Integer> stockSpinner = new Spinner<>(0, 10000, 10, 1);
        stockSpinner.setEditable(true);

        ComboBox<Category> categoryBox = new ComboBox<>();
        try {
            List<Category> categories = categoryDao.findAll();
            if (categories.isEmpty()) {
                Category defaultCat = new Category("General");
                categoryDao.create(defaultCat);
                categories.add(defaultCat);
            }
            categoryBox.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            showError("Database Error", "Could not load categories: " + e.getMessage());
            return;
        }

        categoryBox.setConverter(
                new StringConverter<Category>() {
                    @Override
                    public String toString(Category object) {
                        return object == null ? "" : object.getName();
                    }

                    @Override
                    public Category fromString(String string) {
                        return null; // Not needed for read-only combo
                    }
                });
        categoryBox.getSelectionModel().selectFirst();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceSpinner, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockSpinner, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(
                dialogButton -> {
                    if (dialogButton == addButtonType) {
                        try {
                            String name = nameField.getText();
                            if (name.trim().isEmpty()) return null;

                            // Commit values in case user typed but didn't press enter
                            commitEditorText(priceSpinner);
                            commitEditorText(stockSpinner);

                            double price = priceSpinner.getValue();
                            int stock = stockSpinner.getValue();
                            Category cat = categoryBox.getValue();

                            return new Product(cat.getCategoryId(), name, price, stock);
                        } catch (Exception e) {
                            return null;
                        }
                    }
                    return null;
                });

        Optional<Product> result = dialog.showAndWait();

        result.ifPresent(
                product -> {
                    try {
                        productService.createProduct(product);
                        loadData();
                    } catch (SQLException e) {
                        showError("Add Error", e.getMessage());
                    }
                });
    }

    private <T> void commitEditorText(Spinner<T> spinner) {
        if (!spinner.isEditable()) return;
        String text = spinner.getEditor().getText();
        SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
        if (valueFactory != null) {
            StringConverter<T> converter = valueFactory.getConverter();
            if (converter != null) {
                T value = converter.fromString(text);
                valueFactory.setValue(value);
            }
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper class for Cart TableView
    public static class CartEntry {
        private final Product product;
        private final int quantity;

        public CartEntry(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}