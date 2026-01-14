package com.ecom.controllers;

import com.ecom.models.Category;
import com.ecom.services.CategoryService;
import com.ecom.utils.NavigationUtils;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.DuplicateEntityException;
import com.ecom.exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.List;

/**
 * Controller for category management UI.
 * Provides full CRUD operations for categories with validation and feedback.
 */
public class CategoryManagementController {

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> idColumn;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TextField searchField;
    @FXML private ProgressIndicator progress;

    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private CategoryService categoryService;

    @FXML
    public void initialize() {
        categoryService = CategoryService.getInstance();
        
        // Setup table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        categoriesTable.setItems(categoryList);
        loadCategoriesAsync();
    }

    @FXML
    private void handleAddCategory() {
        showCategoryDialog(null);
    }

    @FXML
    private void handleEditCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showCategoryDialog(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to edit.");
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Category");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete category '" + selected.getName() + "'?\n\n" +
                    "Note: This will fail if the category is being used by any products.");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            categoryService.deleteCategory(selected.getCategoryId());
                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            categoryList.remove(selected);
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    "Category deleted successfully.");
                            progress.setVisible(false);
                        }

                        @Override
                        protected void failed() {
                            progress.setVisible(false);
                            Throwable ex = getException();
                            String message = "Failed to delete category.";
                            if (ex instanceof DaoException) {
                                message = ex.getMessage();
                            } else if (ex.getCause() != null) {
                                message = ex.getCause().getMessage();
                            }
                            showAlert(Alert.AlertType.ERROR, "Error", message);
                        }
                    };
                    progress.setVisible(true);
                    Thread t = new Thread(task, "delete-category");
                    t.setDaemon(true);
                    t.start();
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a category to delete.");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadCategoriesAsync();
            return;
        }

        // Filter categories in-memory
        ObservableList<Category> filtered = FXCollections.observableArrayList();
        for (Category category : categoryList) {
            if (category.getName().toLowerCase().contains(query)) {
                filtered.add(category);
            }
        }
        categoriesTable.setItems(filtered);
    }

    private void loadCategoriesAsync() {
        Task<List<Category>> task = new Task<>() {
            @Override
            protected List<Category> call() throws Exception {
                return categoryService.getAllCategories();
            }

            @Override
            protected void succeeded() {
                categoryList.setAll(getValue());
                categoriesTable.setItems(categoryList);
                progress.setVisible(false);
            }

            @Override
            protected void failed() {
                progress.setVisible(false);
                Throwable ex = getException();
                String message = "Failed to load categories.";
                if (ex instanceof DaoException) {
                    message = ex.getMessage();
                } else if (ex.getCause() != null) {
                    message = ex.getCause().getMessage();
                }
                showAlert(Alert.AlertType.ERROR, "Error", message);
            }
        };
        progress.setVisible(true);
        Thread t = new Thread(task, "load-categories");
        t.setDaemon(true);
        t.start();
    }

    private void showCategoryDialog(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "Add Category" : "Edit Category");
        dialog.setHeaderText(category == null ? "Enter category name" : "Edit category name");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");
        if (category != null) {
            nameField.setText(category.getName());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Enable/Disable save button based on input
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
                    return null;
                }

                Category newCategory;
                if (category == null) {
                    newCategory = new Category(name);
                    Task<Void> createTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            categoryService.createCategory(newCategory);
                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            loadCategoriesAsync();
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    "Category '" + name + "' created successfully.");
                            progress.setVisible(false);
                        }

                        @Override
                        protected void failed() {
                            progress.setVisible(false);
                            Throwable ex = getException();
                            String message = "Failed to create category.";
                            if (ex instanceof DuplicateEntityException) {
                                message = ex.getMessage();
                            } else if (ex instanceof ValidationException) {
                                message = ex.getMessage();
                            } else if (ex instanceof DaoException) {
                                message = ex.getMessage();
                            } else if (ex.getCause() != null) {
                                message = ex.getCause().getMessage();
                            }
                            showAlert(Alert.AlertType.ERROR, "Error", message);
                        }
                    };
                    progress.setVisible(true);
                    Thread t = new Thread(createTask, "create-category");
                    t.setDaemon(true);
                    t.start();
                } else {
                    newCategory = new Category(category.getCategoryId(), name);
                    Task<Void> updateTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            categoryService.updateCategory(newCategory);
                            return null;
                        }

                        @Override
                        protected void succeeded() {
                            loadCategoriesAsync();
                            showAlert(Alert.AlertType.INFORMATION, "Success", 
                                    "Category updated successfully.");
                            progress.setVisible(false);
                        }

                        @Override
                        protected void failed() {
                            progress.setVisible(false);
                            Throwable ex = getException();
                            String message = "Failed to update category.";
                            if (ex instanceof DuplicateEntityException) {
                                message = ex.getMessage();
                            } else if (ex instanceof ValidationException) {
                                message = ex.getMessage();
                            } else if (ex instanceof DaoException) {
                                message = ex.getMessage();
                            } else if (ex.getCause() != null) {
                                message = ex.getCause().getMessage();
                            }
                            showAlert(Alert.AlertType.ERROR, "Error", message);
                        }
                    };
                    progress.setVisible(true);
                    Thread t = new Thread(updateTask, "update-category");
                    t.setDaemon(true);
                    t.start();
                }
                return newCategory;
            }
            return null;
        });

        dialog.showAndWait();
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
            if (NavigationUtils.canGoBack()) {
                NavigationUtils.goBack();
            } else {
                NavigationUtils.navigate("adminDashboard");
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }
}
