package model;

public abstract class Page {
    protected String title;
    protected String contents;
    protected String pageType;


    public Page (String contents) {
        this.contents = contents;
        parseOutTitle(contents);
//        removeHeaderDashes(contents.split("\n")[3]);
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


    protected String removeDoubleBrackets(String text) {
        return text.substring(2, text.length()-2);
    }


//    protected void removeHeaderDashes(String header) {
//        int dashCount;
//        for (dashCount = 0; header.charAt(dashCount) == '='; dashCount++);
//        System.out.println(dashCount);
//    }


}
