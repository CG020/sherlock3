/*
 * Katelyn Rohrer, Camila Grubb, Lydia Dufek
 * CSC 483/583
 * This file defines the MetadataParse, which is a helper object used with
 * Page parsing.
 */
package model;

import java.util.ArrayList;

/**
 * MetadataParse is meant to be used during metadata parsing. Text contains
 * the string from the document WITHOUT the metadata that was originally
 * within it. Metadata contains all of the metadata information that was
 * extracted out.
 */
public class MetadataParse {
    StringBuilder text;
    ArrayList<String> metadata;

    /**
     * Creates a new MetadataParse object by setting up empty field for
     * text and metadata.
     */
    public MetadataParse() {
        this.text = new StringBuilder();
        this.metadata = new ArrayList<>();
    }

    /**
     * Getter for the text. Converts the stringBuilder to a string
     * and trims the text of any extra whitespace or special characters
     * before returning.
     * @return String of the text
     */
    public String text() {
        return text.toString().trim();
    }

    /**
     * String method for the MetadataParse object. Gives information about
     * the text without tags and the data within the tags themselves.
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "Text: " + this.text + "\tMeta Array:" + this.metadata;
    }

}
