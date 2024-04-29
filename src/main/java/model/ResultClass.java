/*
 * Katelyn Rohrer, Lydia Dufek, Camila Grubb
 * CSC 483/583
 * Defines the ResultClass class. ResultClass defines the returned pages 
 * that querying returns and their attributes.
 */
package model;
import org.apache.lucene.document.Document;

/**
 * ResultClass used to manage the information of returned wiki pages
 */
public class ResultClass {
    Document DocName; // title of the wiki page
    double docScore = 0; // score assigned to the wiki page after querying
}
