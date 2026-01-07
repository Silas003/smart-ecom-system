package com.ecom.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private ComboBox<String> statusFilter;

    @FXML
    public void initialize() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "All Orders", "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
        ));
        statusFilter.setValue("All Orders");
        loadOrders();
    }

    @FXML
    private void handleStatusFilter() {
        loadOrders();
    }

    @FXML
    private void handleViewOrder() {
        // View order details
    }

    @FXML
    private void handleUpdateStatus() {
        // Update order status
    }

    private void loadOrders() {
        // Load orders from database
    }

    public static class Order {
        private String orderId;
        private String customerName;
        private String date;
        private double total;
        private String status;
        // Constructor, getters, setters
    }
}
