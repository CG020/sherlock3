package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class Index {

    IndexWriter writer;
    StandardAnalyzer analyzer;
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

    public static void main(String[] args ) {
        try {
            String wikiPath = "src\\main\\resources\\wiki-subset-20140602";
            Index indexer = new Index(wikiPath);
            
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("test");
    }

}
