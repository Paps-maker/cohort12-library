package app.rest;

import app.ejbs.UserBean;
import app.model.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("/users")
public class UserApi extends GenericApi<User> {

    @Inject
    private UserBean userBean;

    // BINDING CORE ABSTRACT METHODS TO USERBEAN WORKFLOWS
    @Override
    protected List<User> getAllEntities() {
        return userBean.getAllUsers();
    }

    @Override
    protected User getEntityById(int id) {
        return userBean.getUserById(id);
    }

    @Override
    protected boolean createEntity(User newUser) {
        if (newUser == null || newUser.getUsername() == null || newUser.getEmail() == null || newUser.getPassword() == null) {
            return false;
        }
        return "success".equals(userBean.registerUser(newUser.getUsername(), newUser.getEmail(), newUser.getPassword()));
    }

    @Override
    protected boolean deleteEntity(int id) {
        return userBean.deleteUser(id);
    }

    // OVERRIDE FETCHALL WITH SECURITY
    @Override
    @GET
    @Consumes(MediaType.WILDCARD)
    public Response fetchAll(@Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Access Denied: Administrative authority required.");
        }
        return Response.ok(getAllEntities()).build();
    }

    // CUSTOM PATH: STUDENT LOOKUP
    @GET
    @Path("/verify")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupStudent(@QueryParam("target") String target, @Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Unauthorized workspace access. ADMIN role required.");
        }

        if (target == null || target.trim().isEmpty()) {
            return Response.ok(Map.of("found", false, "username", "")).build();
        }

        User existingUser = userBean.getUserDetails(target.trim());
        return Response.ok(Map.of("found", existingUser != null, "username", target.trim())).build();
    }
}