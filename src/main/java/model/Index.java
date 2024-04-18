package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

    IndexWriter writer;
    static StandardAnalyzer analyzer;
    IndexWriterConfig config;
    Directory index;
    ArrayList<Page> allPages;

    public Index(String path) throws IOException {
        allPages = new ArrayList<>();
        File[] files;
        URL dirURL = getClass().getClassLoader().getResource(path);
        if (dirURL != null) {
            File dir;
            try {
                dir = new File(dirURL.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            files = dir.listFiles();

        } else {
            throw new IOException("Directory not found");
        }

        analyzer = new StandardAnalyzer();
        index = new ByteBuffersDirectory();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(index, config);

        assert files != null;
        System.out.println("Indexing...");
        int num = 1;
        long startTime = System.currentTimeMillis();
        for (File file : files) {
            ProgressBar.printProgressBar(num, files.length, startTime);
//            ProgressBar.printMemoryUsage();
            makeIndex(file);
            num += 1;
        }
//        System.out.println("Connecting redirects...");
//        connectRedirects();
        writer.commit();
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
            // read in the whole page as one string
            scanner = new Scanner(filename);
            String contents = scanner.useDelimiter("\\A").next();

            // parse the file into separate pages
            WikiPage wp = new WikiPage(contents);
            allPages.addAll(wp.getPages());
//            for (Page p : wp.getPages()) {
//                if (p.pageType.equals("standard")) {
//                    allPages.add(p);
//                    addDoc(writer, p.title, p.categories, p.summary, p.text);
//                }
//                writer.commit();
//            }

        } catch (FileNotFoundException e) {
            System.out.println("File not Found issue for " + filename);
            // e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Issue processing page for " + filename);
            // e.printStackTrace();
        }

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

        for (int i = 0; i < maxNoOfDocs; i++) {
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

    // TODO this function takes about 7 mins to run on MY computer and I don't
    // even think it changes the actual index. Sooooo, kinda pointless?
    private void connectRedirects() {
        ArrayList<RedirectPage> redirects = new ArrayList<>();
        ArrayList<NormalPage> normals = new ArrayList<>();
        for (Page p: allPages) {
            if (p.getPageType().equals("redirect")) {
                redirects.add((RedirectPage) p);
            } else if (p.getPageType().equals("normal")) {
                normals.add((NormalPage) p);
            }
        }

        int num = 1;
        long startTime = System.currentTimeMillis();
        for (RedirectPage r: redirects) {
            ProgressBar.printProgressBar(num, redirects.size(), startTime);
            for (NormalPage n: normals) {
                if (r.redirect.equals(n.title)) {
                    n.categories.add(r.redirect);
                    break;
                }
            }
            num += 1;
        }
    }


    public static void main(String[] args ) {
        // mvn exec:java -D"exec.mainClass=model.Index"
        try {
//            String wikiPath = "sample";
            String wikiPath = "wiki-subset-20140602";
            Index indexer = new Index(wikiPath);

//            // dont mind this long long query
//            String querystr = "dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation";
//            org.apache.lucene.search.Query q = new QueryParser("text", analyzer).parse(querystr);
//            List<ResultClass> ans= indexer.queryAns(q, 10, false);
//
//            for (ResultClass r : ans) {
//                System.out.println(r.DocName.get("title") + ", " + r.docScore);
//                // if (r.DocName.get("title").contains("Comic strip")) {
//                //     System.out.println(r.DocName.get("text"));
//                // }
//            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Done!");

    }

}
