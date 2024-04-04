package model;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class Indexer {

    IndexWriter writer;
    StandardAnalyzer analyzer;
    IndexWriterConfig config;
    Directory index;
    File dirPath;

    public Indexer(String path) throws IOException {
        dirPath = new File(path);
        File[] files = dirPath.listFiles();

        analyzer = new StandardAnalyzer();
        index = new ByteBuffersDirectory();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(index, config);

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
        System.out.println("parsing stuff");
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
            String wikiPath = "../resoureces/wiki-subset-20140602";
            Indexer indexer = new Indexer(wikiPath);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("test");
    }

}
