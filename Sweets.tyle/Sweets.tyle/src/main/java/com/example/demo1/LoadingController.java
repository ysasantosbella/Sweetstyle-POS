package com.example.demo1;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import java.io.IOException;

public class LoadingController {

    @FXML
    private ProgressBar progressBar;  // ProgressBar in the FXML

    private static final int LOAD_TIME = 10; // Total loading time in seconds

    // Call this method to start the loading animation
    public void startLoading() {
        // Set the initial value of the progress bar to 0
        progressBar.setProgress(0);

        // Simple timeline to simulate loading duration
        Timeline timeline = new Timeline();
        for (int i = 1; i <= LOAD_TIME; i++) {
            final double progress = (double) i / LOAD_TIME;
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(i), e -> {
                progressBar.setProgress(progress);  // Update the progress bar
            }));
        }

        // After loading, transition to the main application
        timeline.setOnFinished(e -> transitionToMainApp());
        timeline.play();
    }

    // This method is called after loading completes
    private void transitionToMainApp() {
        try {
            openMainApplication((Stage) progressBar.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Open the main application after loading is complete
    public void openMainApplication(Stage stage) throws IOException {
        // Load the login screen FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Sweets.tyle Café - Login");
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
    }
}
