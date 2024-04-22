package model;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Query {
    Directory index;
    StandardAnalyzer analyzer;
    MultiFieldQueryParser parser;
    HashMap<String, Float> boosts;


    public Query(Directory index, StandardAnalyzer analyzer) {
        this.index = index;
        this.analyzer = analyzer;
    }


    // this is where we will boost nujmbers for the doc fields - creates multifield parser
    public void assignBoosts(HashMap<String, Float> boosts)  {
        boosts = new HashMap<>();
        boosts.put("categories", 2.0f);
        boosts.put("summary", 1.5f);
        boosts.put("bodyText", 1.5f);
        boosts.put("headers", 0.5f);

        this.parser = new MultiFieldQueryParser(
            new String[] {"categories", "summary", "bodyText", "headers"},
            analyzer,
            boosts
        );
    }


    // Lucene's default scoring system is BM25!!! this is good
    /**
     * Runs the lucene query then prints and returns a list of the matching documents
     * @param queryStr: String query given to Lucene's QueryParser
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQuery(String queryStr) throws IOException {
        // make the query parser
        org.apache.lucene.search.Query q;
        // try { q = new QueryParser("text", analyzer).parse(queryStr); }
        // catch (ParseException e) { throw new RuntimeException(e); }
        

        assignBoosts(this.boosts);
        try {q = parser.parse(queryStr); }
        catch (ParseException e) { throw new RuntimeException(e); }

        int hitsPerPage = 10;

        String wikiPath = "sample";
        IndexReader reader = DirectoryReader.open(index);
        // serilized 

        IndexSearcher searcher = new IndexSearcher(reader);

        TopDocs pages = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = pages.scoreDocs;

        // add to score
        System.out.format("Query '%s' returned:\n", queryStr);
        List<ResultClass> ans = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            Document d = searcher.doc(hit.doc);
            ResultClass page = new ResultClass();
            page.DocName = d;
            page.docScore = hit.score;
            ans.add(page);
            System.out.format("\t%s: %f\n", d.get("docid"), hit.score);
        }
        reader.close();
        return ans;
    }

    public static void main(String[] args ) {
        // mvn exec:java -D"exec.mainClass=model.Index"
        String wikiPath = "sample";
        try {
            Index indexer = new Index(wikiPath);
            Query q = new Query(indexer.index, new StandardAnalyzer());
            try {
                List<ResultClass> ans = q.runQuery("The dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation");
            } catch (IOException e) { throw new RuntimeException(e); }
        } catch (IOException e) { e.printStackTrace();}

    }
}
