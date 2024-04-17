package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class Page {
    protected String title;
    protected String contents;
    protected String pageType;


    public Page (String contents) {
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
        this.title = removeDoubleBrackets(title_contents[0]);
    }


    //
    // METHODS TO CLEAN UP STRINGS BELOW
    //

    /**
     * Removes the double brackets around a page title
     */
    static protected String removeDoubleBrackets(String text) {
        return text.replaceAll("^\\[+|\\]+$", "");

//        return text.substring(2, text.length()-2);
    }

    /**
     * Removes n number of dashes around a header, n >= 0.
     * @return The header without the surrounding dashes.
     */
    // TODO FIX	 Headers: [Technologies==, Organisations==, Science==, Culture==]
    static protected String removeHeaderDashes(String header) {
        return header.replaceAll("^\\=+|\\=+$", "");
    }

//    static protected String removeTPL(String line) {
//        StringBuilder result = new StringBuilder();
//        int startIndex = 0;
//        int tplStart = line.indexOf("[tpl]", startIndex);
//        int tplEnd;
//
//        while (tplStart != -1) {
//            tplEnd = line.indexOf("[/tpl]", tplStart);
//
//            if (tplEnd == -1) {  // if end tag doesn't exist, stop
//                break;
//            } else {
//                result.append(line, startIndex, tplStart);
//                startIndex = tplEnd + "[/tpl]".length();
//            }
//
//            // find the next template start
//            tplStart = line.indexOf("[tpl]", startIndex);
//        }
//
//        // add back any remaining part of the string
//        if (startIndex < line.length()) {
//            result.append(line.substring(startIndex));
//        }
//
//        return result.toString();
//    }

    public static String removeTPL(String text) {
        Stack<Integer> stack = new Stack<>();
        StringBuilder result = new StringBuilder(text);
        // List to hold the start and end indices of outermost tpl tags
        List<Integer> startIndices = new ArrayList<>();
        List<Integer> endIndices = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            if (i + 4 < text.length() && text.substring(i, i + 5).equals("[tpl]")) {
                stack.push(i);
            } else if (i + 5 < text.length() && text.substring(i, i + 6).equals("[/tpl]")) {
                int start = stack.pop();

                // Check if the stack is empty to determine if it's an outermost tag
                if (stack.isEmpty()) {
                    startIndices.add(start);
                    endIndices.add(i + 6);  // i + 6 to include the length of the closing tag
                }
            }
        }

        // Removing from the back to avoid messing up the indices
        for (int i = startIndices.size() - 1; i >= 0; i--) {
            result.delete(startIndices.get(i), endIndices.get(i));
        }

        return result.toString();
    }


}
