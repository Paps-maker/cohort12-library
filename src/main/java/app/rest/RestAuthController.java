package app.rest; // Recommendation: Move to app.rest for consistency

import app.dao.UserDAO;
import app.model.User;
import app.security.JwtUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/auth") // Updated to match your path request
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RestAuthController {

    @Inject
    private UserDAO userDAO;

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = userDAO.findUser(username, password);
        
        if (user != null) {
            String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
            return Response.ok(Map.of("token", token)).build();
        }
        
        return Response.status(Response.Status.UNAUTHORIZED)
                       .entity(Map.of("error", "Invalid credentials"))
                       .build();
    }
}