/*
 * Katelyn Rohrer, Lydia Dufek, Camila Grubb
 * CSC 483/583
 * Defines the WikiFile class, which manages a single file containing several
 * wikipedia pages within it.
 */

package model;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The WikiFile class is used to manage wikipedia files containing several
 * wiki pages and creates the corresponding Page objects for each page
 * parsed out.
 */
public class WikiFile {
    private final ArrayList<Page> pages;

    /**
     * Creates the WikiFile object and parses through each of the pages using
     * regex, adding each of the pages to the pages attribute as they're read in.
     * @param contents The contents of the entire file
     */
    public WikiFile(String contents) {
        // Using regex to find the page sections. This also makes it
        // lazy evaluation, so it should be a bit more memory efficient.
        String regex =  "(?<=^|\\r?\\n)" +                               // Matches start of file or newline (Windows or Unix)
                        "\\[\\[(?!File:)(?!Image:)(.*?)]]\\r?\\n" +      // Matches the title, ignoring File: and Image:, with potential Windows newline
                        "(.*?)" +                                        // Matches all text in the middle
                        "(?=\\[\\[(?!File:)(?!Image:)(.*?)]]\\r?\\n|$)"; // Matches until the next title or EOF, considering Windows newline

        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents);


        pages = new ArrayList<>();

        // until there are no more matches, create the correlating page type and add it to pages
        while (matcher.find()) {
            String title = matcher.group(1);
            String pageText = matcher.group(0);
            Page p = createCorrectPageType(title, pageText);
            pages.add(p);
        }
    }

    /**
     * Sorts a page as a redirect page, unclear page, or normal page
     * based on the contents of the document. The corresponding Page subclass
     * is returned.
     * @param title The title of the wikipedia page
     * @param contents All of the contents of the wikipedia page
     * @return Either an UnclearPage, RedirectPage, or NormalPage object,
     * determined by which type of page the string represents.
     */
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

    /**
     * Getter for pages
     * @return Arraylist of Page objects for the pages within the file
     */
    public ArrayList<Page> getPages() {
        return pages;
    }

    /**
     * Clears out all of the contents for all of the pages within this file.
     */
    public void clearAllContents() {
        for (Page p: pages) {
            p.clearContents();
        }
    }

}
