package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AddProductController {
    @FXML
    private TextField nameField, descField, stockField, priceField;
    @FXML
    private Label imagePathLabel;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ImageView productImage;

    private BaseInventoryController baseInventoryController;
    private byte[] imageBytes = null;

    public void setBaseInventoryController(BaseInventoryController controller) {
        this.baseInventoryController = controller;
    }

    public void prefillProductName(String name) {
        nameField.setText(name);
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage currentStage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(currentStage);

        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                productImage.setImage(image);
                imagePathLabel.setText(file.getName());
                imageBytes = convertImageToBytes(file);
            } catch (Exception e) {
                baseInventoryController.showAlert("Error", "Failed to load image.");
                e.printStackTrace();
            }
        }
    }

    private byte[] convertImageToBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            baseInventoryController.showAlert("Error", "Failed to load image file.");
            return null;
        }
    }

    @FXML
    public void initialize() {
        categoryComboBox.getItems().addAll("Pastries", "Drinks");
        categoryComboBox.getSelectionModel().selectFirst();

        // Show/hide stockField based on selected category
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDrinks = "Drinks".equalsIgnoreCase(newVal);
            stockField.setVisible(!isDrinks);
        });

        // Apply visibility initially
        stockField.setVisible(!"Drinks".equalsIgnoreCase(categoryComboBox.getValue()));
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String stockText = stockField.getText().trim();
        String priceText = priceField.getText().trim();
        String category = categoryComboBox.getValue();

        if (name.isEmpty() || desc.isEmpty() || priceText.isEmpty() ||
                (!"Drinks".equalsIgnoreCase(category) && stockText.isEmpty())) {
            baseInventoryController.showAlert("⚠ Warning", "Missing Information. Please fill all fields.");
            return;
        }

        int stock = 0;
        double price;

        if (!"Drinks".equalsIgnoreCase(category)) {
            try {
                stock = Integer.parseInt(stockText);
            } catch (NumberFormatException e) {
                baseInventoryController.showAlert("⚠ Error", "Invalid Stock. Please enter a number.");
                return;
            }
        }

        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            baseInventoryController.showAlert("⚠ Error", "Invalid Price. Please enter a number.");
            return;
        }

        if (DBConnection.getProductByName(name) != null) {
            baseInventoryController.showAlert("⚠ Error", "A product with this name already exists.");
            return;
        }

        boolean success = DBConnection.addProductToDatabase(name, desc, stock, price, imageBytes, category);
        if (success) {
            baseInventoryController.showAlert("Success", "Product added successfully!");
            if (baseInventoryController != null) {
                baseInventoryController.loadProductsForPastries();
                baseInventoryController.loadProductsForDrinks();
            }
            closeWindow();
        } else {
            baseInventoryController.showAlert("⚠ Error", "Failed to save product. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
