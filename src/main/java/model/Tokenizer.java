package model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class Tokenizer {

    /**
     *  Tokenizes a category. Given an array of strings, it tokenizes each string.
     * @param categories: An array of strings that contain category information
     * @return String: Tokenized string result of the categories.
     */
    public static String tokenizeCategories(ArrayList<String> categories) {
        StringBuilder result = new StringBuilder();

        for (String category : categories) {
            result.append(tokenize(category)).append(" ");
        }

        return result.toString();
    }

    /**
     *  Tokenizes a header. Given an array of strings, it tokenizes each string.
     * @param headers: An array of strings that contains each header for the given page
     * @return String: Tokenized string result of the headers.
     */
    public static String tokenizeHeaders(ArrayList<String> headers) {
        StringBuilder result = new StringBuilder();

        for (String header : headers) {
            result.append(tokenize(header)).append(" ");
        }

        return result.toString();
    }

    /**
     *  Tokenizes a summary. Given a string of the summary content it will return a tokenized version.
     * @param content: A string of the content that was within a summary.
     * @return String: Tokenized string result of the summary content info.
     */
    public static String tokenizeSummary(String content) {
        return tokenize(content);
    }

    /**
     *  Tokenizes body text. Given a string of the body text content it will return a tokenized version.
     * @param bodyText: A string of the body text.
     * @return String: Tokenized string result of the body text.
     */
    public static String tokenizeBodyText(StringBuilder bodyText) {
        return tokenize(bodyText.toString());
    }

    /**
     *  Tokenizes all the meta titles within a page.
     * @param metaTitles: An array of strings that contains each meta title for the given page
     * @return String: Tokenized string result of the meta titles.
     */
    public static String tokenizeMetaTitles(ArrayList<String> metaTitles) {
        StringBuilder result = new StringBuilder();

        for (String metaTitle : metaTitles) {
            result.append(tokenize(metaTitle)).append(" ");
        }

        return result.toString();
    }

    /**
     *  Tokenizes the query.
     * @param query: The query.
     * @return String: Tokenized string result of the given query.
     */
    public static String tokenizeQuery(String query) {
        return tokenize(query);
    }

    /**
     * This function handles the actual tokenization part. It creates a StringBuilder, as its easier to concatenate to, and
     * sends the content through a StandardAnalyzer that removes all stop words. It then uses 2 filters, LowerCaseFilter and
     * KStemFilter, to lower case all tokens and reduce each token to its stemmed version. This aids in indexing as well as
     * querying.
     * @param text: String that needs to be tokenized
     * @return String: Tokenized version
     */
    private static String tokenize(String text) {
        StringBuilder newString = new StringBuilder();

        //analyzer that also removes stop words
        try (Analyzer analyzer = new StandardAnalyzer(); TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text))) {
            //lower case
            TokenStream lowerCaseTokenStream = new LowerCaseFilter(tokenStream);
            //stemming 
            TokenStream kstemTokenStream = new KStemFilter(lowerCaseTokenStream);
            CharTermAttribute charTermAttribute = kstemTokenStream.addAttribute(CharTermAttribute.class);
            kstemTokenStream.reset();
            while (kstemTokenStream.incrementToken()) {
                newString.append(charTermAttribute.toString()).append(" ");
            }
            kstemTokenStream.end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return newString.toString().trim();
    }
}
