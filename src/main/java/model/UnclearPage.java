package model;

import java.util.ArrayList;

public class UnclearPage extends Page {
    ArrayList<String> headers;


    public UnclearPage(String contents) {
        super(contents);
        this.pageType = "unclear";
        parseForHeaders();
    }


    private void parseForHeaders() {
        headers = new ArrayList<>();

    }


}
