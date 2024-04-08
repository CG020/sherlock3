package model;

public class Page {
    String title;
    String categories;
    String summary;
    String text;
    String rawText;
    String pageType;

    public Page(String contents) {
        rawText = contents;
        if (checkStandardPage()) {
            pageType = "standard";
            processPage();
        } else {
            pageType = "unprocessed";
        }
    }

    public boolean checkStandardPage() {
        return rawText.contains("]]")
                && rawText.contains("CATEGORIES:")
                && rawText.contains("==");
    }

    public void processPage() {
        int titleEnd = rawText.indexOf("]]");
        title = rawText.substring(0,titleEnd);

        int catIndex = rawText.indexOf("CATEGORIES:");
        int endCatIndex = rawText.indexOf('\n', catIndex);
        categories = rawText.substring(catIndex, endCatIndex);

        int endSummary = rawText.indexOf("==", endCatIndex);
        summary = rawText.substring(endCatIndex, endSummary);

        text = rawText.substring(endSummary);
    }

}
