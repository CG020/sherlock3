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

    public static String tokenizeCategories(ArrayList<String> categories) {
        StringBuilder result = new StringBuilder();

        for (String category : categories) {
            result.append(tokenize(category)).append(" ");
        }

        return result.toString();
    }

    public static String tokenizeHeaders(ArrayList<String> headers) {
        StringBuilder result = new StringBuilder();

        for (String header : headers) {
            result.append(tokenize(header)).append(" ");
        }

        return result.toString();
    }

    public static String tokenizeSummary(String content) {
        return tokenize(content);
    }

    public static String tokenizeBodyText(StringBuilder bodyText) {
        return tokenize(bodyText.toString());
    }

    public static String tokenizeMetaTitles(ArrayList<String> metaTitles) {
        StringBuilder result = new StringBuilder();

        for (String metaTitle : metaTitles) {
            result.append(tokenize(metaTitle)).append(" ");
        }

        return result.toString();
    }

    public static String tokenizeQuery(String query) {
        return tokenize(query);
    }

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
