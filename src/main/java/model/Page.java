package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class Page {
    protected String title;
    protected String contents;
    protected String pageType;
    ArrayList<String> metadata;


    public Page (String contents) {
        this.metadata = new ArrayList<>();
        this.contents = contents;
        parseOutTitle(contents);
    }

    public String getTitle() {
        return title;
    }

    public String getPageType() {
        return pageType;
    }

    private void parseOutTitle(String contents){
        String[] title_contents = contents.split("\n", 2);
        this.title = removeDoubleBrackets(title_contents[0]).trim();
    }


    //
    // METHODS TO CLEAN UP STRINGS BELOW
    //

    /**
     * Removes the double brackets around a page title
     */
    protected static String removeDoubleBrackets(String text) {
        return text.replaceAll("^\\[+|\\]+$", "");

//        return text.substring(2, text.length()-2);
    }

    /**
     * Removes n number of dashes around a header, n >= 0.
     * @return The header without the surrounding dashes.
     */
    protected static String removeHeaderDashes(String header) {
        return header.replaceAll("^\\=+|\\=+$", "");
    }

    protected static MetadataParse extractMetadata(String text) {
        MetadataParse retval = new MetadataParse();
        String[][] tags = {{"[tpl]", "[/tpl]"},
                {"[ref]", "[/ref]"},
                {"<ref>", "</ref>"}};
        for (String line: text.split("\n")) {
            for (String[] tag : tags) {
                String openTag = tag[0];
                String closeTag = tag[1];

                Stack<Integer> stack = new Stack<>();
                StringBuilder result = new StringBuilder(line);
                List<Integer> startIndices = new ArrayList<>();
                List<Integer> endIndices = new ArrayList<>();

                for (int i = 0; i < line.length(); i++) {
                    if (i + 4 < line.length() && line.startsWith(openTag, i)) {
                        stack.push(i);
                    } else if (i + 5 < line.length() && line.startsWith(closeTag, i)) {
                        if (stack.isEmpty()) break;
                        int start = stack.pop();

                        // if it's an outermost tag
                        if (stack.isEmpty()) {
                            startIndices.add(start);
                            endIndices.add(i + closeTag.length());
                        }
                    }
                }

                for (int i = startIndices.size() - 1; i >= 0; i--) {
                    retval.metadata.add(result.substring(startIndices.get(i), endIndices.get(i)));
                    result.delete(startIndices.get(i), endIndices.get(i));
                }
                line = String.valueOf(result);
            }
            retval.text.append(line).append("\n");
        }
        return retval;
    }

    protected static String removeExtraTags(String text) {
        String[] tags = {"\\[tpl\\]", "\\[/tpl\\]", "\\[re\\f]", "\\[/ref\\]", "<ref>", "</ref>"};
        for (String tag: tags) {
            text = text.replaceAll(tag, "");
        }
        return text;
    }

    protected static String parseTPLforTitle(String text) {
        String[] tags = removeExtraTags(text).split("\\|");
        for (String tag : tags) {
            if (!tag.contains("=")) continue;
            String[] field_info = tag.split("=");

            if (field_info.length != 2) continue;
            String field = field_info[0].trim();
            String info = field_info[1].trim();

            if (field.equalsIgnoreCase("title") && !info.isEmpty()) {
                return info;
            }
        }
        return null;
    }


}