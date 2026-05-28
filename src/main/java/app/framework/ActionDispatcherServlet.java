package app.framework;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;


@WebServlet(urlPatterns = "/", loadOnStartup = 1) // initialization immidietly the server starts
public class ActionDispatcherServlet extends HttpServlet {

    private final ActionMap actionMap = new ActionMap();

    // =========================================================================
    // CDI CONTROLLER DISCOVERY
    // =========================================================================
    @Inject
    @Controller
    private Instance<Object> discoveredControllers;

    // =========================================================================
    // VIEW RESOLVER CONFIGURATION (UPDATED FOR WEBAPP/VIEWS/)
    // =========================================================================
    private static final String VIEW_PREFIX = "/views/";
    private static final String VIEW_SUFFIX = ".jsp";

    @Override
    public void init() throws ServletException {

        System.out.println("=========================================================");
        System.out.println("FRAMEWORK STARTUP: Initializing Controller Mappings...");
        System.out.println("=========================================================");

        int counter = 0;

        // =========================================================================
        // ENGINE A: CDI AUTO DISCOVERY scanns for @controller in every class
        // =========================================================================
        if (discoveredControllers != null && !discoveredControllers.isUnsatisfied()) {

            for (Object controller : discoveredControllers) {

                actionMap.register(controller);

                counter++;

                System.out.println(
                        "CDI DISCOVERY: Registered -> "
                                + controller.getClass().getSimpleName()
                );
            }
        }

        // =========================================================================
        // ENGINE B: FALLBACK CLASS SCANNER
        // =========================================================================
        if (counter == 0) {

            System.out.println(
                    "FRAMEWORK WARNING: CDI discovery empty. Activating fallback scanner..."
            );

            ActionRegistry.autoRegister(actionMap, "app.controller");

        } else {

            System.out.println("=========================================================");
            System.out.println("FRAMEWORK READY: Indexed " + counter + " controllers.");
            System.out.println("=========================================================");
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //needed to find the right controller method. Everything else follows from them.
        String path = req.getServletPath();
        String verb = req.getMethod();

        // =========================================================================
        // SAFETY NORMALIZATION
        // =========================================================================
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        // Remove query parameters safely
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        System.out.println(
                "DISPATCHER: Intercepted [" + verb + "] -> " + path
        );

        // =========================================================================
        // ACTION LOOKUP
        // =========================================================================
        ActionMapMatch match = actionMap.find(verb, path);

        if (match == null) {

            System.out.println(
                    "DISPATCHER WARNING: No controller mapping found for path -> "
                            + path
            );

            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No route found for: " + path);

            return;
        }

        try {

            Method method = match.getMethod();
            Object controllerInstance = match.getAction();

            Object result;

            // =========================================================================
            // METHOD INVOCATION ENGINE means controller methods don't have to accept parameters they don't need.
            // =========================================================================
            if (method.getParameterCount() == 2
                    && method.getParameterTypes()[0].equals(HttpServletRequest.class)
                    && method.getParameterTypes()[1].equals(HttpServletResponse.class)) {

                result = method.invoke(controllerInstance, req, resp);

            } else if (method.getParameterCount() == 1
                    && method.getParameterTypes()[0].equals(HttpServletRequest.class)) {

                result = method.invoke(controllerInstance, req);

            } else {

                result = method.invoke(controllerInstance);
            }

            // =========================================================================
            // RESPONSE RENDERING ENGINE modelview
            // =========================================================================

            // -------------------------------------------------------------------------
            // MODEL AND VIEW SUPPORT
            // -------------------------------------------------------------------------
            // main path — your controllers use this
            if (result instanceof ModelAndView) {

                ModelAndView mv = (ModelAndView) result;

                // ---------------------------------------------------------------------
                // REDIRECT HANDLING // "redirect:/login" → strips prefix → "/login"
                // ---------------------------------------------------------------------
                if (mv.isRedirect()) {

                    String redirectTarget = mv.getRedirectUrl();

                    System.out.println(
                            "DISPATCHER: Redirecting browser -> "
                                    + redirectTarget
                    );

                    resp.sendRedirect(req.getContextPath() + redirectTarget);

                    return;
                }

                // ---------------------------------------------------------------------
                // VIEW RESOLUTION
                // ---------------------------------------------------------------------
                String viewPath = resolveView(mv.getViewName());

                System.out.println(
                        "DISPATCHER: Forwarding to resolved JSP -> "
                                + viewPath
                );

                // Push model attributes into request scope ${username} requser= (username)
                mv.getModel().forEach(req::setAttribute);

                req.getRequestDispatcher(viewPath).forward(req, resp);

                return;
            }

            // -------------------------------------------------------------------------
            // STRING RESPONSE SUPPORT
            // -------------------------------------------------------------------------
            if (result instanceof String) {

                String stringResult = ((String) result).trim();

                // ---------------------------------------------------------------------
                // STRING REDIRECT SUPPORT
                // ---------------------------------------------------------------------
                if (stringResult.startsWith("redirect:")) {

                    String redirectTarget =
                            stringResult.substring("redirect:".length()).trim();

                    System.out.println(
                            "DISPATCHER: String redirect -> "
                                    + redirectTarget
                    );

                    resp.sendRedirect(req.getContextPath() + redirectTarget);

                    return;
                }

                // ---------------------------------------------------------------------
                // RAW JSP PATH make sure even if you type something wrong its possibel to be completed automatically
                // ---------------------------------------------------------------------
                if (stringResult.startsWith("/WEB-INF/")
                        || stringResult.startsWith("/views/")
                        || stringResult.endsWith(".jsp")) {

                    System.out.println(
                            "DISPATCHER: Direct JSP forwarding -> "
                                    + stringResult
                    );

                    req.getRequestDispatcher(stringResult)
                            .forward(req, resp);

                    return;
                }

                // ---------------------------------------------------------------------
                // AUTO VIEW RESOLUTION SUPPORT work of resolveview  // "books_dashboard" → "/views/books_dashboard.jsp
                // ---------------------------------------------------------------------
                if (!stringResult.contains(" ")
                        && !stringResult.contains("{")
                        && !stringResult.contains("[")) {

                    String resolvedView = resolveView(stringResult);

                    System.out.println(
                            "DISPATCHER: Auto-resolved String view -> "
                                    + resolvedView
                    );

                    req.getRequestDispatcher(resolvedView)
                            .forward(req, resp);

                    return;
                }

                // ---------------------------------------------------------------------
                // RAW TEXT / JSON RESPONSE
                // ---------------------------------------------------------------------
                resp.setContentType("text/plain");

                resp.getWriter().write(stringResult);

                return;
            }

            // =========================================================================
            // NULL RESPONSE SAFETY
            // =========================================================================
            if (result == null) {

                System.out.println(
                        "DISPATCHER WARNING: Controller returned null."
                );

                return;
            }


            // UNKNOWN RESPONSE TYPE

            resp.setContentType("text/plain");

            resp.getWriter().write(result.toString());

        } catch (Exception e) {

            e.printStackTrace();

            Throwable cause = e.getCause() != null
                    ? e.getCause()
                    : e;

            resp.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Framework execution error: " + cause.getMessage()
            );
        }
    }

    // INTERNAL VIEW RESOLVER (UPDATED FOR WEBAPP/VIEWS/)

    private String resolveView(String viewName) {

        if (viewName == null || viewName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "View name cannot be null or empty."
            );
        }

        viewName = viewName.trim();

        // Already fully qualified JSP path
        if (viewName.startsWith("/WEB-INF/")
                || viewName.startsWith("/views/")
                || viewName.endsWith(".jsp")) {

            return viewName;
        }

        // Normalize accidental leading slash
        if (viewName.startsWith("/")) {
            viewName = viewName.substring(1);
        }

        return VIEW_PREFIX + viewName + VIEW_SUFFIX;
    }
}