package model;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.*;
import java.util.*;
import java.nio.file.Paths;


public class Query {
    IndexSearcher searcher;
    StandardAnalyzer analyzer;
    MultiFieldQueryParser multiParser;
    HashMap<String, Float> boosts = new HashMap<>();
    List<ResultClass> ans;
    // debugging - remove later
    static int correct = 0;


    public Query(IndexSearcher searcher, StandardAnalyzer analyzer) {
        this.searcher = searcher;
        this.analyzer = analyzer;

        // boost assignment
        boosts.put("summary", 1.4f);
        boosts.put("categories", 2.5f);
        boosts.put("bodyText", 2.0f);
        this.multiParser = new MultiFieldQueryParser(
            new String[]{"summary", "categories", "bodyText"},
            analyzer,
            boosts
        );
    }

    /**
     * BM25Similarity configuration 
     */
    public static class tuning extends BM25Similarity {
        public tuning(float k, float b) {
            super(k, b);
        }
    }

    /**
     * Some syntax in a query is actually interpretted by lucene as phrases or boosts - removed them here
     * replace later?
     */
    public String validateQuery (String queryStr) {
        String newStr;

        newStr = queryStr.trim();
        newStr = newStr.replaceAll("\"", "");
        newStr = newStr.replaceAll("!", "");
        newStr = newStr.replaceAll("\\(alex:.*?\\)", "");
        // newStr = newStr.replaceAll("\\(", "").replaceAll("\\)", "");

        return newStr;
    }

    /**
     * reads in all questions and separates by category, question, and answer
     */
    public static ArrayList<ArrayList<String>> readQuestions(Scanner scanner) {
        ArrayList<ArrayList<String>> questionList = new ArrayList<>();

        ArrayList<String> temp = new ArrayList<>();
        scanner.useDelimiter("\n");
        while (scanner.hasNext()) {
            String next = scanner.next().trim();
            if (next.isEmpty()) {
                questionList.add(temp);
                temp = new ArrayList<>();
            } else {
                temp.add(next);
            }
        }
        scanner.close();
        
        return questionList;
    }

    /**
     * makes sure theres no duplicate docs and prints the results for ans in organized format
     * @param hitsPerPage results number 
     * @param finalQuery the boolean query with all the configs from runQuery
     * @return List<ResultCalss>
     * @throws IOException
     */
    public List<ResultClass> duplicateCheck(int hitsPerPage, BooleanQuery finalQuery) throws IOException {
        Set<String> duplicatesCheck = new HashSet<>();
        List<ResultClass> ans = new ArrayList<>();
        
        int hitCount = 0;
        while (ans.size() < hitsPerPage) {
            TopDocs pages = searcher.search(finalQuery, hitCount + hitsPerPage);
            ScoreDoc[] hits = pages.scoreDocs;
    
            // reach 10!!
            if (hitCount >= pages.totalHits) { break; }
    
            for (int i = hitCount; i < hits.length; i++) {
                Document d = searcher.doc(hits[i].doc);
                String title = d.get("title");
                if (duplicatesCheck.add(title)) {
                    ResultClass page = new ResultClass();
                    page.DocName = d;
                    page.docScore = hits[i].score;
                    ans.add(page);
                    if (ans.size() == hitsPerPage) { break; }
                }
            }
            hitCount += hits.length;
        }
        return ans;
    }

