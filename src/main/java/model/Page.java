package model;

public abstract class Page {
    protected String title;
    protected String contents;
    protected String pageType;


    public Page (String contents) {
        this.contents = contents;
        parseOutTitle(contents);
    }

    public String getTitle() {
        return title;
    }

    public String getPageType() {
        return pageType;
    }

    private void parseOutTitle(String contents){
        String[] title_contents = contents.split("\n", 2);
        this.title = removeDoubleBrackets(title_contents[0]);
    }


    //
    // METHODS TO CLEAN UP STRINGS BELOW
    //

    /**
     * Removes the double brackets around a page title
     */
    static protected String removeDoubleBrackets(String text) {
        return text.substring(2, text.length()-2);
    }

    /**
     * Removes n number of dashes around a header, n >= 0.
     * Precondition: dashes are balanced on the header. Meaning, the same
     * number of dashes exist on both sides of the text.
     * @return The header without the surrounding dashes.
     */
    static protected String removeHeaderDashes(String header) {
        int dashCount;
        for (dashCount = 0; dashCount < header.length(); dashCount++) {
            if (header.charAt(dashCount) != '=') break;
        }

        return header.substring(dashCount, header.length()-dashCount);
    }

    static protected String removeTPL(String line) {
        int startPoint = line.indexOf("[tpl]");
        int endPoint = line.indexOf("[/tpl]");
        while (startPoint != -1 && endPoint != -1) {
            line = line.substring(0, startPoint)
                    + line.substring(endPoint + "[/tpl]".length());

            startPoint = line.indexOf("[tpl]");
            endPoint = line.indexOf("[/tpl]");
        }
        return line;
    }

}
