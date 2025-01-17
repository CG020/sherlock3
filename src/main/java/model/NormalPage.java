/**
 * Katelyn Rohrer, Camila Grubb, Lydia Dufek
 * CSC 483/583
 * This file defines the NormalPage class, which is a subclass of the Page.
 */
package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The most important type of Page. This page is the only type that ends up
 * being added to the index, after redirects from the RedirectPage are linked.
 * Defines several methods for string/page parsing.
 */
public class NormalPage extends Page{
    static final List<String> generalHeaders = Arrays.asList(
            "Images", "See also", "References",
            "Bibliography", "Distinctions", "Further reading",
            "Notes", "External links", "Sources"
    );
    static final List<String> headersToIgnore = Arrays.asList(
            "Images", "External links", "Sources"
    );

    ArrayList<String> categories;
    ArrayList<String> headers;
    String summary;
    StringBuilder bodyText;
    ArrayList<String> metaTitles;

    /**
     * Creates the NormalPage object by using the Page constructor, setting
     * the pageType to "normal", then parsing the contents of the Page for
     * information.
     * @param contents String contents of the Page
     */
    public NormalPage(String contents) {
        super(contents);
        bodyText = new StringBuilder();
        this.pageType = "normal";
        parse();
    }

    /**
     * Parses through the entire contents of the page.
     * Acts as a sort of controller for the other parsing functions.
     */
    private void parse() {
        headers = new ArrayList<>();
        metaTitles = new ArrayList<>();
        String[] lines = this.contents.split("\n={2,6}");
        parseTopPart(lines[0]);
        parseHeaders(lines);
        parseMetaTitles();
    }

    /**
     * Parses the top part of the page text, looking for the categories and
     * the summary text. 
     * @param text String top part of the page.
     */
    private void parseTopPart(String text) {
        categories = new ArrayList<>();
        StringBuilder summary = new StringBuilder();

        for (String line: text.split("\n")) {
            line = line.trim();

            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                
                // add each of the categories to the categories attribute
                for (String topic: topicsTemp.split(",")) {
                    categories.add(topic.trim());
                }

            } else if (!line.isEmpty() && !line.startsWith("[[")) {
                summary.append(line).append("\n");
            }
        }

        // clean up the summary then add it to the attribute
        MetadataParse summary_metadata = extractMetadata(summary.toString());
        metadata.addAll(summary_metadata.metadata);
        this.summary = removeExtraTags(summary_metadata.text());
    }

    /**
     * Parses the rest of the file (divided by headers). Organizes the
     * names of the headers, the body text, and the metadata into their
     * respective attributes.
     * @param categories List of strings containing the split up lines 
     * from the page. (Split up on newlines)
     */
    private void parseHeaders(String[] categories) {
        for (int i = 1; i < categories.length; i++) {
            String[] parts = categories[i].split("={2,6}\n");

            // to account for a header with no text (error in the doc?)
            String header;
            try {
                header = removeHeaderDashes(parts[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }

            // if no body of text
            if (parts.length == 1) {
                // add to headers if non-standard header, ignore if it is
                if (!generalHeaders.contains(header)) {
                    MetadataParse header_metadata = extractMetadata(header);
                    metadata.addAll(header_metadata.metadata);
                    header = removeExtraTags(header_metadata.text());
                    if (!header.isEmpty()) headers.add(header);
                }
                continue;
            }  // else body of text exists

            String body = parts[1];
            MetadataParse body_metadata = extractMetadata(body);
            metadata.addAll(body_metadata.metadata);

            if (!headersToIgnore.contains(header)) {
                headers.add(header);
                body = removeExtraTags(body_metadata.text());
                if (!body.isEmpty()) bodyText.append(body).append("\n");
            }

        }
    }

    /**
     * Goes through each of the metadata strings and parses them
     * for titles. Adds each to the metaTitles attribute.
     */
    private void parseMetaTitles() {
        for (String line: metadata) {
            String title = parseTPLforTitle(line);
            if (title != null) {
                metaTitles.add(title);
            }
        }
    }

    /**
     * @return string representation of the obhect
     */
    @Override
    public String toString() {
        String shortSummary;
        if (summary.length() < 50) {
            shortSummary = summary;
        } else {
            shortSummary = summary.substring(0, 50);
        }

        return "Normal Page: " +
                this.title +
                "\n\t Categories: " +
                categories.toString() +
                "\n\t Summary: " +
                shortSummary +
                "...";
    }
}
