package com.ecom.controllers;

import com.ecom.dao.UsersDao;
import com.ecom.models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TextField searchField;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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
            protected List<User> call() {
                return UsersDao.findAll();
            }

            @Override
            protected void succeeded() {
                userList.setAll(getValue());
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage());
            }
        };
        new Thread(task, "load-users").start();
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { loadUsersAsync(); return; }
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                List<User> all = UsersDao.findAll();
                return all.stream().filter(u -> u.getUsername().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)).toList();
            }

            @Override
            protected void succeeded() { userList.setAll(getValue()); }
            @Override
            protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); }
        };
        new Thread(task, "search-users").start();
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    @FXML
    private void handleEditUser() {
        User sel = usersTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "No selection", "Select a user to edit."); return; }
        showUserDialog(sel);
    }

    @FXML
    private void handleDeleteUser() {
        User sel = usersTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "No selection", "Select a user to delete."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete user?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        UsersDao.deleteUser(sel.getUserId());
                        return null;
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
                if (un.isEmpty() || em.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Validation", "Username and email are required"); return null; }
                if (user == null) {
                    User u = new User(); u.setUsername(un); u.setEmail(em); u.setPhone(ph); u.setRole(rl); u.setPassword("changeme");
                    Task<Void> task = new Task<>() { @Override protected Void call() { UsersDao.createUser(u); return null; } @Override protected void succeeded() { loadUsersAsync(); } @Override protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); } };
                    new Thread(task, "create-user").start();
                    return u;
                } else {
                    user.setUsername(un); user.setEmail(em); user.setPhone(ph); user.setRole(rl);
                    Task<Void> task = new Task<>() { @Override protected Void call() { UsersDao.updateUser(user.getUsername(), user.getUserId()); return null; } @Override protected void succeeded() { loadUsersAsync(); } @Override protected void failed() { showAlert(Alert.AlertType.ERROR, "Error", getException().getMessage()); } };
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
