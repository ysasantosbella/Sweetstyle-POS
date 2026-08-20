package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class BaseInventoryController {
    @FXML
    private Button addButton, editButton, deleteButton, stockButton, logoutButton;
    @FXML
    private Label roleLabel;
    @FXML
    private GridPane pastriesGrid, drinksGrid;
    @FXML
    private TextField productField;
    @FXML
    private TabPane tabPane; // Add TabPane for Tabs
    @FXML
    private Tab pastriesTab, drinksTab; // Tabs for Pastries and Drinks
    @FXML
    private ScrollPane PastriesScrollPane, DrinksScrollPane;
    @FXML
    private VBox navigationPane;

    private boolean isSidebarVisible = true;
    private Product selectedProduct = null;
    private String userRole;
    private Parent inventoryRoot; // Stores the root of the inventory screen
    private Stage popupStage; // Stores the popup stage reference
    private double offsetX, offsetY;

    // This will load products based on the selected tab (Pastries or Drinks)
    public void setUserRole(String role) {
        this.userRole = role;
        roleLabel.setText(role + "!");

        // Disable buttons based on role
        if (role.equals("Cashier")) {
            addButton.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        // Load data for Pastries and Drinks tabs
        loadProductsForPastries();

        pastriesTab.setClosable(false);
        drinksTab.setClosable(false);

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == pastriesTab) {
                loadProductsForPastries();
                PastriesScrollPane.setVisible(true);
                DrinksScrollPane.setVisible(false);
            } else if (newTab == drinksTab) {
                loadProductsForDrinks();
                PastriesScrollPane.setVisible(false);
                DrinksScrollPane.setVisible(true);
            }
        });


        // Filter products when text is entered
        productField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayProducts(newValue);
        });

        navigationPane.setTranslateX(-navigationPane.getPrefWidth());
        isSidebarVisible = false;
    }

    public void setInventoryRoot(Parent root) {
        this.inventoryRoot = root;
        if (inventoryRoot != null) {
            System.out.println("inventoryRoot set successfully!");
        } else {
            System.out.println("Failed to set inventoryRoot!");
        }
    }

    // Method to load products for Pastries tab
    public void loadProductsForPastries() {
        pastriesGrid.getChildren().clear();
        int column = 0, row = 0;

        // Modify the DB call to fetch only Pastries products
        List<Product> pastries = DBConnection.getProductsByCategory("Pastries");

        for (Product product : pastries) {
            VBox productCard = createProductCard(product);
            pastriesGrid.add(productCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
        // Add the pastriesGrid to the PastriesScrollPane
        PastriesScrollPane.setContent(pastriesGrid);
    }

    // Method to load products for Drinks tab
    public void loadProductsForDrinks() {
        drinksGrid.getChildren().clear();
        int column = 0, row = 0;

        // Modify the DB call to fetch only Drinks products
        List<Product> drinks = DBConnection.getProductsByCategory("Drinks");

        for (Product product : drinks) {
            VBox productCard = createProductCard(product);
            drinksGrid.add(productCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
        // Add the drinksGrid to the DrinksScrollPane
        DrinksScrollPane.setContent(drinksGrid);
    }

    private VBox createProductCard(Product product) {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(30));
        vbox.setPrefSize(500, 300);
        vbox.setStyle("-fx-border-color: #d9edf4; -fx-border-width: 2; -fx-background-color: #fdd3e1;");

        // ImageView for product
        ImageView productImage = new ImageView();
        productImage.setFitHeight(150);
        productImage.setFitWidth(150);

        if (product.getImage() != null) {
            productImage.setImage(product.getImage());
        }

        // Labels
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label priceLabel = new Label("Price: " + product.getPrice());
        Label stockLabel = new Label("Stock: " + product.getStock());
        Label descLabel = new Label(product.getDescription());
        descLabel.setWrapText(true);

        // Always add image, name, price, description
        vbox.getChildren().addAll(productImage, nameLabel, priceLabel);

        // Add stockLabel only if category is "Pastry"
        if ("Pastries".equalsIgnoreCase(product.getCategory())) {
            vbox.getChildren().add(stockLabel);
        }

        vbox.getChildren().add(descLabel);

        vbox.setOnMouseClicked(event -> {
            selectedProduct = product;
            GridPane currentGrid = tabPane.getSelectionModel().getSelectedIndex() == 0 ? pastriesGrid : drinksGrid;
            highlightProduct(currentGrid, vbox, selectedProduct);
            updateButtonStates();
            if (event.getClickCount() == 2) {
                showProductDetails(selectedProduct);
            }
        });

        return vbox;
    }


    private void showProductDetails(Product product) {
        if (product == null) return;

        applyBlur();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Details");
        alert.setHeaderText(product.getName());
        alert.setContentText(
                "Price: " + product.getPrice() +
                        "\nStock: " + product.getStock() +
                        "\nDescription: " + product.getDescription()
        );

        // Show alert on top of the grid window (pastriesGrid or drinksGrid)
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initOwner(pastriesGrid.getScene().getWindow()); // Use pastriesGrid or drinksGrid here
        alertStage.setAlwaysOnTop(true);

        alert.showAndWait();
        removeBlur();
    }


    private void highlightProduct(GridPane grid, VBox selectedCard, Product product) {
        if (selectedProduct == null) {
            return;
        }

        // Iterate over all nodes in the grid
        for (var node : grid.getChildren()) {
            if (node instanceof VBox) {
                // Reset style for all VBox elements in the grid
                node.setStyle("-fx-border-color: #d9edf4; -fx-border-width: 2; -fx-background-color: #fdd3e1;");
            }
        }

        // Apply a different style to the selected product
        selectedCard.setStyle("-fx-border-color: black; -fx-border-width: 3; -fx-background-color: #fdd3e1;");

        // Now, check the category of the selected product
        checkProductCategory(product);
    }

    private void checkProductCategory(Product product) {
        // Check if the product belongs to a specific category
        if (product.getCategory().equalsIgnoreCase("Pastries")) {
            System.out.println("Product belongs to Pastries category.");
            // Perform specific actions for Pastries
        } else if (product.getCategory().equalsIgnoreCase("Drinks")) {
            System.out.println("Product belongs to Drinks category.");
            // Perform specific actions for Drinks
        } else {
            System.out.println("Product belongs to another category: " + product.getCategory());
            // Handle other categories if necessary
        }
    }

    private void updateButtonStates() {
        if (selectedProduct != null) {
            stockButton.setDisable(false);

            if (userRole.equals("Admin")) {
                addButton.setDisable(false);
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            }
        }
    }

    private void filterAndDisplayProducts(String keyword) {
        // Clear the current grid based on the active tab
        if (tabPane.getSelectionModel().getSelectedItem() == pastriesTab) {
            pastriesGrid.getChildren().clear();
        } else {
            drinksGrid.getChildren().clear();
        }

        // Fetch all products based on category
        List<Product> allProducts = DBConnection.getAllProducts();

        // Filter and sort alphabetically
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().startsWith(keyword.toLowerCase()))
                .sorted(Comparator.comparing(Product::getName))
                .toList();

        int column = 0, row = 0;

        // Populate the grid with filtered products based on the active tab
        for (Product product : filtered) {
            VBox productCard = createProductCard(product);

            if (tabPane.getSelectionModel().getSelectedItem() == pastriesTab) {
                pastriesGrid.add(productCard, column, row);
            } else {
                drinksGrid.add(productCard, column, row);
            }

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }


    @FXML
    private void handleAddProduct() {
        String productName = productField.getText().trim(); // Get product name from input field

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add_product.fxml"));
            Parent root = loader.load();

            AddProductController controller = loader.getController();
            controller.setBaseInventoryController(this); // Link to inventory

            // Pre-fill name if available
            if (!productName.isEmpty()) {
                controller.prefillProductName(productName);
            }

            showPopup(root, "Add Product");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Product window.");
        }
    }


    @FXML
    private void handleEdit() {
        String productName = productField.getText().trim();

        if (selectedProduct == null && productName.isEmpty()) {
            showAlert("No product selected", "Please select or enter a product first.");
            return;
        }

        Product productToEdit = selectedProduct;
        if (productToEdit == null) {
            productToEdit = DBConnection.getProductByName(productName);
        }

        if (productToEdit == null) {
            showAlert("Error", "Product not found!, The entered product name does not exist.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit_product.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProduct(productToEdit.getProductID());
            controller.setBaseController(this);

            showPopup(root, "Edit Product");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Edit Product window.");

        }
    }


    @FXML
    private void handleStockUpdate() {
        String productName = productField.getText().trim();

        // Check if no product is selected and the product name is empty
        if (selectedProduct == null && productName.isEmpty()) {
            showAlert("No product selected", "Please select or enter a product first.");
            return;
        }

        // Check if the product is of the "Drinks" category and prevent stock update if it is
        if (selectedProduct != null && "Drinks".equalsIgnoreCase(selectedProduct.getCategory())) {
            showAlert("Cannot update stock", "Stock update is not allowed for drinks.");
            return;
        }

        // If no product is selected, try to fetch by name
        Product productToUpdate = selectedProduct;
        if (productToUpdate == null) {
            productToUpdate = DBConnection.getProductByName(productName);
        }

        // If product not found
        if (productToUpdate == null) {
            showAlert("Error", "Product not found! The entered product name does not exist.");
            return;
        }

        try {
            // Load the stock update dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("stock.fxml"));
            Parent root = loader.load();

            UpdateStockController controller = loader.getController();
            controller.setProduct(productToUpdate.getProductID(), productToUpdate.getStock()); // Pass current stock
            controller.setBaseController(this);

            // Show the stock update popup
            showPopup(root, "Update Stock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleDelete() {
        String productName = productField.getText().trim();

        // Ensure a product is selected or entered
        if (selectedProduct == null && productName.isEmpty()) {
            showAlert("No product selected", "Please select or enter a product first.");
            return;
        }

        // Fetch product by name if no selection
        final Product productToDelete = (selectedProduct != null) ? selectedProduct : DBConnection.getProductByName(productName);

        if (productToDelete == null) {
            showAlert("Error", "Product not found! The entered product name does not exist.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this product?", ButtonType.YES, ButtonType.NO);
        confirmationAlert.setTitle("Delete Confirmation");

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                DBConnection.deleteProduct(productToDelete.getProductID());
                loadProductsForDrinks();
                loadProductsForPastries();
                selectedProduct = null;
                productField.clear();
            }
        });
    }

    @FXML
    private void handleExit() {
        // Close the application
        Platform.exit();
    }

    private void showPopup(Parent root, String title) {
        popupStage = new Stage();
        popupStage.initModality(Modality.WINDOW_MODAL); // Attach pop-up to the selected grid
        popupStage.initOwner(pastriesGrid.getScene().getWindow()); // Or drinksGrid if needed

        Scene scene = new Scene(root);
        popupStage.setScene(scene);

        // Ensure the window opens at its designed size
        popupStage.sizeToScene(); // This makes sure it takes the exact FXML size
        popupStage.setResizable(false); // Prevents resizing if not needed
        popupStage.setTitle(title);
        popupStage.initStyle(StageStyle.UNDECORATED); // Removes the title bar

        applyBlur();
        popupStage.setOnHidden(event -> removeBlur());

        // Make window draggable
        root.setOnMousePressed(event -> {
            offsetX = event.getSceneX();
            offsetY = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - offsetX);
            popupStage.setY(event.getScreenY() - offsetY);
        });

        popupStage.show();
    }


    private void applyBlur() {
        if (inventoryRoot != null) {
            inventoryRoot.setEffect(new BoxBlur(10, 10, 3)); // ✅ Apply blur
            System.out.println("✅ Blur applied!");
        } else {
            System.out.println("⚠️ inventoryRoot is NULL, cannot apply blur.");
        }
    }

    public void removeBlur() {
        if (inventoryRoot != null) {
            inventoryRoot.setEffect(null); // ✅ Remove blur
            System.out.println("✅ Blur removed!");
        } else {
            System.out.println("⚠️ inventoryRoot is NULL, cannot remove blur.");
        }
    }


    public void handleClosePopup() {
        if (popupStage != null) {
            popupStage.close();
        }
        removeBlur();
    }

    @FXML
    protected void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("SweetStyle Café - Login");
            stage.show();
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlert(String title, String message) {
        showAlert(title, message, null); // Call the existing method with null for the grid
    }

    public void showAlert(String title, String message, GridPane grid) {
        if (inventoryRoot != null) applyBlur(); // Apply blur before showing alert

        Alert alert = new Alert(Alert.AlertType.INFORMATION); // Can be changed to WARNING if needed
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.initModality(Modality.WINDOW_MODAL); // Attach to another window

        // Attach alert to the passed grid (either pastriesGrid or drinksGrid)
        if (grid != null) {
            alertStage.initOwner(grid.getScene().getWindow()); // Set the passed grid as the owner
        } else {
            // If grid is null, you can set a default owner or skip it.
            alertStage.initOwner(inventoryRoot.getScene().getWindow()); // Default to inventoryRoot or use another window
        }

        alertStage.setAlwaysOnTop(true); // Ensures the alert stays above all windows

        alert.showAndWait();

        if (inventoryRoot != null) removeBlur(); // Remove blur after closing alert
    }

    @FXML
    private void toggleSidebar(ActionEvent event) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), navigationPane);
        if (isSidebarVisible) {
            // Slide out (hide)
            transition.setToX(-navigationPane.getWidth());
            isSidebarVisible = false;
        } else {
            // Slide in (show)
            transition.setToX(0);
            isSidebarVisible = true;
        }
        transition.play();
    }

    // Example method for Dashboard
    @FXML
    private void goToSelling(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("selling.fxml"));
            Parent root = loader.load();

            SellingController controller = loader.getController();
            controller.setUserRole(userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Inventory button (reload current if needed)
    @FXML
    private void goToInventory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("inventory.fxml"));
            Parent root = loader.load();

            BaseInventoryController controller = loader.getController();
            controller.setUserRole(userRole);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}