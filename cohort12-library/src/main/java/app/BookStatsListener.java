package app;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.ArrayList;

@WebListener
public class BookStatsListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext context = sce.getServletContext();

        // TOTAL BOOKS (from BookStore)
        context.setAttribute("totalBooks", BookStore.books.size());

        //  BORROWED LIST
        context.setAttribute("borrowedBooks", new ArrayList<String>());

        System.out.println("Book system initialized!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Book system stopped!");
    }
}