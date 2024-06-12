package com.example.firstsolutions.Services;

import java.util.UUID;

public class UniqueIdGenerator {

    public static String uniqueIdGenerator() {

        return UUID.randomUUID().toString();
    }
}
