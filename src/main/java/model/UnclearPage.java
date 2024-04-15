package model;

import java.util.ArrayList;

public class UnclearPage extends Page {
    ArrayList<String> headers;

    public UnclearPage(String contents) {
        super(contents);
        parseForHeaders();
    }

    private void parseForHeaders() {
        headers = new ArrayList<>();

    }


}
