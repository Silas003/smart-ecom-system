package com.ecom.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
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
        URL resource = NavigationUtils.class.getResource("/fxml/" + fxml + ".fxml");
        if (resource == null) {
            throw new IOException("FXML resource not found for: /fxml/" + fxml + ".fxml");
        }
        try {
            Parent root = FXMLLoader.load(resource);
            stage.setScene(new Scene(root));
            currentFxml = fxml;
        } catch (RuntimeException | IOException e) {
            IOException ioe = new IOException("Failed to load FXML '/fxml/" + fxml + ".fxml': " + e.getMessage(), e);
            throw ioe;
        }
    }

    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    public static void goBack() throws IOException {
        if (history.isEmpty()) return; // nothing to do
        String prev = history.pop();
        // set current to previous and load
        URL resource = NavigationUtils.class.getResource("/fxml/" + prev + ".fxml");
        if (resource == null) {
            throw new IOException("FXML resource not found for: /fxml/" + prev + ".fxml");
        }
        Parent root = FXMLLoader.load(resource);
        stage.setScene(new Scene(root));
        currentFxml = prev;
    }

    /**
     * Navigate to fxml without pushing current into history (e.g., used internally)
     */
    public static void navigateNoHistory(String fxml) throws IOException {
        Objects.requireNonNull(stage, "Stage not set in NavigationUtils. Call setStage(...) first.");
        URL resource = NavigationUtils.class.getResource("/fxml/" + fxml + ".fxml");
        if (resource == null) {
            throw new IOException("FXML resource not found for: /fxml/" + fxml + ".fxml");
        }
        Parent root = FXMLLoader.load(resource);
        stage.setScene(new Scene(root));
        currentFxml = fxml;
    }

    /**
     * Check whether an FXML resource exists on the classpath.
     */
    public static boolean fxmlExists(String fxml) {
        return NavigationUtils.class.getResource("/fxml/" + fxml + ".fxml") != null;
    }
}
