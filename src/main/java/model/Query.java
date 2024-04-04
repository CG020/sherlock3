package model;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Query {
    Directory index;
    StandardAnalyzer analyzer;

    public Query(Directory index, StandardAnalyzer analyzer) {
        this.index = index;
        this.analyzer = analyzer;
    }


    // Lucene's default scoring system is BM25!!! this is good
    /**
     * Runs the lucene query then prints and returns a list of the matching documents
     * @param queryStr: String query given to Lucene's QueryParser
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    private List<ResultClass> runQuery(String queryStr) throws IOException {
        // make the query parser
        org.apache.lucene.search.Query q;
        try { q = new QueryParser("contents", analyzer).parse(queryStr); }
        catch (ParseException e) { throw new RuntimeException(e); }

        // search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);

        IndexSearcher searcher = new IndexSearcher(reader);

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
}
