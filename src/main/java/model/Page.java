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

    /**
     * Getter for the page type
     * @return PageType as a String. Either "normal", "unclear", or "redirect"
     */
    public String getPageType() {
        return pageType;
    }

    /**
     * Parses out the title of the page from the contents and sets it to
     * title attribute. Should only be called by the constructor.
     * @param contents String contents of the page
     */
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
     * Extracts the metadata from a given string of text. Metadata includes
     * any pair of matching tags [tpl], [ref], or <ref>. It takes the outermost
     * occurrence of (nested) matching tags and removes all text from between
     * them and adds it to the metadata attribute.
     * @param text The string of text to be parsed
     * @return MetadataParse object, which contains field for the text without
     * metadata and an ArrayList<String> of the metadata itself.
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

                // adds the metadata to return object and removes it from the text
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

    /**
     * Removes any extra metadata tags from the text. Since some pages do not
     * contain balanced tags, some tags can be left over after extractMetadata
     * is called. This removes those tags.
     * @param text The string text to be parsed
     * @return String text without [tpl], [ref], and <ref> tags
     */
    protected static String removeExtraTags(String text) {
        String[] tags = {"\\[tpl\\]", "\\[/tpl\\]", "\\[re\\f]", "\\[/ref\\]", "<ref>", "</ref>"};
        for (String tag: tags) {
            text = text.replaceAll(tag, "");
        }
        return text;
    }

    /**
     * Takes in a string of TPL metadata and returns the title
     * from within that metadata. Used to extract extra information
     * from the TPL metadata.
     * @param text A TPL metadata string
     * @return The title of that metadata, if exists. Null if it can't be found.
     */
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

    /**
     * Clears the contents attribute of the Page. This helps clear up
     * memory during processing.
     */
    public void clearContents() {
        this.contents = null;
    }

}