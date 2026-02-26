import java.io .*;
import java.util.List;

/*
 * FileReaderThread is responsible for reading book entries from a given file
 * and adding them to a shared catalog list.
 * This class implements the Runnable interface so it can be executed inside
 * a separate thread, allowing the file-reading process to run concurrently.
 */

public class FileReaderThread implements Runnable {
    private File file;
    private List<Book> catalog;

    /*
    * Constructor for FileReaderThread.
    * @param catalog The shared list that will hold the book entries read from the file.
    */
    public FileReaderThread(List<Book> catalog, File file) {
        this.catalog = catalog;
        this.file = file;
    }

    /*
    * Reads the book entries from the specified file and adds them to the shared catalog list.
    * Each line in the file is expected to be in the format: Title:Author:ISBN:NumberOfCopies
    */
    @Override
    public void run() {
        System.out.println("FileReaderThread started...");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                
                if (parts.length != 4) {
                    continue; // Skip malformed entries so the thread can continue processing the rest of the file without broken by a single bad line
                }
                String title = parts[0].trim();
                String author = parts[1].trim();
                String isbn = parts[2].trim();
                int numCopies = Integer.parseInt(parts[3].trim());

                catalog.add(new Book(title, author, isbn, numCopies));
            }
        } catch (Exception e) {
            System.err.println("IO Error while reading file: " + e.getMessage());
            LibraryBookTracker.logError(file, "IO Error: " + e.getMessage());
        }
        System.out.println("FileReaderThread finished reading file.");
    }
    
}
