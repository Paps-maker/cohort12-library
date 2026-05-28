package app.rest;

import app.ejbs.BorrowingBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("/loans")
public class LoanApi extends GenericApi<String> {

    @Inject
    private BorrowingBean borrowingBean;

    // OVERRIDE FETCHALL FOR ROLE-BASED SEGREGATION
    @Override
    @GET
    @Produces(MediaType.APPLICATION_JSON)  //@context for the current security state of the person making the request. admin or user
    public Response fetchAll(@Context SecurityContext sec) {
        if (sec == null || sec.getUserPrincipal() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Token missing or expired. Please log in first.");
        }

        String username = sec.getUserPrincipal().getName();
        List<String> borrowedList = sec.isUserInRole("ADMIN")
                ? borrowingBean.getAdminBorrowedRecords()
                : borrowingBean.getMemberActiveLoans(username);

        return Response.ok(borrowedList).build();
    }

    // CUSTOM PATH: PROCESS A BOOK RETURN (Admin Only)
    @POST
    @Path("/return")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnBook(@FormParam("borrowId") String borrowId, @Context SecurityContext sec) {
        if (sec == null || !sec.isUserInRole("ADMIN")) {
            return errorResponse(Response.Status.FORBIDDEN, "Librarian credentials required to process physical returns.");
        }

        String processedId = (borrowId != null) ? borrowId.replaceAll("[^0-9]", "").trim() : "";
        String result = borrowingBean.processReturnRequest("ADMIN", processedId);

        if (result.contains("Success") || result.contains("returned")) {
            return Response.ok(Map.of("success", true, "message", result)).build();
        } else {
            return errorResponse(Response.Status.BAD_REQUEST, result);
        }
    }

    // CUSTOM PATH: CREATE/CHECKOUT A LOAN
    @POST
    @Path("/borrow")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkoutBook(
            @FormParam("bookId") String bookId,
            @FormParam("days") String days,
            @FormParam("targetStudent") String targetStudent,
            @Context SecurityContext sec) {

        if (sec == null || sec.getUserPrincipal() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Access denied. Missing execution credentials.");
        }

        String targetBorrower = sec.isUserInRole("ADMIN") ? targetStudent : sec.getUserPrincipal().getName();

        if (targetBorrower == null || targetBorrower.trim().isEmpty()) {
            return errorResponse(Response.Status.BAD_REQUEST, "A valid borrower identification context must be present.");
        }

        String outcome = borrowingBean.attemptBorrow(targetBorrower, bookId, days);

        if (outcome.contains("Success")) {
            return Response.status(Response.Status.CREATED).entity(Map.of("success", true, "message", outcome)).build();
        } else {
            return errorResponse(Response.Status.BAD_REQUEST, outcome);
        }
    }

    // BINDING REQUIRED CORE GENERIC HOOKS
    @Override protected List<String> getAllEntities() { return borrowingBean.getAdminBorrowedRecords(); }
    @Override protected String getEntityById(int id) { throw new UnsupportedOperationException(); }
    @Override protected boolean createEntity(String e) { throw new UnsupportedOperationException(); }
    @Override protected boolean deleteEntity(int id) { throw new UnsupportedOperationException(); }
}