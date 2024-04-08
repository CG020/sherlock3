package model;

import java.util.ArrayList;

public class WikiPage {
    private final ArrayList<Page> pages;

    public WikiPage(String contents) {
        String[] pageStrs = contents.split("\\[\\[");
        pages = new ArrayList<>();

        for (String pageStr : pageStrs) {
            Page p = new Page(pageStr);
            pages.add(p);
        }
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

}
