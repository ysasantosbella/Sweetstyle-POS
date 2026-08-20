module demo1 {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports com.example.demo1; // Export the package for other modules
    opens com.example.demo1 to javafx.fxml; // Open the package for reflection
}