package app.rest;

import app.ejbs.BookBean;
import app.model.Book;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context; //
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;

@Path("/books")
public class BookApi extends GenericApi<Book> {

    @Inject
    private BookBean bookBean;

    // BINDING CORE ABSTRACT METHODS TO BOOKBEAN WORKFLOWS
    @Override
    protected List<Book> getAllEntities() {
        return bookBean.getAllBooks();
    }

    @Override
    protected Book getEntityById(int id) {
        return bookBean.getBookById(id);
    }

    @Override
    protected boolean createEntity(Book newBook) {
        if (newBook == null || newBook.getTitle() == null) {
            return false;
        }
        return bookBean.addBook(newBook) == null;
    }

    @Override
    protected boolean deleteEntity(int id) {
        return false;
    }

    @Override
    @GET
    @Consumes(MediaType.WILDCARD)
    public Response fetchAll(@Context SecurityContext sec) {
        return Response.ok(getAllEntities()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBookRecord(@PathParam("id") int id, @Context SecurityContext sec) {
        if (sec == null || sec.getUserPrincipal() == null) {
            return errorResponse(Response.Status.UNAUTHORIZED, "Authentication required to delete records.");
        }

        String executionRole = sec.isUserInRole("ADMIN") ? "ADMIN" : "MEMBER";

        try {
            bookBean.deleteBook(id, executionRole);
            return Response.ok(Map.of("message", "Book successfully deleted.")).build();
        } catch (SecurityException e) {
            return errorResponse(Response.Status.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorResponse(Response.Status.NOT_FOUND, e.getMessage());
        }
    }
}