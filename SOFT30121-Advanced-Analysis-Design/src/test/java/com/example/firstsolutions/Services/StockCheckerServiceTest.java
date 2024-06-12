package com.example.firstsolutions.Services;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockCheckerServiceTest {

    private DynamoDbClientStub dynamoDbClientStub;
    private StockCheckerService stockCheckerService;
    private ByteArrayOutputStream consoleOutput;

    @Before
    public void setUp() {
        dynamoDbClientStub = new DynamoDbClientStub();
        stockCheckerService = new StockCheckerService(dynamoDbClientStub);
        consoleOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consoleOutput, true)); // Set the second parameter to 'true'
    }

    @Test
    public void testReOrderStockOnMonday() {
        LocalDate monday = LocalDate.of(2024, 2, 12);
        boolean result = stockCheckerService.reOrderStock(Arrays.asList("Item1", "Item2"));
        assertTrue("Reorder on Monday failed", dynamoDbClientStub.wasPutItemCalled());
        assertTrue("Expected reorder on Monday", result);
        assertConsoleOutputContains("Automatic Reorder Alert");
    }




    @Test
    public void testCheckLowStockItemsWithNoLowStock() {
        dynamoDbClientStub.setItems(Collections.singletonList(
                createDynamoDbItem("Item1", 7)
        ));
        stockCheckerService.checkLowStockItems();
        assertConsoleOutputDoesNotContain("Low stock on:");
        assertFalse(dynamoDbClientStub.wasPutItemCalled());
    }

    private void assertConsoleOutputContains(String expected) {
        assertTrue(consoleOutput.toString().contains(expected));
    }

    private void assertConsoleOutputDoesNotContain(String unexpected) {
        assertFalse(consoleOutput.toString().contains(unexpected));
    }

    private Map<String, AttributeValue> createDynamoDbItem(String itemName, int quantity) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("Name", AttributeValue.builder().s(itemName).build());
        item.put("Quantity", AttributeValue.builder().n(String.valueOf(quantity)).build());
        return item;
    }

    private static class DynamoDbClientStub implements DynamoDbClient {

        private boolean putItemCalled = false;
        private boolean throwException = false;
        private List<Map<String, AttributeValue>> items = Collections.emptyList();

        @Override
        public PutItemResponse putItem(PutItemRequest putItemRequest) {
            putItemCalled = true;
            if (throwException) {
                throw DynamoDbException.builder().message("Simulated error").build();
            }
            return PutItemResponse.builder().build();
        }

        @Override
        public ScanResponse scan(ScanRequest scanRequest) {
            return ScanResponse.builder().items(items).build();
        }

        @Override
        public void close() {
            // Implement the close method, but it can be left empty for this stub
        }

        public void setItems(List<Map<String, AttributeValue>> items) {
            this.items = items;
        }

        public boolean wasPutItemCalled() {
            return putItemCalled;
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

        @Override
        public String serviceName() {
            return "dynamodb";
        }
    }
}
