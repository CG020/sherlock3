package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class Index {

    static String wikiPath;
    static IndexWriter writer;
    static StandardAnalyzer analyzer;
    IndexWriterConfig config;
    static Directory index;
    File dirPath;

    public Index(String path) throws IOException {
        dirPath = new File(path);
        File[] files = dirPath.listFiles();

        analyzer = new StandardAnalyzer();
        index = new ByteBuffersDirectory();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(index, config);

        assert files != null;
        for (File file : files) {
            makeIndex(file);
        }
    }

    public static void addDoc(IndexWriter writer, String title, String categories, String summary, String text) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("title", title, Field.Store.YES));
        doc.add(new TextField("categories", categories, Field.Store.YES));
        doc.add(new TextField("summary", summary, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        writer.addDocument(doc);
    }

    private void makeIndex(File filename) {
        Scanner scanner;
        try {
            scanner = new Scanner(filename);
            String wiki = scanner.useDelimiter("\\A").next();
            Page p = new Page(wiki);
            addDoc(writer, p.title, p.categories, p.summary, p.text);
        } catch (FileNotFoundException e) {
            System.out.println("File not Found issue for " + filename);
            // e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Issue processing page for " + filename);
            // e.printStackTrace();
        }

        // - - - making notes - -
        // new wiki pages have titles in double brackets [[ stuf ]]
        // relevant categories always come after titles right after 'CATEGORIES:'
        // the summary of the page is just below that before the first header signified by ==
        // the == and === are sub page deliminators the number of = is how deep in
        // always ends with the 'see also' piece at the bottom
        // there are also these weird redirect ones though we should look for redirect
        // before making categories in case they dont exist:
        // [[Bronze age]]
        //     #REDIRECT Bronze Age [tpl]R from other capitalisation[/tpl]
        // [[ next page ]]

    }

    private List<ResultClass> queryAns (org.apache.lucene.search.Query q, int maxNoOfDocs, boolean search) throws IOException {
        List<ResultClass> retVal=new ArrayList<ResultClass>();
        int hitsPerPage = maxNoOfDocs;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        if (search) {
            searcher.setSimilarity(new ClassicSimilarity()); 
        }
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        if (docs.scoreDocs.length == 0) {
            return retVal;
        }

        for(int i=0; i < maxNoOfDocs; i++) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            ResultClass res = new ResultClass();
            res.DocName = d;
            res.docScore = hits[i].score;
            retVal.add(res);
        }
        reader.close();
        return retVal;
    }


    public static void main(String[] args ) {
        // mvn exec:java -D"exec.mainClass=model.Index"      
        try {
            wikiPath = "src\\main\\resources\\wiki-subset-20140602";
            Index indexer = new Index(wikiPath);

            writer.commit();

            // dont mind this long long query
            String querystr = "dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation";
            org.apache.lucene.search.Query q = new QueryParser("text", analyzer).parse(querystr);
            List<ResultClass> ans= indexer.queryAns(q, 10, false);

            for (ResultClass r : ans) {
                System.out.println(r.DocName.get("title") + ", " + r.docScore);
                // if (r.DocName.get("title").contains("Comic strip")) {
                //     System.out.println(r.DocName.get("text"));
                // }
            }

        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("test");
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
