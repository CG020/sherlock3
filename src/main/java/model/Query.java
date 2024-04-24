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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;


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
    List<ResultClass> ans;


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
        boosts.put("summary", 1.0f);
        boosts.put("categories", 0.8f);
        // boosts.put("bodyText", 0.8f);
        // boosts.put("headers", 0.3f);

        this.multiParser = new MultiFieldQueryParser(
            new String[] {"summary", "categories"},
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


    public static class tuning extends BM25Similarity {
        public tuning(float k, float b) {
            super(k, b);
        }
    }


    // Lucene's default scoring system is BM25!!! this is good
    /**
     * Runs the lucene query then prints and returns a list of the matching documents
     * @param queryStr: String query given to Lucene's QueryParser
     * @return List<ResultClass>: A list of ResultClass objects representing the matching
     * documents. A ResultClass object holds a document's score Document object and score
     */
    public List<ResultClass> runQuery(String category, String queryStr) throws IOException {
        // lucene query, answer arraylist, number of hits to return
        // org.apache.lucene.search.Query categoryQuery;
        org.apache.lucene.search.Query multiQuery;
        // org.apache.lucene.search.Query q;
        List<ResultClass> ans = new ArrayList<>();
        int hitsPerPage = 10;

        // boosts the category word
        queryStr =  queryStr + " AND " + category + "^2.0";
        // q is a combination of two types of query parsing multifield and phrase
        BooleanQuery.Builder q = new BooleanQuery.Builder();

        // categories not parse yet wait to test
        // categoryQuery = new TermQuery(new Term("categories", category));
        
        // multi-field query
        assignBoosts(this.boosts);
        try {
            multiQuery = multiParser.parse(queryStr);
            q.add(multiQuery, BooleanClause.Occur.SHOULD);
        }
        catch (ParseException e) { throw new RuntimeException(e); }

        // phrase query
        List<PhraseQuery> phraseQueries = buildPhraseQ(queryStr);
        for (PhraseQuery pq : phraseQueries) {
            q.add(pq, BooleanClause.Occur.SHOULD);
        }

        TopDocs pages = searcher.search(q.build(), hitsPerPage);
        ScoreDoc[] hits = pages.scoreDocs;

        System.out.format("Query '%s' returned:\n", queryStr);
        for (ScoreDoc hit : hits) {
            Document d = searcher.doc(hit.doc);
            String title = d.get("title");
            ResultClass page = new ResultClass();
            page.DocName = d;
            page.docScore = hit.score;
            ans.add(page);
            System.out.format("\t%s: %f\n", title, hit.score);
            // debugging
            // System.out.println("\n" + d.get("categories") + "\n");
        }
        
        return ans;
    }

    public static void main(String[] args ) {
        // mvn exec:java -D"exec.mainClass=model.Query" dont mind me maven notes 

        Directory index;
        DirectoryReader reader;
        IndexSearcher searcher;
        try {
            index = FSDirectory.open(Paths.get("IndexBuild-20240424T182433Z-001\\IndexBuild"));
            reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);

            // $k_1$ and $k_3$ to a value between 1.2 and 2 and b = 0.75 -- random rn
            float k = 1.8f;
            float b = 0.75f;
            searcher.setSimilarity(new tuning(k, b));


            Query q = new Query(searcher, new StandardAnalyzer());
            String category = ("NEWSPAPER").toLowerCase();
            String question = "The dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation";
            List<ResultClass> ans = q.runQuery(category, question);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not set up searcher");
        }


    }
}
