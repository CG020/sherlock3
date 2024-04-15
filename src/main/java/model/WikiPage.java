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
            String title = matcher.group(1);
            String pageText = matcher.group(0);
            Page p = createCorrectPageType(title, pageText);
            pages.add(p);
            return;
        }
    }


    private Page createCorrectPageType(String title, String contents) {
        String unclearSearchPhrase = title + " may refer to:";
        String redirectSearchPhrase = "#REDIRECT";

        if (contents.contains(unclearSearchPhrase)) {
            return new UnclearPage(contents);
        } else if (contents.contains(redirectSearchPhrase)) {
            return new RedirectPage(contents);
        } else {
            return new NormalPage(contents);
        }
    }


    public ArrayList<Page> getPages() {
        return pages;
    }


}
