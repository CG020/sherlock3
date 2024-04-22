package model;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Index implements Serializable {

    IndexWriter writer;
    StandardAnalyzer analyzer;
    IndexWriterConfig config;
    ArrayList<Page> allPages;

    public Index(String path) throws IOException {
        // initialize
        Directory directory = FSDirectory.open(Paths.get("IndexBuild"));
        analyzer = new StandardAnalyzer();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, config);
        allPages = new ArrayList<>();

        // read in resource directory from resources
        URL dirURL = getClass().getClassLoader().getResource(path);
        if (dirURL == null) {
            System.out.println("Resource directory not found");
            System.exit(1);
        }

        // read through wiki pages directory
        File dir;
        try {
            dir = new File(dirURL.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        File[] files = dir.listFiles();
        assert files != null;

        // index the files
        System.out.println("Indexing...");
        int num = 1;
        long startTime = System.currentTimeMillis();
        for (File file : files) {
            ProgressBar.printProgressBar(num, files.length, startTime);
            readFile(file);
            num += 1;
        }
        connectRedirects();

        // add files to the index
        System.out.println("Adding to index...");
        num = 1;
        startTime = System.currentTimeMillis();
        for (Page page: allPages) {
            ProgressBar.printProgressBar(num, files.length, startTime);
            if (page.getPageType().equals("normal")) { // always true if redirects are run
                addToIndex((NormalPage) page);
            }
        }
    }

    private void readFile(File file) {
        Scanner scanner;
        try {
            // read in the whole page as one string
            scanner = new Scanner(file);
            String contents = scanner.useDelimiter("\\A").next();

            // parse the file into separate pages
            WikiPage wp = new WikiPage(contents);
            allPages.addAll(wp.getPages());
            wp.clearAllContents();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
        }
    }


    private void addToIndex(NormalPage p) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("title", p.title, Field.Store.YES));
        doc.add(new TextField("categories", p.bodyText.toString(), Field.Store.YES));
        doc.add(new TextField("summary",p.summary, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
    }


    private void connectRedirects() {
        System.out.println("Connecting redirects...");
        ArrayList<RedirectPage> redirects = new ArrayList<>();
        ArrayList<Page> normals = new ArrayList<>();
        for (Page p: allPages) {
            if (p.getPageType().equals("redirect")) {
                redirects.add((RedirectPage) p);
            } else if (p.getPageType().equals("normal")) {
                normals.add(p);
            }
        }

        int num = 1;
        long startTime = System.currentTimeMillis();
        for (RedirectPage r: redirects) {
            ProgressBar.printProgressBar(num, redirects.size(), startTime);
            for (Page n: normals) {
                NormalPage np = (NormalPage) n;
                if (r.redirect.equals(n.title)) {
                    np.categories.add(r.redirect);
                    break;
                }
            }
            num += 1;
        }
        allPages = normals;
    }


    public static void main(String[] args ) {
        try {
//            String wikiPath = "sample";
            String wikiPath = "wiki-subset-20140602";
            Index indexer = new Index(wikiPath);
        } catch (IOException e) {
            System.out.println("Failed to create Index. Check directories.");
            System.exit(1);
        }

        System.out.println("Done!");
    }

}
