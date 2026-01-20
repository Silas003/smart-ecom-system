package com.ecom.controllers;

import com.ecom.services.UserService;
import com.ecom.models.User;
import com.ecom.exceptions.DaoException;
import com.ecom.exceptions.InvalidInputException;
import com.ecom.exceptions.ValidationException;
import com.ecom.utils.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * Admin controller for managing users: list, edit, and delete user accounts.
 */
public class UserManagementController {

    // Suppress warnings for unused fields and methods that are linked to FXML
    @SuppressWarnings("unused")
    @FXML private TableView<User> usersTable;
    @SuppressWarnings("unused")
    @FXML private TableColumn<User, Integer> idColumn;
    @SuppressWarnings("unused")
    @FXML private TableColumn<User, String> usernameColumn;
    @SuppressWarnings("unused")
    @FXML private TableColumn<User, String> emailColumn;
    @SuppressWarnings("unused")
    @FXML private TableColumn<User, String> phoneColumn;
    @SuppressWarnings("unused")
    @FXML private TableColumn<User, String> roleColumn;
    @SuppressWarnings("unused")
    @FXML private TextField searchField;

    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Ensure this method is used by FXML loader
        assert usersTable != null : "Users table is not injected";
        assert idColumn != null : "ID column is not injected";
        assert usernameColumn != null : "Username column is not injected";
        assert emailColumn != null : "Email column is not injected";
        assert phoneColumn != null : "Phone column is not injected";
        assert roleColumn != null : "Role column is not injected";
        assert searchField != null : "Search field is not injected";

        idColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getUserId()).asObject());
        usernameColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername()));
        emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getEmail()));
        phoneColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getPhone()));
        roleColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getRole()));

        usersTable.setItems(userList);
        loadUsersAsync();
    }

    private void loadUsersAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                try {
                    return UserService.findAll();
                } catch (DaoException e) {
                    throw e;
                }
            }

            @Override
            protected void succeeded() {
                userList.setAll(getValue());
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        };
        new Thread(task, "load-users").start();
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { loadUsersAsync(); return; }
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                try {
                    List<User> all = UserService.findAll();
                    return all.stream().filter(u -> u.getUsername().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)).toList();
                } catch (DaoException e) {
                    throw e;
                }
            }

            @Override
            protected void succeeded() { userList.setAll(getValue()); }
            @Override
            protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); }
        };
        new Thread(task, "search-users").start();
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleEditUser() {
        User sel = usersTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "No selection", "Select a user to edit."); return; }
        showUserDialog(sel);
    }

    @SuppressWarnings("unused")
    @FXML
    private void handleDeleteUser() {
        User sel = usersTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "No selection", "Select a user to delete."); return; }
        if (sel.getUserId() <= 0) { showAlert(Alert.AlertType.ERROR, "Invalid user", "Selected user has invalid id."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete user?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            UserService.deleteUser(sel.getUserId());
                            return null;
                        } catch (DaoException | InvalidInputException e) {
                            throw e;
                        }
                    }
                    @Override
                    protected void succeeded() { userList.remove(sel); }
                    @Override
                    protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); }
                };
                new Thread(task, "delete-user").start();
            }
        });
    }

    private void showUserDialog(User user) {
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle(user == null ? "Add User" : "Edit User");
        ButtonType ok = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField username = new TextField(); username.setPromptText("Username");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField phone = new TextField(); phone.setPromptText("Phone");
        TextField role = new TextField(); role.setPromptText("Role");

        if (user != null) { username.setText(user.getUsername()); email.setText(user.getEmail()); phone.setText(user.getPhone()); role.setText(user.getRole()); }

        grid.addRow(0, new Label("Username:"), username);
        grid.addRow(1, new Label("Email:"), email);
        grid.addRow(2, new Label("Phone:"), phone);
        grid.addRow(3, new Label("Role:"), role);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(btn -> {
            if (btn == ok) {
                String un = username.getText().trim();
                String em = email.getText().trim();
                String ph = phone.getText().trim();
                String rl = role.getText().trim();
                try {

                    ValidationUtils.validateNotEmpty(un, "Username");
                    ValidationUtils.validateEmail(em);
                    ValidationUtils.validateNotEmpty(rl, "Role");
                    ValidationUtils.validateRegex(ph, "^\\+?[0-9]{10,15}$", "Phone number");
                } catch (ValidationException ve) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", ve.getMessage());
                    return null;
                }
                if (user == null) {
                    User u = new User(); u.setUsername(un); u.setEmail(em); u.setPhone(ph); u.setRole(rl); u.setPassword("changeme");
                    Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { try { UserService.createUser(u); return null; } catch (ValidationException | DaoException e) { throw e; } } @Override protected void succeeded() { loadUsersAsync(); } @Override protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); } };
                    new Thread(task, "create-user").start();
                    return u;
                } else {
                    user.setUsername(un); user.setEmail(em); user.setPhone(ph); user.setRole(rl);
                    Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { try { UserService.updateUser(user); return null; } catch (ValidationException | DaoException e) { throw e; } } @Override protected void succeeded() { loadUsersAsync(); } @Override protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); } };
                    new Thread(task, "update-user").start();
                    return user;
                }
            }
            return null;
        });
        dlg.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    @SuppressWarnings("unused")
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

    private void rethrowException(Exception e) throws DaoException {
        if (e instanceof DaoException) {
            throw (DaoException) e;
        } else {
            throw new DaoException("Unexpected error", e);
        }
    }
}