    /**
     * How the phrase query is constructed - phrases are every combination of sequential
     * two words in query string
     */
    public List<PhraseQuery> buildPhraseQ(String queryString) {
        String[] words = queryString.split("\\s+");

        List<PhraseQuery> queries = new ArrayList<>();

        for (int i = 0; i < words.length - 1; i++) {
            PhraseQuery.Builder builder = new PhraseQuery.Builder();
            builder.add(new Term("bodyText", words[i]));
            builder.add(new Term("bodyText", words[i + 1]));
            // builder.add(new Term("bodyText", words[i + 2]));
            PhraseQuery phrase = builder.build();
            queries.add(phrase);
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
    public List<ResultClass> runQuery(String category, String queryStr, String right) throws IOException {
        BooleanQuery.Builder q = new BooleanQuery.Builder();
        int hitsPerPage = 10;
    
        // double quotes signify a phrase query in lucene
        queryStr = validateQuery (queryStr);
        category = validateQuery (category);
    
        // multi field parsee
        org.apache.lucene.search.Query multiQuery;
        try {
            multiQuery = multiParser.parse(queryStr);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing query: " + e.getMessage(), e);
        }

        if (multiQuery != null) {
            q.add(multiQuery, BooleanClause.Occur.SHOULD);
        }


        // category boosting
        TermQuery termCombine = new TermQuery(new Term("bodyText", "(" + category + ")"));
        BoostQuery boostedQuery = new BoostQuery(termCombine, 2.0f);
        q.add(boostedQuery, BooleanClause.Occur.SHOULD); 
        
        // phrase queries
        BooleanQuery.setMaxClauseCount(2048);
        List<PhraseQuery> phraseQueries = buildPhraseQ(queryStr);
        for (PhraseQuery pq : phraseQueries) {
            q.add(pq, BooleanClause.Occur.SHOULD);
        }

        //  the boolean query that has all the other query types layered within
        BooleanQuery finalQuery = q.build();

        //  duplicate check + returns final results list + printing 
        List<ResultClass> ans = duplicateCheck(hitsPerPage, finalQuery);
    
        // printing the results
        System.out.format("Query '%s' in category '%s' returned:\n", queryStr, category);
        Boolean first = true;
        for (ResultClass page : ans) {
            // debugging stuff dont mind the extra parsing actually becomes like 2 lines when not debugging
            if (first) {
                if (page.DocName.get("title").equals(right)) {correct += 1;}
                else {
                    String[] options = right.split("\\|");
                    for (String o : options) { if (o.equals(page.DocName.get("title"))) { correct += 1;}}
                }
            }
            System.out.format("\t%s: %f\n", page.DocName.get("title"), page.docScore);
            first = false;
        }
    
        return ans;
    }
    

    public static void main(String[] args ) throws FileNotFoundException {
        // mvn exec:java -D"exec.mainClass=model.Query"
        Directory index;
        DirectoryReader reader;
        IndexSearcher searcher;

        // im using this for debugging throwing all the out data into answers.txt
        try (PrintStream out = new PrintStream(new FileOutputStream("answers.txt"))) {
            System.setOut(out);

            try {
                index = FSDirectory.open(Paths.get("IndexBuild"));
                reader = DirectoryReader.open(index);
                searcher = new IndexSearcher(reader);

                // $k_1$ and $k_3$ to a value between 1.2 and 2 and b = 0.75 -- random rn
                float k = 1.2f; // k being lower reduces saturation of term frequency
                float b = 0.4f; // b lower means doc length affects scoring less
                searcher.setSimilarity(new tuning(k, b));

                // read in the questions
                Scanner scanner = new Scanner(new File("questions.txt"));
                ArrayList<ArrayList<String>> questionList = readQuestions(scanner);
                Query q = new Query(searcher, new StandardAnalyzer());

                // query everything in questions.txt
                for (ArrayList<String> quest : questionList) {
                    String category = quest.get(0).toLowerCase();
                    String question = quest.get(1).toLowerCase();
                    String right = quest.get(2);
                    // GET RID OF 'RIGHT' LATER DEBUGGING PURPOSES
                    String tokenizedQuery = Tokenizer.tokenizeQuery(question);
                    List<ResultClass> ans = q.runQuery(category, tokenizedQuery, right);
                    System.out.println(quest.get(2));
                    System.out.println("\n");
                }

                System.out.println("\n\n FINAL COUNT: " + correct); // get rid of this too

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not set up searcher");
            }
        }
    }
}