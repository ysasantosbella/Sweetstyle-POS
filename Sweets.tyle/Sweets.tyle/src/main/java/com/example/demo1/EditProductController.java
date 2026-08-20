package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProductController {

    @FXML
    private TextField nameField, priceField, stockField, descriptionField;
    @FXML
    private Button saveButton, closeButton;
    @FXML
    private ImageView productImage, previewProductImage;
    @FXML
    private Label imagePathLabel, productNameLabel, descriptionLabel, priceLabel, stockLabel, stockLabel2;
    @FXML
    private ComboBox<String> categoryComboBox;

    private int productID;
    private BaseInventoryController baseController;
    private String imagePath = "";
    private byte[] existingImageBytes;

    public void setProduct(int productID) {
        this.productID = productID;
        loadProductDetails();
    }

    public void setBaseController(BaseInventoryController controller) {
        this.baseController = controller;
    }

    private void loadProductDetails() {
        String query = "SELECT * FROM ProductTable WHERE ProductID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                priceField.setText(rs.getBigDecimal("price").toString());
                stockField.setText(String.valueOf(rs.getInt("stock")));
                descriptionField.setText(rs.getString("product_description"));

                String category = rs.getString("category");
                categoryComboBox.setValue(category);

                byte[] imageBytes = rs.getBytes("image");
                if (imageBytes != null && imageBytes.length > 0) {
                    existingImageBytes = imageBytes;
                    Image image = new Image(new ByteArrayInputStream(imageBytes));
                    previewProductImage.setImage(image);
                    productImage.setImage(image);
                }

                // Adjust stock field visibility based on the category
                updateStockFieldVisibility(category);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This method will update the stock fields visibility based on the selected category
    private void updateStockFieldVisibility(String category) {
        if ("Drinks".equalsIgnoreCase(category)) {
            stockField.setVisible(false);
            stockLabel.setVisible(false);
            stockLabel2.setVisible(false);
        } else {
            stockField.setVisible(true);
            stockLabel.setVisible(true);
            stockLabel2.setVisible(true);
        }
    }

    private void updatePreview() {
        productNameLabel.setText(nameField.getText().isEmpty() ? "Product Name" : nameField.getText());
        descriptionLabel.setText(descriptionField.getText().isEmpty() ? "No description available" : descriptionField.getText());
        priceLabel.setText("Price: $" + (priceField.getText().isEmpty() ? "0.00" : priceField.getText()));
        stockLabel.setText("Stock: " + (stockField.getText().isEmpty() ? "0" : stockField.getText()));

        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                previewProductImage.setImage(new Image(file.toURI().toString()));
            }
        }

        // Update stock field visibility based on category selection
        updateStockFieldVisibility(categoryComboBox.getValue());
    }

    @FXML
    private void initialize() {
        // Initialize category combo box with categories
        categoryComboBox.getItems().addAll("Drinks", "Pastries");

        // Add listener for category selection
        categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateStockFieldVisibility(newValue));

        // Update preview when any field changes
        nameField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        priceField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        stockField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> updatePreview());

        // Image Drag & Drop Handling
        productImage.setOnDragOver(event -> {
            if (event.getGestureSource() != productImage && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        productImage.setOnDragDropped(event -> {
            var db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                setProductImage(file);
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void setProductImage(File file) {
        if (file.exists()) {
            imagePath = file.getAbsolutePath();
            productImage.setImage(new Image(file.toURI().toString()));
            previewProductImage.setImage(new Image(file.toURI().toString()));
            imagePathLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage currentStage = (Stage) saveButton.getScene().getWindow();

        File file = fileChooser.showOpenDialog(currentStage);

        if (file != null) {
            setProductImage(file);
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || priceField.getText().isEmpty() ||
                stockField.getText().isEmpty() || descriptionField.getText().isEmpty() ||
                categoryComboBox.getValue() == null) {
            baseController.showAlert("Missing Information", "Please fill in all fields before saving.");
            return;
        }

        String query = "UPDATE ProductTable SET name = ?, price = ?, stock = ?, product_description = ?, category = ?, image = ? WHERE ProductID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nameField.getText());
            stmt.setBigDecimal(2, new BigDecimal(priceField.getText()));
            stmt.setInt(3, Integer.parseInt(stockField.getText()));
            stmt.setString(4, descriptionField.getText());
            stmt.setString(5, categoryComboBox.getValue());

            byte[] imageBytes = imagePath.isEmpty() ? existingImageBytes : convertImageToBytes(new File(imagePath));
            stmt.setBytes(6, imageBytes);

            stmt.setInt(7, productID);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                baseController.showAlert("Success", "Product updated successfully!");
                baseController.loadProductsForPastries();
                baseController.loadProductsForDrinks();
                closePopup();
            } else {
                baseController.showAlert("Error", "Failed to update product.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            baseController.showAlert("Database Error", "An error occurred while updating the product.");
        }
    }

    private byte[] convertImageToBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void handleClose() {
        closePopup();
    }

    private void closePopup() {
        if (baseController != null) {
            baseController.removeBlur();
        }
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
