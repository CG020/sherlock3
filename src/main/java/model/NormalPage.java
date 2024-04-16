package model;

import java.util.ArrayList;

public class NormalPage extends Page{
    ArrayList<String> categories;
    ArrayList<String> headers;
    String summary;

    public NormalPage(String contents) {
        super(contents);
        this.pageType = "normal";
        System.out.println(this.title);
        parse();
    }

    private void parse() {
        headers = new ArrayList<>();
        String[] lines = this.contents.split("\n={2,6}");
        parseTopPart(lines[0]);
    }

    private void parseTopPart(String text) {
        categories = new ArrayList<>();
        StringBuilder summary = new StringBuilder();
        for (String line: text.split("\n")) {
            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                for (String topic: topicsTemp.split(",")) {
                    categories.add(topic.trim());
                }
            } else if (!line.isEmpty() && !line.startsWith("[[")) {
                summary.append(line).append("\n");
            }
        }
        this.summary = removeTPL(summary.toString());
    }

    @Override
    public String toString() {
        return "Normal Page: " +
                this.title +
                "\n\t Categories: " +
                categories.toString() +
                "\n\t Summary: " +
                summary.substring(0, 50) +
                "...";
    }
}
