package com.example.demo1;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

public class Product {
    private int productID;
    private String name;
    private BigDecimal price;
    private int stock;
    private String description;
    private byte[] imageData; // Store image as binary data
    private String category;  // Add category field (Pastries or Drinks)

    // Constructor with category
    public Product(int productID, String name, BigDecimal price, int stock, String description, byte[] imageData, String category) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imageData = imageData;
        this.category = category; // Initialize category
    }

    // Getters and Setters
    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return String.valueOf(price);
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    // Get image from byte[] for JavaFX UI
    public Image getImage() {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null; // No image available
    }

    // Get category
    public String getCategory() {
        return category;
    }

    // Set category
    public void setCategory(String category) {
        this.category = category;
    }
}
