package app.pages;

import java.util.Arrays;
import java.util.List;

public class About {

    public static String getDescription() {
        return "Our Digital Library  allows registered users to borrow books easily.If not Registered Visit our library to get registered";
    }

    //  THIS
    public static List<String> getSteps() {
        return Arrays.asList(
                "Login using your username and password",
                "Go to the Books section",
                "Select a book to borrow",
                "Submit borrow request",
                "View borrowed books list"
        );
    }
}