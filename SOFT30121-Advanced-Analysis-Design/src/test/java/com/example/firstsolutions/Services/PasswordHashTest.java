package com.example.firstsolutions.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

public class PasswordHashTest {

    @Test
    public void testHashPassword() {
        // Arrange
        String password = "password123";

        // Act
        String hashedPassword1 = PasswordHash.hashPassword(password);
        String hashedPassword2 = PasswordHash.hashPassword(password);

        // Assert
        assertEquals(hashedPassword1, hashedPassword2);
    }
}
