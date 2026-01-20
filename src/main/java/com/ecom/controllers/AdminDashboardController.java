package com.ecom.controllers;

import com.ecom.dao.OrderDao;
import com.ecom.models.Order;
import com.ecom.models.User;
import com.ecom.services.ProductService;
import com.ecom.utils.NavigationUtils;
import com.ecom.utils.QueryTimer;
import com.ecom.services.UserService;
import com.ecom.services.PerformanceReportService;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InvalidInputException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin dashboard controller: shows sales metrics, cache/performance summaries and navigation to admin panels.
 */
public class AdminDashboardController {

    @FXML private Label totalSalesLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private Label cacheHitsLabel;
    @FXML private Label cacheMissesLabel;
    @FXML private Label performanceSummaryLabel;
    @FXML private Label avgQueryTimeLabel;

    private final OrderDao orderDao = new OrderDao();
    private final com.ecom.services.ProductService productService = ProductService.getInstance();
    private final PerformanceReportService performanceReportService = PerformanceReportService.getInstance();

    @FXML
    public void initialize() {
        configureRecentOrdersTable();
        loadDashboardDataAsync();
        updateCacheLabels();
        updatePerformanceLabels();
    }

    private void configureRecentOrdersTable() {
        recentOrdersTable.getColumns().clear();
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setPrefWidth(100);
        idCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getOrderId()).asObject());

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setPrefWidth(200);
        customerCol.setCellValueFactory(cell -> {
            try {
                User u = UserService.getUserById(cell.getValue().getUserId());
                String name = (u != null) ? u.getUsername() : "User #" + cell.getValue().getUserId();
                return new SimpleStringProperty(name);
            } catch (DaoException | InvalidInputException e) {
                return new SimpleStringProperty("User #" + cell.getValue().getUserId());
            }
        });

        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setPrefWidth(100);
        totalCol.setCellValueFactory(cell -> new SimpleStringProperty(
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

    private void updatePerformanceLabels() {
        Platform.runLater(() -> {
            Map<String, QueryTimer.QueryMetrics> metrics = QueryTimer.getAllMetrics();
            if (metrics.isEmpty()) {
                if (avgQueryTimeLabel != null) {
                    avgQueryTimeLabel.setText("No queries executed yet");
                }
                if (performanceSummaryLabel != null) {
                    performanceSummaryLabel.setText("No performance data available");
                }
                return;
            }

            double totalAvg = metrics.values().stream()
                .mapToDouble(QueryTimer.QueryMetrics::getAverageTimeMs)
                .average()
                .orElse(0.0);

            if (avgQueryTimeLabel != null) {
                avgQueryTimeLabel.setText(String.format("%.2f ms", totalAvg));
            }

            if (performanceSummaryLabel != null) {
                String summary = performanceReportService.getSummary();
                performanceSummaryLabel.setText(summary);
            }
        });
    }

    @FXML
    private void handleClearCache() {
        productService.clearCache();
        updateCacheLabels();
    }

    @FXML
    private void handleCaptureBaseline() {
        performanceReportService.captureBaseline();
        showAlert(Alert.AlertType.INFORMATION, "Baseline Captured", 
            "Baseline metrics have been captured. Perform some operations, then capture optimized metrics.");
        updatePerformanceLabels();
    }

    @FXML
    private void handleCaptureOptimized() {
        performanceReportService.captureOptimized();
        showAlert(Alert.AlertType.INFORMATION, "Optimized Metrics Captured", 
            "Optimized metrics captured. You can now generate a performance report.");
        updatePerformanceLabels();
    }

    @FXML
    private void handleGenerateReport() {
        String report = performanceReportService.generateReport();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Performance Report");
        alert.setHeaderText("Query Performance Analysis");
        
        TextArea textArea = new TextArea(report);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        textArea.setPrefColumnCount(80);
        
        VBox vbox = new VBox(textArea);
        vbox.setPrefWidth(700);
        alert.getDialogPane().setContent(vbox);
        alert.getDialogPane().setPrefWidth(750);
        
        Button saveButton = new Button("Save to File");
        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Performance Report");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fileChooser.setInitialFileName("performance_report_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
            
            java.io.File file = fileChooser.showSaveDialog(alert.getOwner());
            if (file != null) {
                try {
                    performanceReportService.saveReportToFile(file.getAbsolutePath());
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Report saved to: " + file.getAbsolutePath());
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to save report: " + ex.getMessage());
                }
            }
        });
        
        alert.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    private void handleClearMetrics() {
        QueryTimer.clearMetrics();
        updatePerformanceLabels();
        showAlert(Alert.AlertType.INFORMATION, "Metrics Cleared", 
            "All query performance metrics have been cleared.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    private void handleManageCategories() {
        try {
            NavigationUtils.navigate("categorymanagement");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            if (NavigationUtils.canGoBack()) {
                NavigationUtils.goBack();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

