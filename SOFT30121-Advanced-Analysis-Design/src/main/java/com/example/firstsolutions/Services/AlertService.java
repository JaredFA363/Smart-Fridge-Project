package com.example.firstsolutions.Services;

import com.example.firstsolutions.Aws.AWSConfig;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.Map;

public class AlertService {
    private final SqsClient sqsClient;
    private final String queueUrl;

    private final AWSConfig awsConfig = new AWSConfig();
    private DynamoDbClient dynamoDbClient;

    public AlertService() {
        this.sqsClient = AWSConfig.getSqsClient();
        this.queueUrl = "https://sqs.eu-west-2.amazonaws.com/352374022025/HeadChefAlertQueue.fifo";
    }

    public void sendAlert(String title, String header, String message) {
        String fullMessage = title + "\n" + header + "\n" + message;

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(fullMessage)
                .messageGroupId("Alerts")
                .build();

        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
    }


    public void receiveAndShowAlerts() {
        Thread alertThread = new Thread(() -> {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .waitTimeSeconds(20)
                    .visibilityTimeout(60)
                    .build();

            ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);

            for (Message message : receiveMessageResponse.messages()) {
                String[] parts = message.body().split("\n");

                if (parts.length >= 3) {
                    String title = parts[0];
                    String header = parts[1];
                    String content = parts[2];

                    Thread processingThread = new Thread(() -> {
                        storeAlert(title, header, content);
                        showAlert(title, header, content);
                    });

                    processingThread.start();

                } else {
                    System.out.println("Invalid message format: " + message.body());
                }

                deleteMessage(message);
            }
        });

        alertThread.start();
    }

    private void storeAlert(String title, String header, String body) {

        try {
            if (dynamoDbClient == null) {
                dynamoDbClient = AWSConfig.getDynamoDbClient();
            }

            String uniqueAlertID = UniqueIdGenerator.uniqueIdGenerator();

            String tableName = "Alerts";

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("AlertID", AttributeValue.builder().s(uniqueAlertID).build());
            item.put("Alert Title", AttributeValue.builder().s(title).build());
            item.put("Alert Header", AttributeValue.builder().s(header).build());
            item.put("Alert Message", AttributeValue.builder().s(body).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

        }catch (DynamoDbException e) {
            // Alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("DynamoDb Error");
            alert.setHeaderText(null);
            alert.setContentText("Please Contact Tech Support");
            alert.showAndWait();

            System.err.println("Error processing registration logic: " + e.getMessage());
        }

    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();

        sqsClient.deleteMessage(deleteMessageRequest);
    }

    private void showAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
