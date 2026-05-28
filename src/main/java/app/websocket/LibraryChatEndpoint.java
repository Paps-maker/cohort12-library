package app.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/chat")
public class LibraryChatEndpoint {

    private static final ConcurrentHashMap<String, String> sessionUsernames = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> clientSessions = new ConcurrentHashMap<>();
    private static final Set<Session> librarianSessions = Collections.synchronizedSet(new HashSet<>());

    // TRACKER: Keeps tabs on whether a client session has sent a message yet
    private static final ConcurrentHashMap<String, Boolean> firstMessageTracker = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        String queryString = session.getQueryString();
        boolean isLibrarian = queryString != null && queryString.contains("role=librarian");

        if (isLibrarian) {
            librarianSessions.add(session);
            sendInitialClientList(session);
        } else {
            String username = extractQueryParam(queryString, "username");
            if (username == null || username.trim().isEmpty()) {
                username = "Student_" + session.getId().substring(0, 4);
            }

            clientSessions.put(session.getId(), session);
            sessionUsernames.put(session.getId(), username);

            // Mark this session as eligible for an auto-welcome message upon their first text submission
            firstMessageTracker.put(session.getId(), true);

            System.out.println("CHAT ROUTE: Handshake opened for User [" + username + "] with Session ID [" + session.getId() + "]");
            notifyLibrariansUserChanged();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("ping".equals(message)) {
            try { session.getBasicRemote().sendText("pong"); } catch (IOException ignored) {}
            return;
        }

        boolean isLibrarian = librarianSessions.contains(session);

        if (isLibrarian) {
            String targetId = extractJsonValue(message, "targetSessionId");
            String text = extractJsonValue(message, "text");

            Session clientDest = clientSessions.get(targetId);
            if (clientDest != null && clientDest.isOpen()) {
                String outbound = String.format("{\"username\":\"Librarian\",\"sender\":\"Librarian\",\"text\":\"%s\"}", escapeJson(text));
                sendMessageTo(clientDest, outbound);
            }

            String syncPayload = String.format("{\"sender\":\"Librarian\",\"clientSessionId\":\"%s\",\"text\":\"%s\"}", targetId, escapeJson(text));
            broadcastToLibrarians(syncPayload);
        } else {
            // Message originates from a Student Client
            String text = extractJsonValue(message, "text");
            String clientName = sessionUsernames.getOrDefault(session.getId(), "Student");

            String outbound = String.format("{\"sender\":\"Client\",\"username\":\"%s\",\"clientSessionId\":\"%s\",\"text\":\"%s\"}",
                    clientName, session.getId(), escapeJson(text));

            // 1. Send the student's message out to the system normally
            broadcastToLibrarians(outbound);
            sendMessageTo(session, outbound);

            //  2. CHAT BOT TRIGGER: Fire the welcome message ONLY right after their first text arrives
            Boolean isFirstMessage = firstMessageTracker.get(session.getId());
            if (isFirstMessage != null && isFirstMessage) {
                // Instantly toggle flag off so it never sends again for this session
                firstMessageTracker.put(session.getId(), false);

                String welcomeMsg = "Hi, welcome to our Support Desk A librarian will connect with you shortly.";

                // Route auto-response back to student screen
                String studentWelcome = String.format("{\"username\":\"Librarian\",\"sender\":\"Librarian\",\"text\":\"%s\"}", welcomeMsg);
                sendMessageTo(session, studentWelcome);

                // Route auto-response to librarian screen queue matching the client's tracking ID
                String librarianWelcome = String.format("{\"sender\":\"Librarian\",\"clientSessionId\":\"%s\",\"text\":\"%s\"}", session.getId(), welcomeMsg);
                broadcastToLibrarians(librarianWelcome);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (librarianSessions.contains(session)) {
            librarianSessions.remove(session);
        } else {
            String username = sessionUsernames.remove(session.getId());
            clientSessions.remove(session.getId());
            firstMessageTracker.remove(session.getId()); // Clean up memory footprint

            System.out.println("CHAT ROUTE: Handshake closed for User [" + username + "]");
            notifyLibrariansUserChanged();
            broadcastToLibrarians(String.format("{\"system\":\"disconnect\",\"clientSessionId\":\"%s\"}", session.getId()));
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Chat WebSocket error on session " + session.getId() + ": " + throwable.getMessage());
    }

    private void broadcastToLibrarians(String rawJson) {
        synchronized (librarianSessions) {
            for (Session lib : librarianSessions) {
                if (lib.isOpen()) {
                    sendMessageTo(lib, rawJson);
                }
            }
        }
    }

    private void notifyLibrariansUserChanged() {
        StringBuilder json = new StringBuilder("{\"system\":\"userList\",\"users\":[");
        int count = 0;
        for (String id : clientSessions.keySet()) {
            String name = sessionUsernames.getOrDefault(id, "Student");
            if (count > 0) json.append(",");
            json.append(String.format("{\"id\":\"%s\",\"username\":\"%s\"}", id, escapeJson(name)));
            count++;
        }
        json.append("]}");
        broadcastToLibrarians(json.toString());
    }

    private void sendInitialClientList(Session librarianSession) {
        StringBuilder json = new StringBuilder("{\"system\":\"userList\",\"users\":[");
        int count = 0;
        for (String id : clientSessions.keySet()) {
            String name = sessionUsernames.getOrDefault(id, "Student");
            if (count > 0) json.append(",");
            json.append(String.format("{\"id\":\"%s\",\"username\":\"%s\"}", id, escapeJson(name)));
            count++;
        }
        json.append("]}");
        sendMessageTo(librarianSession, json.toString());
    }

    private void sendMessageTo(Session target, String rawJson) {
        try {
            target.getBasicRemote().sendText(rawJson);
        } catch (IOException e) {
            System.err.println("Failed routing message block to session: " + target.getId());
        }
    }

    private String extractJsonValue(String json, String key) {
        if (json == null || !json.contains(key)) return "";
        try {
            String match = "\"" + key + "\"";
            int keyIndex = json.indexOf(match);
            if (keyIndex == -1) return "";

            int colonIndex = json.indexOf(":", keyIndex + match.length());
            int openQuote = json.indexOf("\"", colonIndex);
            int closeQuote = json.indexOf("\"", openQuote + 1);

            if (openQuote != -1 && closeQuote != -1 && openQuote < closeQuote) {
                return json.substring(openQuote + 1, closeQuote);
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private String extractQueryParam(String query, String paramName) {
        if (query == null || paramName == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equalsIgnoreCase(paramName)) {
                return kv[1];
            }
        }
        return null;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}