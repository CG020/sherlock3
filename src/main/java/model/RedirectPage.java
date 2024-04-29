/*
 * Katelyn Rohrer, Camila Grubb, Lydia Dufek
 * CSC 483/583
 * Defines the RedirectPage object, which is a type of Page.
 */
package model;

/**
 * This object contains the same information as a Page, with the added field
 * of a redirect, which is the title of another wikipedia page. This redirect
 * can later be used to "connect" the redirect page to its corresponding normal page.
 */
public class RedirectPage extends Page {
    String redirect;

    /**
     * Creates the RedirectPage object by using the Page constructor, setting
     * the pageType to "redirect", then parsing the contents of the Page for
     * information.
     * @param contents String contents of the Page
     */
    public RedirectPage(String contents) {
        super(contents);
        this.pageType = "redirect";
        parse();
    }

    /**
     * Parses the contents of the page. Essentially just looks for the word
     * #REDIRECT then saves the rest of the string to the title (ignoring
     * metadata)
     */
    private void parse() {
        for (String line: this.contents.split("\n")) {
            line = line.trim();
            // ignores all lines that aren't #REDIRECT
            if (line.startsWith("#REDIRECT")) {
                String lineTemp = line.substring("#REDIRECT".length()).trim();
                MetadataParse redirect_metadata = extractMetadata(lineTemp);
                metadata.addAll(redirect_metadata.metadata);
                this.redirect = removeExtraTags(redirect_metadata.text());
                break;
            }
        }
    }

    /**
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "Redirect Page: "
                + this.title
                + " --> "
                + this.redirect;
    }
}
