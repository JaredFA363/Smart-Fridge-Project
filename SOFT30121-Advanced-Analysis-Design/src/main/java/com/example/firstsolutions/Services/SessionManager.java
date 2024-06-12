package com.example.firstsolutions.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static final long SESSION_TIMEOUT_MS = 15 * 60 * 1000; // 15 minutes
    private static final Map<String, SessionInfo> sessions = new HashMap<>();

    public static void createSession(String username, String role, boolean deleteAccess, boolean writeAccess, boolean accessToFridge) {
        String sessionId = generateUniqueSessionId();
        SessionInfo sessionInfo = new SessionInfo(username, role, deleteAccess, writeAccess, accessToFridge, System.currentTimeMillis());
        sessions.put(sessionId, sessionInfo);
    }

    public static String getUsername() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
                return sessionInfo.getUsername();
            }
        }
        return null;
    }

    public static String getRole() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
                return sessionInfo.getRole();
            }
        }
        return null;
    }

    public static void setAccessToFridge(String sessionId, boolean accessToFridge) {
        SessionInfo sessionInfo = sessions.get(sessionId);
        if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
            sessionInfo.setAccessToFridge(accessToFridge);
        }
    }

    public static boolean isCurrentSessionExpired() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            return sessionInfo == null || isSessionExpired(sessionInfo);
        }
        return true;
    }

    public static boolean hasDeleteAccess() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
                return sessionInfo.hasdeleteAccess();
            }
        }
        return false;
    }

    public static boolean hasWriteAccess() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
                return sessionInfo.hasWriteAccess();
            }
        }
        return false;
    }

    public static boolean hasAccessToFridge() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null && !isSessionExpired(sessionInfo)) {
                return sessionInfo.hasAccessToFridge();
            }
        }
        return false;
    }

    public static void logoutUser() {
        String currentSessionId = getCurrentSessionId();
        if (currentSessionId != null) {
            SessionInfo sessionInfo = sessions.get(currentSessionId);
            if (sessionInfo != null) {
                sessionInfo.setCreationTime(0);
            }
        }
    }

    public static String getCurrentSessionId() {
        return sessions.keySet().iterator().next();
    }

    private static String generateUniqueSessionId() {
        return UUID.randomUUID().toString();
    }

    private static boolean isSessionExpired(SessionInfo sessionInfo) {
        long currentTime = System.currentTimeMillis();
        long sessionCreationTime = sessionInfo.getCreationTime();
        return sessionCreationTime == 0 || (currentTime - sessionCreationTime) > SESSION_TIMEOUT_MS;
    }

    public static class SessionInfo {
        private final String username;
        private final String role;
        private final boolean deleteAccess;
        private final boolean writeAccess;
        private boolean accessToFridge;
        private long creationTime;

        public SessionInfo(String username, String role, boolean deleteAccess, boolean writeAccess, boolean accessToFridge, long creationTime) {
            this.username = username;
            this.role = role;
            this.deleteAccess = deleteAccess;
            this.writeAccess = writeAccess;
            this.accessToFridge = accessToFridge;
            this.creationTime = creationTime;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public boolean hasdeleteAccess() {
            return deleteAccess;
        }

        public boolean hasWriteAccess() {
            return writeAccess;
        }

        public boolean hasAccessToFridge() {
            return accessToFridge;
        }

        public void setAccessToFridge(boolean accessToFridge) {
            this.accessToFridge = accessToFridge;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }


    }
}
