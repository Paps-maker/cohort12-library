package app.rest;

import app.ejbs.UserBean;
import app.model.User;
import app.security.JwtUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthApi {

    @Inject
    private UserBean userBean;

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        // Defensive Check: Structural payload validation
        if (credentials == null || !credentials.containsKey("username") || !credentials.containsKey("password")) {
            return errorResponse(Response.Status.BAD_REQUEST, "Malformed payload. Username and password keys are required.");
        }

        String username = credentials.get("username");
        String password = credentials.get("password");

        // Null safety parsing
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return errorResponse(Response.Status.BAD_REQUEST, "Credentials cannot contain blank or empty values.");
        }

        User user = userBean.getUserDetails(username.trim());
        if (user != null && password.equals(user.getPassword())) {
            String role = (user.getRole() != null) ? user.getRole().toUpperCase() : "MEMBER";

            // Issue a secure stateless signature token
            String token = JwtUtil.generateToken(username.trim(), role);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("username", username.trim());
            responseData.put("role", role);

            return Response.ok(responseData).build();
        }

        return errorResponse(Response.Status.UNAUTHORIZED, "Invalid structural database security credentials");
    }

    // Centralized helper for uniform error responses
    private Response errorResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(Map.of("error", message))
                .build();
    }
}