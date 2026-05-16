package app.framework;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

// Importing the new controllers
import app.controller.UserController;
import app.controller.BookController;
import app.controller.AuthController;
import app.controller.FineController;
import app.controller.AccountController;
import app.controller.LibraryOperationController;

@WebServlet(urlPatterns = "/", loadOnStartup = 1)
public class ActionDispatcherServlet extends HttpServlet {

    private ActionMap actionMap = new ActionMap();

    // ==========================================
    // INJECTED CONTROLLER INSTANCES
    // ==========================================
    @Inject
    private UserController userController;

    @Inject
    private BookController bookController;

    @Inject
    private AuthController authController;

    @Inject
    private FineController fineController;

    @Inject
    private AccountController accountController;

    @Inject
    private LibraryOperationController libraryOperationController;

    @Override
    public void init() throws ServletException {
        // Registering the full suite of controllers into the ActionMap
        actionMap.register(userController);
        actionMap.register(bookController);
        actionMap.register(authController);
        actionMap.register(fineController);
        actionMap.register(accountController);          // Replaces RegisterServlet & ContactServlet
        actionMap.register(libraryOperationController); // Replaces borrow, BorrowedBooks, & ReturnServlet

        System.out.println("FRAMEWORK: All Controllers Registered Successfully.");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();
        String verb = req.getMethod();

        // Debugging logs for your HP ProBook terminal
        System.out.println("DISPATCHER DEBUG: [" + verb + "] requested for path: " + path);

        ActionMapMatch match = actionMap.find(verb, path);

        if (match != null) {
            try {
                match.getMethod().invoke(match.getAction(), req, resp);
            } catch (Exception e) {
                e.printStackTrace();
                // Providing the specific cause helps with rapid debugging during development
                resp.sendError(500, "Framework execution error: " + (e.getCause() != null ? e.getCause() : e.getMessage()));
            }
        } else {
            // If no controller matches, fall back to default servlet (serving JSPs/Static files)
            req.getServletContext().getNamedDispatcher("default").forward(req, resp);
        }
    }
}