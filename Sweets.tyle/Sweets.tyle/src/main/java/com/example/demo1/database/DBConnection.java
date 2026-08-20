package com.example.demo1.database;

import com.example.demo1.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=SweetStyleDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sweetstyle";
    private static final String PASSWORD = "sweetstyle";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ADD PRODUCT (With Image and Category)
    public static boolean addProductToDatabase(String name, String description, int stock, double price, byte[] imageBytes, String category) {
        String query = "INSERT INTO ProductTable (name, product_description, stock, price, image, category) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, stock);
            stmt.setDouble(4, price);

            // Allow NULL images
            if (imageBytes != null) {
                stmt.setBytes(5, imageBytes);
            } else {
                stmt.setNull(5, java.sql.Types.BLOB);
            }

            stmt.setString(6, category);  // Added category parameter

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product added successfully!");
                return true; // Return success
            } else {
                System.out.println("⚠ Failed to add product.");
                return false; // Return failure
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("⚠ Error: A product with this name already exists.");
            } else {
                System.out.println("⚠ Database error: " + e.getMessage());
            }
            return false;
        }
    }

    // Convert File to byte array (Helper method)
    public static byte[] convertImageToBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // UPDATE STOCK
    public static void updateStock(int productID, int newStock) {
        String query = "UPDATE ProductTable SET stock = ? WHERE ProductID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productID);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Stock updated successfully.");
            } else {
                System.out.println("⚠ No product found with ID: " + productID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE PRODUCT FROM DATABASE
    public static void deleteProduct(int productID) {
        String query = "DELETE FROM ProductTable WHERE ProductID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productID);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Product deleted successfully.");
            } else {
                System.out.println("⚠ No product found with ID: " + productID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // FETCH ALL PRODUCTS (With Image)
    public static ObservableList<Product> getAllProducts() {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        String query = "SELECT * FROM ProductTable";  // No category filter here, fetch all products

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("ProductID");
                String name = rs.getString("name");
                String desc = rs.getString("product_description");
                int stock = rs.getInt("stock");
                BigDecimal price = rs.getBigDecimal("price");
                String category = rs.getString("category"); // Fetch category

                // Retrieve image as byte array
                byte[] imageBytes = rs.getBytes("image");

                productList.add(new Product(id, name, price, stock, desc, imageBytes, category)); // Added category to Product constructor
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    // FETCH PRODUCTS BY CATEGORY (With Image)
    public static ObservableList<Product> getProductsByCategory(String category) {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        String query = "SELECT * FROM ProductTable WHERE category = ?";  // Fetch products by category

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("ProductID");
                String name = rs.getString("name");
                String desc = rs.getString("product_description");
                int stock = rs.getInt("stock");
                BigDecimal price = rs.getBigDecimal("price");
                String productCategory = rs.getString("category"); // Fetch category

                // Retrieve image as byte array
                byte[] imageBytes = rs.getBytes("image");

                productList.add(new Product(id, name, price, stock, desc, imageBytes, productCategory)); // Added category to Product constructor
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    public static Product getProductByName(String name) {
        String query = "SELECT * FROM ProductTable WHERE name = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("image");
                String category = rs.getString("category");  // Fetch category

                return new Product(
                        rs.getInt("ProductID"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock"),
                        rs.getString("product_description"),
                        imageBytes,
                        category  // Added category to Product constructor
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
