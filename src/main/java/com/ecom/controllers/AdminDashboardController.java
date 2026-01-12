package com.ecom.controllers;

import com.ecom.dao.OrderDao;
import com.ecom.models.Order;
import com.ecom.models.User;
import com.ecom.utils.NavigationUtils;
import com.ecom.services.UserService;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InvalidInputException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private Label cacheHitsLabel;
    @FXML private Label cacheMissesLabel;

    private final OrderDao orderDao = new OrderDao();
    private final com.ecom.services.ProductService productService = com.ecom.services.ProductService.getInstance();

    @FXML
    public void initialize() {
        configureRecentOrdersTable();
        loadDashboardDataAsync();
        updateCacheLabels();
    }

    private void configureRecentOrdersTable() {
        recentOrdersTable.getColumns().clear();
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setPrefWidth(100);
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getOrderId()).asObject());

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setPrefWidth(200);
        customerCol.setCellValueFactory(cell -> {
            try {
                User u = UserService.getUserById(cell.getValue().getUserId());
                String name = (u != null) ? u.getUsername() : "User #" + cell.getValue().getUserId();
                return new javafx.beans.property.SimpleStringProperty(name);
            } catch (DaoException | InvalidInputException e) {
                return new javafx.beans.property.SimpleStringProperty("User #" + cell.getValue().getUserId());
            }
        });

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setPrefWidth(100);
        totalCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", cell.getValue().getTotalAmount())));

        recentOrdersTable.getColumns().addAll(idCol, customerCol, dateCol, totalCol);
    }

    private void loadDashboardDataAsync() {
        Task<Void> task = new Task<>() {
            double totalSales = 0;
            int totalOrders = 0;
            long totalCustomers = 0;
            List<Order> recentOrders = List.of();
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            @Override
            protected Void call() {
                try {
                    List<Order> orders = orderDao.findAll();
                    totalOrders = orders.size();
                    totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();

                    List<User> users = UserService.findAll();
                    totalCustomers = users.stream().filter(user->user.getRole().equalsIgnoreCase("customer")).count();


                    recentOrders = orders.stream().limit(10).collect(Collectors.toList());

                    LocalDate today = LocalDate.now();
                    Map<LocalDate, Double> byDay = orders.stream()
                            .collect(Collectors.groupingBy(o -> o.getOrderDate().toLocalDate(), Collectors.summingDouble(Order::getTotalAmount)));

                    series.setName("Sales");
                    for (int i = 6; i >= 0; i--) {
                        LocalDate d = today.minusDays(i);
                        double val = byDay.getOrDefault(d, 0.0);
                        series.getData().add(new XYChart.Data<>(d.toString(), val));
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (DaoException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                totalSalesLabel.setText(String.format("$%.2f", totalSales));
                totalOrdersLabel.setText(String.valueOf(totalOrders));
                totalCustomersLabel.setText(String.valueOf(totalCustomers));
                salesChart.getData().clear();
                salesChart.getData().add(series);
                recentOrdersTable.getItems().setAll(recentOrders);
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                totalSalesLabel.setText("Error");
                totalOrdersLabel.setText("Error");
                totalCustomersLabel.setText("Error");
                System.err.println("Failed loading admin dashboard: " + ex.getMessage());
            }
        };
        Thread t = new Thread(task, "admin-dashboard-load");
        t.setDaemon(true);
        t.start();
    }

    private void updateCacheLabels() {
        cacheHitsLabel.setText(String.valueOf(productService.getCacheHits()));
        cacheMissesLabel.setText(String.valueOf(productService.getCacheMisses()));
    }

    @FXML
    private void handleClearCache() {
        productService.clearCache();
        updateCacheLabels();
    }

    @FXML
    private void handleManageProducts() {
        try {
            NavigationUtils.navigate("productmanagement");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageOrders() {
        try {
            NavigationUtils.navigate("ordermanagement");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageUsers() {
        try {
            NavigationUtils.navigate("usermanagement");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (com.ecom.utils.NavigationUtils.canGoBack()) {
                com.ecom.utils.NavigationUtils.goBack();
            } else {
                // nothing to do
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

