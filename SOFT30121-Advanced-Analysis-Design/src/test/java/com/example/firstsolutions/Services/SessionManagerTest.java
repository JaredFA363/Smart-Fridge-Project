package com.example.firstsolutions.Services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @Test
    void testCreateSession() {
        SessionManager.createSession("test", "admin", true, true, true);
        String username = SessionManager.getUsername();
        assertNotNull(username);
        assertEquals("test", username);

        String role = SessionManager.getRole();
        assertNotNull(role);
        assertEquals("admin", role);

        assertTrue(SessionManager.hasDeleteAccess());
        assertTrue(SessionManager.hasWriteAccess());
        assertTrue(SessionManager.hasAccessToFridge());
    }

    @Test
    void testSetAccessToFridge() {
        SessionManager.createSession("test", "admin", true, true, true);
        String sessionId = SessionManager.getCurrentSessionId();

        assertTrue(SessionManager.hasAccessToFridge());

        SessionManager.setAccessToFridge(sessionId, false);

        assertFalse(SessionManager.hasAccessToFridge());
    }
    @Test
    void testSessionExpiration() {
        SessionManager.createSession("test", "admin", false, true, true);
        String sessionId = SessionManager.getCurrentSessionId();

        assertFalse(SessionManager.isCurrentSessionExpired());

        // Logout the user, which marks the session as expired
        SessionManager.logoutUser();

        assertTrue(SessionManager.isCurrentSessionExpired());
    }




}
