package app.soap;

import app.model.Book;
import app.ejbs.BookBean;
import jakarta.ejb.EJB;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import java.util.List;

@WebService(serviceName = "BookWebService", targetNamespace = "http://soap.app/")
public class BookSoapApi {

    @EJB
    private BookBean bookBean;

    //fetch all books
    @WebMethod(operationName = "getAllBooks")
    @WebResult(name = "bookItem")
    public List<Book> getAllBooks() {
        return bookBean.getAllBooks();
    }

    // add a book
    @WebMethod(operationName = "addNewBook")
    @WebResult(name = "statusMessage")
    public String addNewBook(
            @WebParam(name = "title") String title,
            @WebParam(name = "author") String author,
            @WebParam(name = "isbn") String isbn,
            @WebParam(name = "quantity") int quantity
    ) {
        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setIsbn(isbn);
        newBook.setQuantity(quantity);
        newBook.setAvailableCopies(quantity); // Set original capacity balance

        // Calls your exact bean method. Returns null on success, or the validation error string!
        String result = bookBean.addBook(newBook);

        if (result == null) {
            return "Success: Book record created successfully.";
        }
        return result;
    }
}