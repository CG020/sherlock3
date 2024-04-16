package model;

import java.util.ArrayList;

public class UnclearPage extends Page {
    ArrayList<String> categories;
    ArrayList<String> headers;


    public UnclearPage(String contents) {
        super(contents);
        this.pageType = "unclear";
        parse();
    }

    private void parse() {
        categories = new ArrayList<>();
        headers = new ArrayList<>();
        for (String line: this.contents.split("\n")) {
            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                for (String topic: topicsTemp.split(",")) {
                    categories.add(topic.trim());
                }
            } else if (line.startsWith("=")) {
                if (ignoreHeader(line)) {
                    continue;
                }
                headers.add(Page.removeHeaderDashes(line));
            }
        }
    }

    private boolean ignoreHeader(String line) {
        return (line.contains("=See also=")
                || line.contains("=Other=")
                || line.contains("=Other uses="));
    }

    @Override
    public String toString() {
        return "Unclear Page: " +
                this.title +
                "\n\t Categories: " +
                categories.toString() +
                "\n\t Headers: " +
                headers.toString();
    }

}
