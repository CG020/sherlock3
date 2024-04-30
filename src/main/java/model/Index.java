/*
 * Katelyn Rohrer, Camila Grubb, Lydia Dufek
 * CSC 483/583
 * Defines the Index class, which does everything related to indexing:
 * reads in a directory and parses each file into several pages,
 * then adds each of the pages into the index. The index is saved to
 * a directory within the project for easy querying. 
 */
package model;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;


/**
 * Class to define the Index object. The Index object reads in the 
 * directory of wikipedia files, parses them, then adds to the index.
 * Relevant methods include addToIndex, which adds a document to the index,
 * connectRedirects, which connects each of the redirect pages to its
 * corresponding normal page, printProgressBar, which is used to keep track
 * of time, and main, which creates the actual Index object.
 */
public class Index {

    IndexWriter writer;
    Analyzer analyzer;
    IndexWriterConfig config;
    ArrayList<Page> allPages;

    /**
     * Creates the Index object and controls all of the functionality
     * of adding a given directory to the Index. Imports a given folder based
     * on the string input path, parses through the folder, then adds each
     * of the pages to the index.
     * @param path String name of the folder within resources to be read in.
     * @throws IOException If the folder does not exist within reaources.
     */
    public Index(String path) throws IOException {
        // initialize
        Directory directory = FSDirectory.open(Paths.get("IndexBuild"));
        analyzer = new WhitespaceOnlyAnalyzer();
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(directory, config);
        allPages = new ArrayList<>();

        // read in resource directory from resources
        URL dirURL = getClass().getClassLoader().getResource(path);
        if (dirURL == null) {
            throw new IOException();
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

        // read in the wiki pages
        System.out.println("Reading in files...");
        int num = 1;
        long startTime = System.currentTimeMillis();
        for (File file : files) {
            printProgressBar(num, files.length, startTime);
            readFile(file);
            num += 1;
        }
        connectRedirects();

        // add files to the index
        System.out.println("Adding " + allPages.size() + " to index...");
        num = 1;
        startTime = System.currentTimeMillis();
        for (Page page: allPages) {
            printProgressBar(num, allPages.size(), startTime);
            addToIndex((NormalPage) page);
            num += 1;
        }
    }

    /**
     * Reads in a given file and creates a WikiFile object out of it.
     * The WikiFile object further parses the file contents into relevant
     * Page objects, which are added to allPages.
     * @param file File object to be read through
     */
    private void readFile(File file) {
        Scanner scanner;
        try {
            // read in the whole page as one string
            scanner = new Scanner(file);
            String contents = scanner.useDelimiter("\\A").next();

            // parse the file into separate pages
            WikiFile wp = new WikiFile(contents);
            allPages.addAll(wp.getPages());
            wp.clearAllContents();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + file);
        }
    }


    /**
     * Adds a NormalPage object to the Index by creating a document and adding
     * the relevant fields to it.
     * @param p NormalPage object to be added to the doc
     * @throws IOException Committing to the writer can result in an IO error
     * if something goes wrong with the files during indexing
     */
    private void addToIndex(NormalPage p) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("title", p.title, Field.Store.YES));
        doc.add(new TextField("categories", Tokenizer.tokenizeCategories(p.categories), Field.Store.YES));
        doc.add(new TextField("headers", Tokenizer.tokenizeHeaders(p.headers), Field.Store.YES));
        doc.add(new TextField("summary",Tokenizer.tokenizeSummary(p.summary), Field.Store.YES));
        doc.add(new TextField("metaTitles", Tokenizer.tokenizeMetaTitles(p.metaTitles), Field.Store.YES));
        doc.add(new TextField("bodyText", Tokenizer.tokenizeBodyText(p.bodyText), Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
    }


    /**
     * Connects all of the redirect pages to their correlating
     * normal pages by first sorting through all of the files
     * and extracting out the redirects and normals. Then, for each
     * of the redirects, search for the corresponding normal page.
     * This function takes several minutes to run.
     */
    private void connectRedirects() {
        // divide all pages into redirects list and normals list
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

        // for each of the redirect pages, search for the correlating normal page
        int num = 1;
        long startTime = System.currentTimeMillis();
        for (RedirectPage r: redirects) {
            printProgressBar(num, redirects.size(), startTime);
            for (Page n: normals) {
                NormalPage np = (NormalPage) n;
                // if we find a match, add the redirect string to title categories
                if (r.redirect.equals(n.title)) {
                    np.categories.add(r.title);
                    break;
                }
            }
            num += 1;
        }
        allPages = normals;
    }

    /**
     * Prints a progress bar for whatever task is currently being run.
     * Useful for longer running tasks such as connectRedirects and especially
     * for adding the documents to the index. 
     * @param completed The number of tasks that have been completed so far
     * (should be incremented on each call to the progress bar)
     * @param total The total number of iterations needed to be completed
     * @param startTime The time in ms that the task began at. Used to calculate
     * time elapsed and estimated time remaining
     */
    private static void printProgressBar(int completed, int total, long startTime) {
        int width = 50;
        int progressPercentage = (100 * completed) / total;
        int progress = (width * completed) / total;

        // calculate the time and convert to human readable format
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - startTime; // in milliseconds
        long estimatedTotalTime = (timeElapsed / completed) * total;
        long estimatedTimeLeft = estimatedTotalTime - timeElapsed;

        String timeLeftFormatted = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(estimatedTimeLeft),
                TimeUnit.MILLISECONDS.toSeconds(estimatedTimeLeft) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTimeLeft)));

        String elapsedTimeFormatted = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(timeElapsed),
                TimeUnit.MILLISECONDS.toSeconds(timeElapsed) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeElapsed)));

        // display the actual bar
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < progress) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ").append(progressPercentage).append("% ");
        bar.append("Elapsed: ").append(elapsedTimeFormatted).append(", ");
        bar.append("Left: ").append(timeLeftFormatted);

        // return the cursor to the beginning of the line
        System.out.print("\r");
        System.out.print(bar);

        if (completed == total) {
            System.out.println("\nCompleted!");
        }
    }


    public static void main(String[] args ) {
        try {
            String wikiPath = "wiki-subset-20140602";
            new Index(wikiPath);
        } catch (IOException e) {
            System.out.println("Failed to create Index. Check directories.");
            System.exit(1);
        }

        System.out.println("Done!");
    }

}

/**
 * Class to define the WhiteSpace analyzer, which simply splits up the text
 * on whitespace. Used when adding documents to the index, since the document
 * text is already tokenized and simply divided by whitespace.
 */
class WhitespaceOnlyAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new WhitespaceTokenizer());
    }
}

