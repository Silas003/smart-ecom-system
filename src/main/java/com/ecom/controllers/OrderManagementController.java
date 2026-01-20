package com.ecom.controllers;

import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.models.User;
import com.ecom.dao.OrderDao;
import com.ecom.services.UserService;
import com.ecom.exceptions.DaoException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller that allows administrators to view and manage orders.
 * Supports filtering by status, viewing order details in a modal, and updating order status.
 */
public class OrderManagementController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private ComboBox<String> statusFilter;
    // no embedded details area; view via modal popup

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
            return new SimpleStringProperty(formattedDate);
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        // bind status column to Order.status with a safe null fallback
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue() == null ? null : cellData.getValue().getStatus();
            return new SimpleStringProperty(status == null ? "(none)" : status);
        });
        customerColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            String customerName = userMap.getOrDefault(order.getUserId(), "User #" + order.getUserId());
            return new SimpleStringProperty(customerName);
        });

        ordersTable.setItems(orderList);
        // selection does not auto-open details; use the "View Details" button to open a simple popup
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // no-op
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
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to view.");
            return;
        }

        try {
            List<OrderItem> items = orderDao.findOrderItemsByOrderId(selected.getOrderId());
            String details = buildOrderDetailsString(selected, items);

            // Show details in a simple modal dialog with a close button
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Details - #" + selected.getOrderId());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            TextArea content = new TextArea(details);
            content.setEditable(false);
            content.setWrapText(true);
            content.setPrefWidth(600);
            content.setPrefHeight(400);
            dialog.getDialogPane().setContent(content);

            // Set owner if possible
            if (ordersTable != null && ordersTable.getScene() != null && ordersTable.getScene().getWindow() != null) {
                dialog.initOwner(ordersTable.getScene().getWindow());
            }

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order details: " + e.getMessage());
        }
    }

    // Helper to build order details string for reuse (popup + embedded view)
    private String buildOrderDetailsString(Order order, List<OrderItem> items) {
        StringBuilder details = new StringBuilder();
        details.append("Order ID: ").append(order.getOrderId()).append("\n");
        details.append("Status: ").append(order.getStatus() == null ? "(none)" : order.getStatus()).append("\n");
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
        return details.toString();
    }

    @FXML
    private void handleUpdateStatus() {

        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to update.");
            return;
        }

        // Present a simple choice dialog for new status
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus() == null ? "processing" : selected.getStatus(),
                FXCollections.observableArrayList("processing", "delivered", "cancelled"));
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + selected.getOrderId());
        dialog.setContentText("Choose new status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                boolean ok = orderDao.updateStatus(selected.getOrderId(), newStatus);
                if (ok) {
                    selected.setStatus(newStatus);
                    ordersTable.refresh();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order status updated.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failed", "Order status update affected no rows.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order status: " + e.getMessage());
            }
        });
    }

    private void loadOrders() {
        try {
            String status = statusFilter.getValue();
            List<Order> orders = orderDao.findByStatus(status);
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
            details.append("Status: ").append(order.getStatus() == null ? "(none)" : order.getStatus()).append("\n");
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
            
            //orderDetailsArea.setText(details.toString());
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
