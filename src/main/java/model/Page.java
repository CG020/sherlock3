package model;

abstract class Page {
    private String title;
    private String contents;
    private String pageType;


    public Page (String contents) {
        this.contents = contents;
        parseOutTitle(contents);
        determinePageType(contents);
        removeHeaderDashes(contents.split("\n")[3]);
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

    private void determinePageType(String contents) {
        String unclearSearchPhrase = this.title + " may refer to:";
        String redirectSearchPhrase = "#REDIRECT";

        if (contents.contains(unclearSearchPhrase)) {
            this.pageType = "unclear";
        } else if (contents.contains(redirectSearchPhrase)) {
            this.pageType = "redirect";
        } else {
            this.pageType = "normal";
        }

    }


    //
    // METHODS TO CLEAN UP STRINGS BELOW
    //

    private String removeDoubleBrackets(String text) {
        return text.substring(2, text.length()-2);
    }

    private void removeHeaderDashes(String header) {
        int dashCount;
        for (dashCount = 0; header.charAt(dashCount) == '='; dashCount++);
        System.out.println(dashCount);
    }


}
