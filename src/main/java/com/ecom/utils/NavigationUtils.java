package com.ecom.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class NavigationUtils {

    private static Stage stage;

    public static void setStage(Stage primaryStage){
        stage = primaryStage;
    }

    public static void navigate(String fxml) throws IOException {
        Parent root = FXMLLoader.load(NavigationUtils.class.getResource("/fxml/"+fxml+".fxml"));
        stage.setScene(new Scene(root));
    }
}
