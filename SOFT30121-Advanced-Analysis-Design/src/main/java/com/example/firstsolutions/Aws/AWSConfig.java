package com.example.firstsolutions.Aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;


public class AWSConfig {

    private static String accessKey;
    private static String secretKey;
    private static String region;

    public AWSConfig() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("aws-config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.out.println("Sorry, unable to find aws-config.properties");
                return;
            }
            prop.load(input);
            accessKey = prop.getProperty("aws.accessKey");
            secretKey = prop.getProperty("aws.secretKey");
            region = prop.getProperty("aws.region");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static DynamoDbClient getDynamoDbClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    public static SqsClient getSqsClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return SqsClient.builder()
                .region(Region.of(region)) // Use SQS region
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }


}

