package com.ecom.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class NavigationUtils {

    private static Stage stage;
    // history holds previous fxml names (e.g., "product", "login")
    private static final Deque<String> history = new ArrayDeque<>();
    private static String currentFxml = null;

    public static void setStage(Stage primaryStage){
        stage = primaryStage;
    }

    public static void navigate(String fxml) throws IOException {
        Objects.requireNonNull(stage, "Stage not set in NavigationUtils. Call setStage(...) first.");
        // push current onto history before navigating to new
        if (currentFxml != null && !currentFxml.equals(fxml)) {
            history.push(currentFxml);
        }
        Parent root = FXMLLoader.load(NavigationUtils.class.getResource("/fxml/" + fxml + ".fxml"));
        stage.setScene(new Scene(root));
        currentFxml = fxml;
    }

    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    public static void goBack() throws IOException {
        if (history.isEmpty()) return; // nothing to do
        String prev = history.pop();
        // set current to previous and load
        Parent root = FXMLLoader.load(NavigationUtils.class.getResource("/fxml/" + prev + ".fxml"));
        stage.setScene(new Scene(root));
        currentFxml = prev;
    }

    /**
     * Navigate to fxml without pushing current into history (e.g., used internally)
     */
    public static void navigateNoHistory(String fxml) throws IOException {
        Objects.requireNonNull(stage, "Stage not set in NavigationUtils. Call setStage(...) first.");
        Parent root = FXMLLoader.load(NavigationUtils.class.getResource("/fxml/" + fxml + ".fxml"));
        stage.setScene(new Scene(root));
        currentFxml = fxml;
    }
}
