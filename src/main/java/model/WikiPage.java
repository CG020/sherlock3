package model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiPage {
    private final ArrayList<Page> pages;

    public WikiPage(String contents) {
        // Using regex to find the page sections. This also makes it
        // lazy evaluation, so it should be a bit more memory efficient.
        String regex =  "\\[\\[(.*?)]]\n" +       // match the title
                        "(.*?)" +                 // and all text in the middle
                        "(?=\\[\\[(.*?)]]\n|$)";  // until the next title match (or EOF)
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents);


        pages = new ArrayList<>();

        while (matcher.find()) {
            String pageText = matcher.group(0);
            Page p = new Page(pageText);
            pages.add(p);
            return;
        }
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

}
