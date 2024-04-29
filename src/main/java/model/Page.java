/*
 * Katelyn Rohrer, Lydia Dufek, Camila Grubb
 * CSC 483/583
 * This file defines the Page class and supporting methods. A Page is an
 * abstract class for each wikipedia page, containing the title, raw contents,
 * metadata, and pageType of the Page.
 */
package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Defines the abstract class Page, which is the parent class to NormalPage,
 * UnclearPage, and RedirectPage. It also contains several string parsing
 * methods to assist in parsing these pages.
 */
public abstract class Page {
    protected String title;
    protected String contents;
    protected String pageType;
    protected ArrayList<String> metadata;


    /**
     * Defines a new Page object, setting the contents attribute to the
     * given string of contents and parsing out and setting the title based
     * on those contents.
     * @param contents The entire contents of a single wikipedia page
     */
    public Page (String contents) {
        this.metadata = new ArrayList<>();
        this.contents = contents;
        parseOutTitle(contents);
    }

    public String getPageType() {
        return pageType;
    }

    private void parseOutTitle(String contents){
        String[] title_contents = contents.split("\n", 2);
        this.title = removeDoubleBrackets(title_contents[0]).trim();
    }


    //
    // METHODS TO CLEAN UP STRINGS
    //

    /**
     * Removes the double brackets around a page title.
     * @param text String input text surrounded by [[ ]]
     * @return Updated String without brackets
     */
    protected static String removeDoubleBrackets(String text) {
        return text.replaceAll("^\\[+|\\]+$", "");
    }

    /**
     * Removes n number of dashes around a header, n >= 0.
     * @param header String input text in the format: ==text==
     * @return The header without the surrounding equal signs (dashes).
     */
    protected static String removeHeaderDashes(String header) {
        return header.replaceAll("^\\=+|\\=+$", "");
    }

    /**
     *
     * @param text
     * @return
     */
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
                        // if we have a mismatch in the metadata, abort
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

    public void clearContents() {
        this.contents = null;
    }

}