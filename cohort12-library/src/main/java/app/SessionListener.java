package app;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionListener implements HttpSessionListener {

    private static int activeUsers = 0;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeUsers++;
        System.out.println("Session created! Active users: " + activeUsers);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeUsers--;
        System.out.println("Session destroyed! Active users: " + activeUsers);
    }

    //  METHOD TO ACCESS COUNT
    public static int getActiveUsers() {
        return activeUsers;
    }
}