package app.pages;


import java.util.Arrays;
import java.util.List;

public class Books {

    public static List<String> getCategories() {
        return Arrays.asList(
                "Fiction",
                "Technology",
                "Education",
                "Kids",
                "Story",
                "History"

        );
    }
}