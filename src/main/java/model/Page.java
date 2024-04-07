package model;

public class Page {
    String title;
    String categories;
    String summary;
    String text;

    public Page(String contents) {
        String[] pages = contents.split("[[");

        for (String page : pages) {
            if (checkNormalized(page)) {
                processPage(page);
            }
        }
    }

    private boolean checkNormalized(String p) {
        if (p.contains("]]" ) && p.contains("CATEGORIES")
            && p.contains("==")) {
            return true;
        }

        return false;
    }

    private void processPage(String p) {
        
        int titleEnd = p.indexOf("]]");
        title = p.substring(0,titleEnd);

        int catIndex = p.indexOf("CATEGORIES:");
        int endCatIndex = p.indexOf('\n', catIndex);
        categories = p.substring(catIndex, endCatIndex);

        int endSummary = p.indexOf("==", endCatIndex);
        summary = p.substring(endCatIndex, endSummary);

        text = p.substring(endSummary);
        
    }
}
