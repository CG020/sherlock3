package model;

public class RedirectPage extends Page{
    String redirect;

    public RedirectPage(String contents) {
        super(contents);
        this.pageType = "redirect";
        parse();
    }

    private void parse() {
        for (String line: this.contents.split("\n")) {
            line = line.trim();
            if (line.startsWith("#REDIRECT")) {
                String lineTemp = line.substring("#REDIRECT".length()).trim();
                MetadataParse redirect_metadata = extractMetadata(lineTemp);
                metadata.addAll(redirect_metadata.metadata);
                this.redirect = removeExtraTags(redirect_metadata.text());
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Redirect Page: "
                + this.title
                + " --> "
                + this.redirect;
    }
}
