package com.ecom.controllers;

import com.ecom.models.Product;
import com.ecom.models.Category;
import com.ecom.services.ProductService;
import com.ecom.dao.CategoryDao;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductManagementController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TextField searchField;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ProductService productService;
    private Map<Integer, String> categoryMap = new HashMap<>();

    @FXML
    public void initialize() {
        productService = new ProductService();
        loadCategories();
        
        // Setup table columns with correct property names
        idColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Custom cell factory for category column to show category name
        categoryColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            String categoryName = categoryMap.getOrDefault(product.getCategoryId(), "N/A");
            return new javafx.beans.property.SimpleStringProperty(categoryName);
        });

        productsTable.setItems(productList);
        loadProducts();
    }
    
    private void loadCategories() {
        try {
            CategoryDao categoryDao = new CategoryDao();
            List<Category> categories = categoryDao.findAll();
            for (Category category : categories) {
                categoryMap.put(category.getCategoryId(), category.getName());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddProduct() {
        showProductDialog(null);
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Product");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this product?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        productService.deleteProduct(selected.getProductId());
                        productList.remove(selected);
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
                    } catch (SQLException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product: " + e.getMessage());
                    }
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> searchResults = productService.searchProductsByName(query);
            productList.clear();
            productList.addAll(searchResults);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Search failed: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.clear();
            productList.addAll(products);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void showProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(product == null ? "Add Product" : "Edit Product");
        dialog.setHeaderText(product == null ? "Enter product details" : "Edit product details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");
        ComboBox<String> categoryCombo = new ComboBox<>();
        
        // Populate category combo
        ObservableList<String> categoryNames = FXCollections.observableArrayList(categoryMap.values());
        categoryCombo.setItems(categoryNames);
        categoryCombo.setPromptText("Select Category");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryCombo, 1, 3);

        if (product != null) {
            nameField.setText(product.getName());
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStockQuantity()));
            String categoryName = categoryMap.get(product.getCategoryId());
            if (categoryName != null) {
                categoryCombo.setValue(categoryName);
            }
        }

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether fields are filled
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty() || 
                                 priceField.getText().trim().isEmpty() || 
                                 stockField.getText().trim().isEmpty() ||
                                 categoryCombo.getValue() == null);
        });
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || 
                                 newValue.trim().isEmpty() || 
                                 stockField.getText().trim().isEmpty() ||
                                 categoryCombo.getValue() == null);
        });
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || 
                                 priceField.getText().trim().isEmpty() || 
                                 newValue.trim().isEmpty() ||
                                 categoryCombo.getValue() == null);
        });
        categoryCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(nameField.getText().trim().isEmpty() || 
                                 priceField.getText().trim().isEmpty() || 
                                 stockField.getText().trim().isEmpty() ||
                                 newValue == null);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    double price = Double.parseDouble(priceField.getText().trim());
                    int stock = Integer.parseInt(stockField.getText().trim());
                    String categoryName = categoryCombo.getValue();
                    
                    // Find category ID from name
                    int categoryId = 0;
                    for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
                        if (entry.getValue().equals(categoryName)) {
                            categoryId = entry.getKey();
                            break;
                        }
                    }
                    
                    Product newProduct;
                    if (product == null) {
                        newProduct = new Product(categoryId, name, price, stock);
                        productService.createProduct(newProduct);
                    } else {
                        newProduct = new Product(product.getProductId(), categoryId, name, price, stock);
                        productService.updateProduct(newProduct);
                    }
                    return newProduct;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for price and stock.");
                    return null;
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to save product: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                loadProducts();
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
