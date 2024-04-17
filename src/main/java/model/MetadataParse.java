package model;

import java.util.ArrayList;

class MetadataParse {
    StringBuilder text;
    ArrayList<String> metadata;

    MetadataParse() {
        this.text = new StringBuilder();
        this.metadata = new ArrayList<>();
    }

    String text() {
        return text.toString().trim();
    }

    @Override
    public String toString() {
        return "Text: " + this.text + "\tMeta Array:" + this.metadata;
    }

}
