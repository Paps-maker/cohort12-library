package app.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class GenericApi<T> {

    // Helper to enforce security constraints uniformly
    protected boolean isAdmin(SecurityContext sec) {
        return sec != null && sec.isUserInRole("ADMIN");
    }

    // Helper to generate consistent error responses
    protected Response errorResponse(Response.Status status, String message) {
        return Response.status(status)
                .entity("{\"error\":\"" + message + "\"}")
                .build();
    }

    // Abstract hooks for concrete resource implementations
    protected abstract List<T> getAllEntities();
    protected abstract T getEntityById(int id);
    protected abstract boolean createEntity(T entity);
    protected abstract boolean deleteEntity(int id);

    // GENERIC CRUD ENDPOINTS

    @GET
    @Consumes(MediaType.WILDCARD)
    public Response fetchAll(@Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Access Denied: Administrative authority required.");
        }
        return Response.ok(getAllEntities()).build();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.WILDCARD)
    public Response fetchById(@PathParam("id") int id, @Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Access Denied: Administrative authority required.");
        }
        T entity = getEntityById(id);
        if (entity == null) {
            return errorResponse(Response.Status.NOT_FOUND, "Record not found.");
        }
        return Response.ok(entity).build();
    }

    @POST
    public Response create(T entity, @Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Access Denied: Administrative authority required.");
        }
        if (entity == null) {
            return errorResponse(Response.Status.BAD_REQUEST, "Payload cannot be empty.");
        }

        return createEntity(entity)
                ? Response.status(Response.Status.CREATED).entity("{\"message\":\"Record created successfully.\"}").build()
                : errorResponse(Response.Status.BAD_REQUEST, "Failed to persist record.");
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") int id, @Context SecurityContext sec) {
        if (!isAdmin(sec)) {
            return errorResponse(Response.Status.FORBIDDEN, "Access Denied: Administrative authority required.");
        }

        return deleteEntity(id)
                ? Response.noContent().build()
                : errorResponse(Response.Status.NOT_FOUND, "Record not found or could not be removed.");
    }
}