package com.example.firstsolutions.Controller;

import com.example.firstsolutions.Services.UniqueIdGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

import javafx.scene.control.*;
;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;

public class AddItemDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField expiryDateField;

    private Dialog<FridgeItem> dialog;

    public void setDialog(Dialog<FridgeItem> dialog) {
        this.dialog = dialog;
    }

    @FXML
    private void onAddButtonClick() {
        if (nameField.getText().isEmpty() || quantityField.getText().isEmpty() || expiryDateField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
        } else {
            String uniqueUserID = UniqueIdGenerator.uniqueIdGenerator();
            FridgeItem item = new FridgeItem(uniqueUserID, nameField.getText(), quantityField.getText(), expiryDateField.getText());
            dialog.setResult(item);
            dialog.close();
        }
    }

    public static class FridgeItem {
        private final StringProperty itemID;
        private final StringProperty name;
        private final StringProperty quantity;
        private final StringProperty expiryDate;

        public FridgeItem(String itemID, String name, String quantity, String expiryDate) {
            this.itemID = new SimpleStringProperty(itemID);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleStringProperty(quantity);
            this.expiryDate = new SimpleStringProperty(expiryDate);
        }

        public StringProperty getItemIDProperty() {
            return itemID;
        }

        public String getItemID() {
            return itemID.get();
        }

        public void setItemID(String newID) {
            itemID.set(newID);
        }

        public StringProperty getNameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String newName) {
            name.set(newName);
        }

        public StringProperty getQuantityProperty() {
            return quantity;
        }

        public String getQuantity() {
            return quantity.get();
        }

        public void setQuantity(String newQuantity) {
            quantity.set(newQuantity);
        }

        public StringProperty getExpiryDateProperty() {
            return expiryDate;
        }

        public String getExpiryDate() {
            return expiryDate.get();
        }

        public void setExpiryDate(String newExpiryDate) {
            expiryDate.set(newExpiryDate);
        }
    }
}
