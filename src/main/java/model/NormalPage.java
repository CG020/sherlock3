package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NormalPage extends Page{
    static final List<String> generalHeaders = Arrays.asList(
            "Images", "See also", "References",
            "Bibliography", "Distinctions", "Further reading",
            "Notes", "External links", "Sources"
    );

    ArrayList<String> categories;
    ArrayList<String> headers;
    String summary;
    StringBuilder bodyText;
    ArrayList<String> metadataTitles;

    public NormalPage(String contents) {
        super(contents);
        this.pageType = "normal";
//        System.out.println(this.title);
        parse();
//        System.out.println(headers.toString());
    }

    private void parse() {
        headers = new ArrayList<>();
        String[] lines = this.contents.split("\n={2,6}");
        parseTopPart(lines[0]);
        parseHeaders(lines);
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

    private void parseHeaders(String[] categories) {
        for (int i = 1; i < categories.length; i++) {
            String[] parts = categories[i].split("={2,6}\n");
            String header = removeHeaderDashes(parts[0]);

            // if no body of text
            if (parts.length == 1) {
                // add to headers if non-standard header, ignore if it is
                if (!generalHeaders.contains(header)) {
                    headers.add(header);
                }
                continue;
            }

            String body = parts[1];
//            System.out.println(removeTPL(body));



        }
    }

    private void parseMetadata(String metadata) {

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
