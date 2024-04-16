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
            if (line.startsWith("#REDIRECT")) {
                String lineTemp = line.substring("#REDIRECT".length()).trim();
                this.redirect = removeTPL(lineTemp);
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
