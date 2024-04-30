/**
 * Katelyn Rohrer, Camila Grubb, Lydia Dufek
 * CSC 483/583
 * This file defines the UnclearPage object, which is a type of Page.
 */
package model;

import java.util.ArrayList;

/**
 * This object contains the same information as a Page, with the added fields
 * `categories` and `headers`, which is all that makes up the body text of
 * an UnclearPage.
 */
public class UnclearPage extends Page {
    ArrayList<String> categories;
    ArrayList<String> headers;


    /**
     * Creates the UnclearPage object by using the Page constructor, setting
     * the pageType to "unclear", then parsing the contents of the Page for
     * information.
     * @param contents String contents of the Page
     */
    public UnclearPage(String contents) {
        super(contents);
        this.pageType = "unclear";
        parse();
    }

    /**
     * Parses through the contents of the file, looking for the "categories"
     * section and all of the headers. 
     */
    private void parse() {
        categories = new ArrayList<>();
        headers = new ArrayList<>();
        for (String line: this.contents.split("\n")) {
            line = line.trim();

            // add each of the categories (trimmed, split, and metadata extracted)
            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                MetadataParse category_metadata = extractMetadata(topicsTemp);
                metadata.addAll(category_metadata.metadata);
                for (String topic: category_metadata.text().split(",")) {
                    categories.add(removeExtraTags(topic.trim()));
                }

            // add header to headers list, if we don't want to ignore it
            } else if (line.startsWith("=")) {
                if (ignoreHeader(line)) {
                    continue;
                }
                headers.add(Page.removeHeaderDashes(line));
            }
        }
    }

    /**
     * Determines whether a header should be ignored or not based on
     * whether it is one of the three search strings for ignored headers.
     * @param line String header to be judged
     * @return boolean: true if should be ignored, false if not
     */
    private boolean ignoreHeader(String line) {
        return (line.contains("=See also=")
                || line.contains("=Other=")
                || line.contains("=Other uses="));
    }

    /**
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "Unclear Page: " +
                this.title +
                "\n\t Categories: " +
                categories.toString() +
                "\n\t Headers: " +
                headers.toString();
    }

}
