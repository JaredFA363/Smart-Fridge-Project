module com.example.firstsolutions {
    exports com.example.firstsolutions.Services;
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.dynamodb;
    requires com.google.common;
    requires junit;
    requires software.amazon.awssdk.services.sqs;

    opens com.example.firstsolutions to javafx.fxml;
    exports com.example.firstsolutions;
    exports com.example.firstsolutions.Controller;
    opens com.example.firstsolutions.Controller to javafx.fxml;
    exports com.example.firstsolutions.Controller.HeadChefController;
    opens com.example.firstsolutions.Controller.HeadChefController to javafx.fxml;
    exports com.example.firstsolutions.Controller.DeliveryController;
    opens com.example.firstsolutions.Controller.DeliveryController to javafx.fxml;
    exports com.example.firstsolutions.Controller.ChefController;
    opens com.example.firstsolutions.Controller.ChefController to javafx.fxml;
    exports com.example.firstsolutions.Controller.ServerController;
    opens com.example.firstsolutions.Controller.ServerController to javafx.fxml;
}