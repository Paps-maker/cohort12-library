package app.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/activity")
public class SystemActivityServer {

    // Thread-safe set tracking all active connected admin dashboard browser sessions
    private static final Set<Session> clients = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("WEBSOCKET: Admin dashboard connected. Session ID: " + session.getId());

        // Send a warm welcome message directly to the newly connected user
        try {
            session.getBasicRemote().sendText("SYSTEM: Real-time monitoring connection established successfully.");
        } catch (IOException e) {
            System.err.println("WEBSOCKET ERROR: Failed to send init handshake: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        System.out.println("WEBSOCKET: Admin dashboard disconnected. Session ID: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        clients.remove(session);
        System.err.println("WEBSOCKET ERROR on session " + (session != null ? session.getId() : "unknown") + ": " + throwable.getMessage());
    }

    /**
     * STATIC BROADCAST METHOD:
     * Call this method from ANY EJB, Filter, Servlet, or Bean in your application 
     * to instantly push an activity log notice down to all connected open browsers.
     */
    public static void broadcastActivity(String logMessage) {
        System.out.println("WEBSOCKET BROADCAST: " + logMessage);
        for (Session client : clients) {
            if (client.isOpen()) {
                // Using asynchronous transmission so backend threads don't stall waiting for slow browser clients
                client.getAsyncRemote().sendText(logMessage);
            } else {
                // 🌟 ADDED: Active Self-Healing Clean. Remove dead references dynamically to protect heap allocation memory
                clients.remove(client);
            }
        }
    }
}