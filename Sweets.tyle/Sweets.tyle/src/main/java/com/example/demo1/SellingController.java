package com.example.demo1;

import com.example.demo1.database.DBConnection;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SellingController implements Initializable {
    @FXML
    private ScrollPane ProductScrollPane;

    @FXML
    private VBox productsList;

    @FXML
    private VBox selectedProductsList;

    @FXML
    private Label totalPrice;

    @FXML
    private Button acceptOrderButton;
    @FXML
    private VBox navigationPane;
    private boolean isSidebarVisible = true;

    private List<Product> allProducts;

    private final HashMap<String, HBox> selectedProductMap = new HashMap<>();
    private final HashMap<String, Integer> productQuantityMap = new HashMap<>();
    private String userRole;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allProducts = DBConnection.getAllProducts();

        for (Product product : allProducts) {
            HBox card = createProductCard(product);
            productsList.getChildren().add(card);
        }

        acceptOrderButton.setOnAction(event -> handleAcceptOrder());
        ProductScrollPane.setFitToWidth(true);
    }

    private HBox createProductCard(Product product) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-font-size: 13");

        ImageView imageView = new ImageView();
        if (product.getImageData() != null) {
            Image img = new Image(new ByteArrayInputStream(product.getImageData()));
            imageView.setImage(img);
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
        }

        Label name = new Label(product.getName());
        Label price = new Label("₱" + product.getPrice());
        VBox infoBox = new VBox(5);

        infoBox.getChildren().addAll(name, price);

        if ("Pastries".equalsIgnoreCase(product.getCategory())) {
            Label stock = new Label("Stock: " + product.getStock());
            infoBox.getChildren().add(stock);
        }


        card.getChildren().addAll(imageView, infoBox);

        card.setOnMouseClicked(event -> displaySelectedProduct(product));

        return card;
    }


    private void displaySelectedProduct(Product product) {
        String productName = product.getName();

        if (selectedProductMap.containsKey(productName)) {
            int currentQty = productQuantityMap.get(productName) + 1;
            productQuantityMap.put(productName, currentQty);

            HBox existingCard = selectedProductMap.get(productName);
            Label qtyLabel = (Label) existingCard.getChildren().get(2); // Ensure this is always correct
            qtyLabel.setText("x" + currentQty);
        } else {
            HBox selectedProductCard = new HBox(20);
            selectedProductCard.setPadding(new Insets(10));
            selectedProductCard.setAlignment(Pos.CENTER_LEFT);
            selectedProductCard.setStyle("-fx-border-color: #ccc; -fx-background-color: #fff; -fx-border-radius: 5; -fx-background-radius: 5;");
            selectedProductCard.setStyle("-fx-background-color: #fff; -fx-background-radius: 5;");


            Label nameLabel = new Label(productName);
            Label priceLabel = new Label("₱" + product.getPrice());
            Label qtyLabel = new Label("x1");

            selectedProductCard.getChildren().addAll(nameLabel, priceLabel, qtyLabel);

            selectedProductCard.setOnMouseClicked(event -> {
                int currentQty = productQuantityMap.get(productName) - 1;

                if (currentQty <= 0) {
                    selectedProductsList.getChildren().remove(selectedProductCard);
                    selectedProductMap.remove(productName);
                    productQuantityMap.remove(productName);
                } else {
                    productQuantityMap.put(productName, currentQty);
                    qtyLabel.setText("x" + currentQty);
                }
                updateTotalPrice(); // Called after change
            });

            selectedProductsList.getChildren().add(selectedProductCard);
            selectedProductMap.put(productName, selectedProductCard);
            productQuantityMap.put(productName, 1);
        }

        // Always update the price after any product is clicked
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : productQuantityMap.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            for (Product p : allProducts) {
                if (p.getName().equals(productName)) {
                    total += Double.parseDouble(p.getPrice()) * quantity;
                    break;
                }
            }
        }

        totalPrice.setText("Total: ₱" + String.format("%.2f", total));
    }

    private void handleAcceptOrder() {
        for (Map.Entry<String, Integer> entry : productQuantityMap.entrySet()) {
            String productName = entry.getKey();
            int quantityPurchased = entry.getValue();

            for (Product product : allProducts) {
                if (product.getName().equals(productName)) {

                    // Only update stock for pastries
                    if ("Pastries".equalsIgnoreCase(product.getCategory())) {
                        int newStock = product.getStock() - quantityPurchased;
                        product.setStock(newStock);
                        DBConnection.updateStock(product.getProductID(), newStock);

                        // Update stock label in UI
                        for (Node node : productsList.getChildren()) {
                            if (node instanceof HBox) {
                                HBox hbox = (HBox) node;
                                if (hbox.getChildren().size() >= 2 && hbox.getChildren().get(1) instanceof VBox) {
                                    VBox infoBox = (VBox) hbox.getChildren().get(1);
                                    Label nameLabel = (Label) infoBox.getChildren().get(0);
                                    if (nameLabel.getText().equals(productName)) {
                                        // Only update stock label if it exists
                                        if (infoBox.getChildren().size() > 2) {
                                            Label stockLabel = (Label) infoBox.getChildren().get(2);
                                            stockLabel.setText("Stock: " + newStock);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    break;
                }
            }
        }

        // Clear UI after placing the order
        selectedProductsList.getChildren().clear();
        selectedProductMap.clear();
        productQuantityMap.clear();
        totalPrice.setText("Total: ₱0.00");
    }


    public void setUserRole(String userRole) {
        this.userRole = userRole;
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
