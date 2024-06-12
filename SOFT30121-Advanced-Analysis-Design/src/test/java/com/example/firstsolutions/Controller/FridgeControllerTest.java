package com.example.firstsolutions.Controller;

import javafx.scene.control.TableView;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.junit.jupiter.api.Assertions.assertNotNull;
class FridgeControllerTest {

    @Test
    void testGetAllFridgeItems() {
        FridgeController fridgeController = new FridgeController();
        List<FridgeController.FridgeItem> fridgeItems = fridgeController.getAllFridgeItems();
        assertNotNull(fridgeItems);

    }

}
