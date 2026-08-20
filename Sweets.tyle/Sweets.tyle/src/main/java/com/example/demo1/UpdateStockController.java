package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateStockController {
    @FXML
    private TextField newStockField;
    @FXML
    private Label currentStockLabel, productIDLabel; // Display the current stock

    private int productID;
    private BaseInventoryController baseController;

    public void setProduct(int productID, int currentStock) {
        this.productID = productID;
        productIDLabel.setText("Product ID: " + productID);
        currentStockLabel.setText(String.valueOf(currentStock)); // Show current stock
    }

    public void setBaseController(BaseInventoryController baseController) {
        this.baseController = baseController;
    }

    @FXML
    private void handleSave() {
        try {
            int newStock = Integer.parseInt(newStockField.getText().trim());
            DBConnection.updateStock(productID, newStock);
            System.out.println("Stock updated successfully!");

            // Refresh inventory and close the pop-up
            baseController.loadProductsForPastries();
            baseController.loadProductsForDrinks();
            baseController.handleClosePopup();
        } catch (NumberFormatException e) {
            baseController.showAlert("Error", "Invalid stock input! Please enter a valid number.");
        }
    }

    @FXML
    private void handleCancel() {
        baseController.handleClosePopup(); // Close pop-up and remove blur
    }
}
