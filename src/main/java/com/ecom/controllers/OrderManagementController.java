package com.ecom.controllers;

import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.models.User;
import com.ecom.dao.OrderDao;
import com.ecom.services.UserService;
import com.ecom.exceptions.DaoException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextArea orderDetailsArea;

    private OrderDao orderDao;
    private ObservableList<Order> orderList = FXCollections.observableArrayList();
    private Map<Integer, String> userMap = new HashMap<>();

    @FXML
    public void initialize() {
        orderDao = new OrderDao();
        loadUsers();
        

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            String formattedDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        customerColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            String customerName = userMap.getOrDefault(order.getUserId(), "User #" + order.getUserId());
            return new javafx.beans.property.SimpleStringProperty(customerName);
        });

        ordersTable.setItems(orderList);
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showOrderDetails(newSelection);
            }
        });

        statusFilter.setItems(FXCollections.observableArrayList("All Orders","processing", "delivered", "cancelled"));
        statusFilter.setValue("All Orders");
        loadOrders();
    }

    private void loadUsers() {
        try {
            List<User> users = UserService.findAll();
            for (User user : users) {
                userMap.put(user.getUserId(), user.getUsername());
            }
        } catch (DaoException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    @FXML
    private void handleStatusFilter() {
        loadOrders();
    }

    @FXML
    private void handleViewOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showOrderDetails(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to view.");
        }
    }

    @FXML
    private void handleUpdateStatus() {

        showAlert(Alert.AlertType.INFORMATION, "Info", "Status update functionality requires order status field in database.");
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderDao.findAll();
            orderList.clear();
            orderList.addAll(orders);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void showOrderDetails(Order order) {
        try {
            List<OrderItem> items = orderDao.findOrderItemsByOrderId(order.getOrderId());
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(order.getOrderId()).append("\n");
            details.append("Customer: ").append(userMap.getOrDefault(order.getUserId(), "User #" + order.getUserId())).append("\n");
            details.append("Date: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
            details.append("Total: $").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");
            details.append("Items:\n");
            details.append("----------------------------------------\n");
            
            for (OrderItem item : items) {
                details.append("Product ID: ").append(item.getProductId())
                       .append(", Quantity: ").append(item.getQuantity())
                       .append(", Price: $").append(String.format("%.2f", item.getPriceAtPurchase()))
                       .append("\n");
            }
            
            orderDetailsArea.setText(details.toString());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order details: " + e.getMessage());
        }
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
}
