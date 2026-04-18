package app;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class RequestListener implements ServletRequestListener {

    private static int totalRequests = 0;

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        totalRequests++;

        System.out.println("Request received! Total requests: " + totalRequests);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("Request completed!");
    }

    //  ACCESS METHOD
    public static int getTotalRequests() {
        return totalRequests;
    }
}