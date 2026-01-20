package com.ecom.controllers;

import com.ecom.models.Order;
import com.ecom.models.OrderItem;
import com.ecom.services.SessionService;
import com.ecom.dao.OrderDao;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for viewing a customer's own orders and order details.
 */
public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TextArea orderDetailsArea;

    private OrderDao orderDao = new OrderDao();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            String formattedDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        ordersTable.setItems(orderList);
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) showOrderDetails(newSel);
        });

        loadUserOrders();
    }

    private void loadUserOrders() {
        int userId = SessionService.getInstance().getCurrentUserId();
        if (userId <= 0) return;
        try {
            List<Order> orders = orderDao.findByUserId(userId);
            orderList.clear();
            orderList.addAll(orders);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load your orders: " + e.getMessage());
        }
    }

    private void showOrderDetails(Order order) {
        try {
            List<OrderItem> items = orderDao.findOrderItemsByOrderId(order.getOrderId());
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(order.getOrderId()).append("\n");
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

    @FXML
    private void handleBack() {
        try {
            if (com.ecom.utils.NavigationUtils.canGoBack()) com.ecom.utils.NavigationUtils.goBack();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

