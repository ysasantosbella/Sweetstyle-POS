package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;

public class LoadingScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML for the loading screen and get the controller
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("loading.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("Loading...");

            // Access the LoadingController to initialize the behavior
            LoadingController controller = fxmlLoader.getController();
            controller.startLoading(); // Start the loading animation

            // Make the stage full screen
            primaryStage.setFullScreen(true);

            // Create a KeyCombination for the ESC key to exit full-screen
            KeyCombination keyCombination = new KeyCodeCombination(KeyCode.ESCAPE);
            primaryStage.setFullScreenExitKeyCombination(keyCombination); // Set ESC to exit fullscreen

            // Add event handler to toggle full-screen with ESC key
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    if (primaryStage.isFullScreen()) {
                        primaryStage.setFullScreen(false); // Exit full-screen mode
                    }
                }
            });

            // Toggle full-screen when maximizing or resizing the window
            primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    primaryStage.setFullScreen(true); // Go back to fullscreen when maximized
                }
            });

            // Show the stage (loading screen)
            primaryStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
