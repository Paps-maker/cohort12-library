package app.framework;

import java.util.HashMap;
import java.util.Map;


 // ModelAndView acts as a "Data Envelope."
 //It holds both the name of the View (the destination) and the Model (the data to show).

public class ModelAndView {
    // The name of the JSP file or the redirect URL
    private final String viewName;

    // A key-value store containing the data to be passed to the JSP
    private final Map<String, Object> model = new HashMap<>();

    // Constructor: Sets the destination view (e.g., "books_dashboard" or "redirect:/login")
    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }


      //Adds an object to the model.
      //Uses method chaining (returns 'this') for easy syntax:
     //new ModelAndView("view").addObject("key", val).addObject("key2", val2);

    public ModelAndView addObject(String attributeName, Object attributeValue) {
        this.model.put(attributeName, attributeValue);
        return this;
    }


     // Checks if the viewName is a redirect.
     //Used by the Dispatcher to decide between forward() and sendRedirect().

    public boolean isRedirect() {
        return viewName != null && viewName.startsWith("redirect:");
    }


     // Extracts the URL from the "redirect:..." string.
     // Example: "redirect:/login" becomes "/login"

    public String getRedirectUrl() {
        if (!isRedirect()) return viewName;
        return viewName.substring("redirect:".length());
    }

    // Getter for the view destination name
    public String getViewName() {
        return viewName;
    }

    // Getter for the map of data; used by the Dispatcher to call setAttribute()
    public Map<String, Object> getModel() {
        return model;
    }
}