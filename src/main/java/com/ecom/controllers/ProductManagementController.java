package com.ecom.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductManagementController {

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TextField searchField;

    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        productsTable.setItems(productList);
        loadProducts();
    }

    @FXML
    private void handleAddProduct() {
        // Open add product dialog
        showProductDialog(null);
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showProductDialog(selected);
        }
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Product");
            alert.setContentText("Are you sure you want to delete this product?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    productList.remove(selected);
                }
            });
        }
    }

    @FXML
    private void handleSearch() {
        // Implement search functionality
    }

    private void loadProducts() {
        // Load products from database
    }

    private void showProductDialog(Product product) {
        // Show dialog to add/edit product
    }

    // Product class (you can create this in a separate file)
    public static class Product {
        private String id;
        private String name;
        private String category;
        private double price;
        private int stock;

        // Constructor, getters, and setters
    }
}