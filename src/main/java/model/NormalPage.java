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
    static final List<String> headersToIgnore = Arrays.asList(
            "Images", "External links", "Sources"
    );

    ArrayList<String> categories;
    ArrayList<String> headers;
    String summary;
    StringBuilder bodyText;
    ArrayList<String> metaTitles;

    public NormalPage(String contents) {
        super(contents);
        bodyText = new StringBuilder();
        this.pageType = "normal";
        parse();
    }

    private void parse() {
        headers = new ArrayList<>();
        metaTitles = new ArrayList<>();
        String[] lines = this.contents.split("\n={2,6}");
        parseTopPart(lines[0]);
        parseHeaders(lines);
        parseMetaTitles();
    }

    private void parseTopPart(String text) {
        categories = new ArrayList<>();
        StringBuilder summary = new StringBuilder();
        for (String line: text.split("\n")) {
            line = line.trim();
            if (line.startsWith("CATEGORIES:")) {
                String topicsTemp = line.substring("CATEGORIES:".length());
                for (String topic: topicsTemp.split(",")) {
                    categories.add(topic.trim());
                }
            } else if (!line.isEmpty() && !line.startsWith("[[")) {
                summary.append(line).append("\n");
            }
        }
        MetadataParse summary_metadata = extractMetadata(summary.toString());
        metadata.addAll(summary_metadata.metadata);
        this.summary = removeExtraTags(summary_metadata.text());
    }

    private void parseHeaders(String[] categories) {
        for (int i = 1; i < categories.length; i++) {
            String[] parts = categories[i].split("={2,6}\n");

            // THERE'S LIKE ONE HEADER WITH NO TEXT IN THE MIDDLE
            String header;
            try {
                header = removeHeaderDashes(parts[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }

            // if no body of text
            if (parts.length == 1) {
                // add to headers if non-standard header, ignore if it is
                if (!generalHeaders.contains(header)) {
                    MetadataParse header_metadata = extractMetadata(header);
                    metadata.addAll(header_metadata.metadata);
                    header = removeExtraTags(header_metadata.text());
                    if (!header.isEmpty()) headers.add(header);
                }
                continue;
            }  // else body of text exists

            String body = parts[1];
            MetadataParse body_metadata = extractMetadata(body);
            metadata.addAll(body_metadata.metadata);

            if (!headersToIgnore.contains(header)) {
                headers.add(header);
                body = removeExtraTags(body_metadata.text());
                if (!body.isEmpty()) bodyText.append(body).append("\n");
            }

        }
    }

    private void parseMetaTitles() {
        for (String line: metadata) {
            String title = parseTPLforTitle(line);
            if (title != null) {
                metaTitles.add(title);
            }
        }
    }

    @Override
    public String toString() {
        String shortSummary;
        if (summary.length() < 50) {
            shortSummary = summary;
        } else {
            shortSummary = summary.substring(0, 50);
        }

        return "Normal Page: " +
                this.title +
                "\n\t Categories: " +
                categories.toString() +
                "\n\t Summary: " +
                shortSummary +
                "...";
    }
}
