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


    public UnclearPage(String contents) {
        super(contents);
        this.pageType = "unclear";
        parse();
    }

    private void parse() {
        categories = new ArrayList<>();
        headers = new ArrayList<>();
        for (String line: this.contents.split("\n")) {
            line = line.trim();
            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                MetadataParse category_metadata = extractMetadata(topicsTemp);
                metadata.addAll(category_metadata.metadata);
                for (String topic: category_metadata.text().split(",")) {
                    categories.add(removeExtraTags(topic.trim()));
                }
            } else if (line.startsWith("=")) {
                if (ignoreHeader(line)) {
                    continue;
                }
                headers.add(Page.removeHeaderDashes(line));
            }
        }
    }

    private boolean ignoreHeader(String line) {
        return (line.contains("=See also=")
                || line.contains("=Other=")
                || line.contains("=Other uses="));
    }

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
