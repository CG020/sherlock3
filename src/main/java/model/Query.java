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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class Query {
    IndexSearcher searcher;
    StandardAnalyzer analyzer;
    MultiFieldQueryParser multiParser;
    HashMap<String, Float> boosts = new HashMap<>();
    static int correct = 0;


    /**
     * Constructor method Query - initializes Query object with custom boosts for
     * each field in index.
     * @param searcher IndexSearcher used to query index
     * @param analyzer Lucene StandardAnalyzer 
     */
    public Query(IndexSearcher searcher, StandardAnalyzer analyzer) {
        this.searcher = searcher;
        this.analyzer = analyzer;

        // boost assignments per field
        boosts.put("summary", 1.3f);
        boosts.put("categories", 1.8f);
        boosts.put("bodyText", 1.4f);
        boosts.put("metaTitles", 1.0f);
        this.multiParser = new MultiFieldQueryParser(
            new String[]{"summary", "categories", "bodyText", "metaTitles"},
            analyzer,
            boosts
        );
    }

    /**
     * BM25Similarity configuration - tuning is a subclass that allows us to
     * define the k and b values for the BM25 Similarity measure we use to score
     */
    public static class tuning extends BM25Similarity {
        public tuning(float k, float b) {
            super(k, b);
        }
    }

    /**
     * validateQuery manipulates the parameter querystr for symbols or extra clauses
     * we want to change or edit
     * @param queryStr String to be manipulated
     * @return updated String
     */
    public String validateQuery (String queryStr) {
        String newStr;

        newStr = queryStr.trim();
        newStr = newStr.replaceAll("\"", "");
        newStr = newStr.replaceAll("!", "");
        newStr = newStr.replaceAll("\\(alex:.*?\\)", "");

        return newStr;
    }

    /**
     * readQuestions parses the questions text file and divides each query by category,
     * question, and answer
     * @param scanner Scanner object to read file
     * @return ArrayList<ArrayList<String>> arraylist of the three part of all 100 questions
     */
    public static ArrayList<ArrayList<String>> readQuestions(Scanner scanner) {
        ArrayList<ArrayList<String>> questionList = new ArrayList<>();

        ArrayList<String> temp = new ArrayList<>();
        scanner.useDelimiter("\n"); // parsing by blocks of category, question, answer
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
     * duplicateCheck ensures no duplicate documents are returned in the results + prints
     * results in formatted paragraphs
     * @param hitsPerPage results number 
     * @param finalQuery the boolean query with all the configs from runQuery
     * @return List<ResultClass>
     * @throws IOException
     */
    public List<ResultClass> duplicateCheck(int hitsPerPage, BooleanQuery finalQuery, String query) throws IOException {
        Set<String> duplicatesCheck = new HashSet<>();
        List<ResultClass> ans = new ArrayList<>();
        
        int hitCount = 0;
        while (ans.size() < hitsPerPage) {
            TopDocs pages = searcher.search(finalQuery, hitCount + hitsPerPage);
            ScoreDoc[] hits = pages.scoreDocs;
    
            // reach 10 results
            if (hitCount >= pages.totalHits) { break; }

            for (int i = hitCount; i < hits.length; i++) { //printing formatted results
                Document d = searcher.doc(hits[i].doc);
                String title = d.get("title");
                String tokenizedCategory = Tokenizer.tokenizeQuery(title); // check for duplicate results
                if (duplicatesCheck.add(title) && !query.contains(tokenizedCategory.toLowerCase())) {
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
     * buildPhraseQ constructs the phrase query sequence - phrases are every combination of sequential
     * two words in query string
     * @param queryString String of the query question
     * @param field field of Index phrase query is tested against
     * @return List<PhraseQuery> list of phrase queries 
     */
    public List<PhraseQuery> buildPhraseQ(String queryString, String field, int slop) {
        String[] words = queryString.split("\\s+");

        List<PhraseQuery> queries = new ArrayList<>();

        // iterates over all combinations building phrases
        for (int i = 0; i < words.length - 1; i++) {
            PhraseQuery.Builder builder = new PhraseQuery.Builder();
            builder.add(new Term(field, words[i]));
            builder.add(new Term(field, words[i + 1]));
            builder.setSlop(slop);
            // builder.add(new Term("bodyText", words[i + 2]));
            PhraseQuery phrase = builder.build();
            queries.add(phrase);
        }
        return queries;
    }


    /**
     * runQuery combines all Lucene query types and queries the index in one function and
     * returns the ResultClass list of matching documents
     * @param category String category of question
     * @param queryStr String question 
     * @param right String used for printing out the results of matching documents to answer
     * @return List<ResultClass> of result documents that get matched from the query
     * @throws IOException
     */
    public List<ResultClass> runQuery(String category, String queryStr, String right) throws IOException {
        BooleanQuery.Builder q = new BooleanQuery.Builder();
        int hitsPerPage = 10;
    
        // edit strings of query and category 
        queryStr = validateQuery (queryStr);
        category = validateQuery (category);
    
        // multi field parse
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
        String[] queryParts = category.split(" ");
        TermQuery termCombine;
        BoostQuery boostedQuery;
        for (String s: queryParts) {
            String tokenizedStr = Tokenizer.tokenizeQuery(s);
            termCombine = new TermQuery(new Term("bodyText", tokenizedStr));
            boostedQuery = new BoostQuery(termCombine, 1.8f);
            q.add(boostedQuery, BooleanClause.Occur.SHOULD); 
        }


        // score boost for included years or specific numbers
        String[] words = queryStr.split("\\s+");
        TermQuery year;
        BoostQuery boostYear;
        for (String s : words) {
            try {
                int num = Integer.parseInt(s);
                year = new TermQuery(new Term("bodyText", s));
                boostYear = new BoostQuery(year, 1.9f);
                q.add(boostYear, BooleanClause.Occur.SHOULD);
            } catch (NumberFormatException e) {}
        }
        
        // phrase queries -- query phrases
        BooleanQuery.setMaxClauseCount(2048);
        List<PhraseQuery> phraseQueries = buildPhraseQ(queryStr, "bodyText", 0);
        for (PhraseQuery pq : phraseQueries) {
            BoostQuery boostQuery = new BoostQuery(pq,2.5f);
            q.add(boostQuery, BooleanClause.Occur.SHOULD);
        }

        // phrase queries -- category phrases
        List<PhraseQuery> phraseCategory = buildPhraseQ(category, "bodyText", 2);
        for (PhraseQuery pc : phraseCategory) {
            BoostQuery boostQuery = new BoostQuery(pc, 1.9f);
            q.add(boostQuery, BooleanClause.Occur.SHOULD);
        }

        //  the boolean query that has all the other query types layered within
        BooleanQuery finalQuery = q.build();

        //  duplicate check + returns final results list + printing 
        List<ResultClass> ans = duplicateCheck(hitsPerPage, finalQuery, queryStr);
    
        // printing the results
        System.out.format("Query '%s' in category '%s' returned:\n", queryStr, category);
        boolean first = true;
        for (ResultClass page : ans) {
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

        // pipes result answers into answers.txt        
        try (PrintStream out = new PrintStream(new FileOutputStream("answers.txt"))) {
            System.setOut(out);

            try {
                index = FSDirectory.open(Paths.get("IndexBuild"));
                reader = DirectoryReader.open(index);
                searcher = new IndexSearcher(reader);

                // $k_1$ and $k_3$ to a value between 1.2 and 2 and b = 0.75 -- random rn
                float k = 1.9f; // k being lower reduces saturation of term frequency
                float b = 0.0f; // b lower means doc length affects scoring less
                searcher.setSimilarity(new tuning(k, b));


                // read in the questions
                Scanner scanner = new Scanner(new File("questions.txt"));
                ArrayList<ArrayList<String>> questionList = readQuestions(scanner);
                Query q = new Query(searcher, new StandardAnalyzer());

                double totalMRR = 0.0;
                int queryCount = 0;

                // query everything in questions.txt
                for (ArrayList<String> quest : questionList) {
                    String category = quest.get(0).toLowerCase();
                    String question = quest.get(1).toLowerCase();
                    String right = quest.get(2);
                    String tokenizedQuery = Tokenizer.tokenizeQuery(question);
                    List<ResultClass> ans = q.runQuery(category, tokenizedQuery, right);
                    System.out.println(quest.get(2));

                    // MRR calculation
                    double mrr = q.calculateMRR(ans, right);
                    totalMRR += mrr;
                    queryCount++;
                    System.out.println("MRR for query: " + mrr);
                    System.out.println("\n");
                }

                // copying the file to the python directory (python venv needs it)
                try {
                    Path sourcePath = Paths.get("answers.txt"); 
                    Path destinationPath = Paths.get("src", "main", "python", "answers.txt");
                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("File could not be copied to python environment.");
                }

                System.out.println("\n\n MRR: " + (totalMRR / queryCount));
                System.out.println(" P@1: " + correct);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not set up searcher");
            }
        }
    }

    public double calculateMRR(List<ResultClass> results, String correctAnswer) {
        // Get the results for the title --> check if the correct answer is within the top ranked documents
        for (int i = 0; i < results.size(); i++) {
            String[] options = correctAnswer.split("\\|");
            for (String o : options) {
                if (o.equals(results.get(i).DocName.get("title"))) {
                    return 1.0 / (i + 1); // rank starts at 1
                }
            }
        }
        return 0.0; // answer not found
    }
}