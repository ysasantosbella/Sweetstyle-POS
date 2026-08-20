package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.sql.*;

public class LoginController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblMessage;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String role = authenticateUser(username, password);

        if (role != null) {
            lblMessage.setText("Login successful! Redirecting...");
            openInventory(role);
        } else {
            lblMessage.setText("Invalid username or password!");
        }
    }

    private String authenticateUser(String username, String password) {
        String queryAdmin = "SELECT 'Admin' AS role FROM AdminTable WHERE username = ? AND password = ?";
        String queryCashier = "SELECT 'Cashier' AS role FROM CashierTable WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection()) {
            // Check Admin Table
            try (PreparedStatement stmt = conn.prepareStatement(queryAdmin)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("role"); // Returns "Admin"
                }
            }

            // Check Cashier Table
            try (PreparedStatement stmt = conn.prepareStatement(queryCashier)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("role"); // Returns "Cashier"
                }
            }
        } catch (SQLException e) {
            lblMessage.setText("Database error: " + e.getMessage());
        }
        return null;
    }


    private void openInventory(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("inventory.fxml"));
            Parent root = loader.load(); // Load Inventory UI

            BaseInventoryController controller = loader.getController();
            controller.setUserRole(role);
            controller.setInventoryRoot(root); // Set inventoryRoot before showing

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(role + "Inventory");
            stage.setFullScreen(true);
            stage.setAlwaysOnTop(false); // Keeps the inventory behind popups but not behind other apps
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
