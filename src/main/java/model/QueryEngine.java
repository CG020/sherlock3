/*
 * Katelyn Rohrer
 * CSC 583
 * Program to run several simple lucene queries, using AND, AND NOT, and
 * proximity queries, as well as changing the scoring algorithm.
 */

package model;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;


import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * Object to run and contain a Lucene query
 */
public class QueryEngine {

    boolean indexExists = false;
    String inputFilePath;
    Directory index;
    StandardAnalyzer analyzer;

    /**
     * Creates a QueryEngine object and builds the query index
     * @param inputFile: String file path of a text file containing the documents
     */
    public QueryEngine(String inputFile){
        inputFilePath = inputFile;
        buildIndex();
    }


    /**
     * Builds the index of documents to be later queried by reading in a text
     * file and parsing it into Document objects
     */
    private void buildIndex() {
        // Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(inputFilePath).getFile());

        analyzer = new StandardAnalyzer();
        index = new ByteBuffersDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        try (Scanner inputScanner = new Scanner(file)) {
            IndexWriter writer = new IndexWriter(index, config);
            // add each of the documents
            while (inputScanner.hasNextLine()) {
                // line parsing
                String fileLine = inputScanner.nextLine();
                String[] elems = fileLine.split(" ", 2); // split off the first token
                String name = elems[0];
                String contents = elems[1];

                // create document and add to index
                Document doc = new Document();
                doc.add(new StringField("docid", name, Field.Store.YES));
                doc.add(new TextField("contents", contents, Field.Store.YES));
                writer.addDocument(doc);
            }
            writer.close();
            inputScanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        indexExists = true;
    }


    public static void main(String[] args ) {
        try {
            String fileName = "input.txt";
            System.out.println("********Welcome to  Homework 3!");
            String[] query13a = {"information", "retrieval"};
            QueryEngine objQueryEngine = new QueryEngine(fileName);
            objQueryEngine.runQ1_1(query13a);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    /**
     * Runs a basic Lucene query using AND to join tokens together
     * @param query: List of query tokens
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQ1_1(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if (!indexExists) {
            buildIndex();
        }

        String queryStr = buildQuery(query, "AND");
        return runQuery(queryStr, true);
    }


    /**
     * Runs a basic Lucene query using AND to join tokens together
     * @param query: List of query tokens
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQ1_2_a(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if (!indexExists) {
            buildIndex();
        }
        String queryStr = buildQuery(query, "AND");
        return runQuery(queryStr, true);
    }


    /**
     * Runs a basic Lucene query using AND NOT to join tokens together
     * @param query: List of query tokens
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQ1_2_b(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if (!indexExists) {
            buildIndex();
        }
        String queryStr = buildQuery(query, "AND NOT");
        return runQuery(queryStr, true);
    }


    /**
     * Runs a Lucene query of tokens within a one word proximity to each other
     * @param query: List of query tokens
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQ1_2_c(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if (!indexExists) {
            buildIndex();
        }
        String queryStr = '"' + String.join(" ", query) + "\"~1";
        return runQuery(queryStr, true);
    }


    /**
     * Runs a query using the non-default scoring system, tf-idf.
     * @param query: List of query tokens
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQ1_3(String[] query) throws java.io.FileNotFoundException,java.io.IOException {
        if (!indexExists) {
            buildIndex();
        }

        String queryStr = buildQuery(query, "AND");
        return runQuery(queryStr, false);
    }


    /**
     * Builds the Lucene query based on the given list of tokens and the operation
     * that they join on.
     * @param query: List of query tokens
     * @param op: Operation for token joining (AND, OR, etc)
     * @return String: the resulting string that was built
     */
    private String buildQuery(String[] query, String op) {
        String[] quotedWords = new String[query.length];
        for (int i = 0; i < query.length; i++) {
            quotedWords[i] = '"' + query[i] + '"';
        }
        String joinStr = " " + op.toUpperCase() + " ";
        return String.join(joinStr, quotedWords);
    }


    /**
     * Runs the lucene query then prints and returns a list of the matching documents
     * @param queryStr: String query given to Lucene's QueryParser
     * @param defaultScoring: Boolean representing whether Lucene's default
     * scoring system (BM25) will be used or not, with the alternative being tf-idf scoring
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    private List<ResultClass> runQuery(String queryStr, Boolean defaultScoring) throws IOException {
        // make the query parser
        Query q;
        try { q = new QueryParser("contents", analyzer).parse(queryStr); }
        catch (ParseException e) { throw new RuntimeException(e); }

        // search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);

        IndexSearcher searcher = new IndexSearcher(reader);
        if (!defaultScoring)
            searcher.setSimilarity(new ClassicSimilarity()); // Classic is tf-idf

        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // add to score
        System.out.format("Query '%s' returned:\n", queryStr);
        List<ResultClass> ans = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            Document d = searcher.doc(hit.doc);
            ResultClass docObj = new ResultClass();
            docObj.DocName = d;
            docObj.docScore = hit.score;
            ans.add(docObj);
            System.out.format("\t%s: %f\n", d.get("docid"), hit.score);
        }
        reader.close();
        return ans;
    }


    private List<ResultClass> returnDummyResults(int maxNoOfDocs) {

        List<ResultClass> doc_score_list = new ArrayList<>();
            for (int i = 0; i < maxNoOfDocs; ++i) {
                Document doc = new Document();
                doc.add(new TextField("title", "", Field.Store.YES));
                doc.add(new StringField("docid", "Doc"+Integer.toString(i+1), Field.Store.YES));
                ResultClass objResultClass= new ResultClass();
                objResultClass.DocName =doc;
                doc_score_list.add(objResultClass);
            }

        return doc_score_list;
    }

}
