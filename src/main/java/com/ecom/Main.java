package com.ecom;
import java.io.IOException;

import com.ecom.utils.NavigationUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{
    @Override
    public void start(Stage primaryStage){
        NavigationUtils.setStage(primaryStage);
        try {
            NavigationUtils.navigate("login");
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Smart E-Commerce Application");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
