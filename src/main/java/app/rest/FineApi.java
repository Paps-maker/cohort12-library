package app.rest;

import app.ejbs.FineBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("/fines")
public class FineApi extends GenericApi<String> {

    @Inject
    private FineBean fineBean;

    // OVERRIDE FETCHALL FOR CONTEXT-AWARE SECURITY MAPPINGS
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAll(@Context SecurityContext sec) {
        if (sec == null || sec.getUserPrincipal() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Token missing or expired. Please log in first.");
        }

        String username = sec.getUserPrincipal().getName();
        List<String> history = sec.isUserInRole("ADMIN")
                ? fineBean.getAdminFineHistory()
                : fineBean.getMemberFineHistory(username);

        return Response.ok(history).build();
    }

    // CUSTOM PATHS: PROCESS FINE PAYMENT
    @POST
    @Path("/pay")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePayment(@FormParam("fineId") String fineId, @Context SecurityContext sec) {
        if (sec == null || sec.getUserPrincipal() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Token validation failed.");
        }

        boolean success = fineBean.processFinePayment(sec.getUserPrincipal().getName(), fineId);

        if (success) {
            return Response.ok(Map.of("message", "Payment successfully processed.")).build();
        } else {
            return errorResponse(Response.Status.BAD_REQUEST, "Payment failed. Verify fine allocation state.");
        }
    }

    // BINDING REQUIRED CORE GENERIC HOOKS
    @Override
    protected List<String> getAllEntities() {
        return fineBean.getAdminFineHistory();
    }

    @Override
    protected String getEntityById(int id) {
        throw new UnsupportedOperationException("Fine tracking by localized ID is managed via custom ledger endpoints.");
    }

    @Override
    protected boolean createEntity(String entity) {
        throw new UnsupportedOperationException("Fines are calculated automatically by automated system processes.");
    }

    @Override
    protected boolean deleteEntity(int id) {
        throw new UnsupportedOperationException("Fines can only be cleared via the payment settlement portal.");
    }
}