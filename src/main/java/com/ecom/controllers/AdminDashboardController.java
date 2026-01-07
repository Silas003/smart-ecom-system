package com.ecom.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.*;

public class AdminDashboardController {

    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private TableView<Object> recentOrdersTable;

    @FXML
    public void initialize() {
        loadDashboardData();
    }

    @FXML
    private void handleManageProducts() {
        // Navigate to product management
    }

    @FXML
    private void handleManageOrders() {
        // Navigate to order management
    }

    @FXML
    private void handleManageUsers() {
        // Navigate to user management
    }

    private void loadDashboardData() {
        totalSalesLabel.setText("$12,345.67");
        totalOrdersLabel.setText("156");
        totalCustomersLabel.setText("89");

        // Load chart data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        series.getData().add(new XYChart.Data<>("Mon", 1200));
        series.getData().add(new XYChart.Data<>("Tue", 1500));
        series.getData().add(new XYChart.Data<>("Wed", 1800));
        series.getData().add(new XYChart.Data<>("Thu", 1600));
        series.getData().add(new XYChart.Data<>("Fri", 2100));
        salesChart.getData().add(series);
    }
}