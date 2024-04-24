package model;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Query {
    IndexSearcher searcher;
    StandardAnalyzer analyzer;
    MultiFieldQueryParser multiParser;
    HashMap<String, Float> boosts;


    public Query(IndexSearcher searcher, StandardAnalyzer analyzer) {
        this.searcher = searcher;
        this.analyzer = analyzer;
    }

    /**
     * assigns subjective boost values to each field, builds the mutilFieldParser
     * @param boosts the hashmap that stores boost numbers
     */
    public void assignBoosts(HashMap<String, Float> boosts)  {
        boosts = new HashMap<>();
        boosts.put("categories", 1.0f);
        boosts.put("summary", 0.8f);
        boosts.put("bodyText", 0.8f);
        boosts.put("headers", 0.3f);

        this.multiParser = new MultiFieldQueryParser(
            new String[] {"categories", "summary", "bodyText", "headers"},
            analyzer,
            boosts
        );
    }


    public List<PhraseQuery> buildPhraseQ(String queryString) {
        String[] words = queryString.split("\\s+");

        List<PhraseQuery> queries = new ArrayList<>();

        for (String word : words) {
            for (int i = 0; i < words.length - 1; i++) {
                PhraseQuery.Builder builder = new PhraseQuery.Builder();
                builder.add(new Term(word, words[i]));
                builder.add(new Term(word, words[i + 1]));
                PhraseQuery phrase = builder.build();
                queries.add(phrase);
            }
        }
        return queries;
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

        int hitsPerPage = 10;
        
        // multi-field query
        assignBoosts(this.boosts);
        try {q = multiParser.parse(queryStr); }
        catch (ParseException e) { throw new RuntimeException(e); }

        // phrase query
        List<TopDocs> phraseHits = new ArrayList<TopDocs>();
        List<PhraseQuery> phraseQueries = buildPhraseQ(queryStr);
        for (PhraseQuery pq : phraseQueries) {
            phraseHits.add(searcher.search(pq, hitsPerPage));
        }


        TopDocs pages = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = pages.scoreDocs;

        // add to score and display the returned docs
        System.out.format("Query '%s' returned:\n", queryStr);
        List<ResultClass> ans = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            Document d = searcher.doc(hit.doc);
            String title = d.get("title");
            ResultClass page = new ResultClass();
            page.DocName = d;
            page.docScore = hit.score;
            ans.add(page);
            System.out.format("\t%s: %f\n", title, hit.score);
        }
        return ans;
    }

    public static void main(String[] args ) {
        // mvn exec:java -D"exec.mainClass=model.Index"

        Directory index;
        DirectoryReader reader;
        IndexSearcher searcher;
        try {
            index = FSDirectory.open(Paths.get("IndexBuild-20240424T182433Z-001\\IndexBuild"));
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);

            Query q = new Query(searcher, new StandardAnalyzer());
            List<ResultClass> ans = q.runQuery("The dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not set up searcher");
        }


    }
}
