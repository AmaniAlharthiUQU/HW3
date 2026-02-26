import java.io.File;
import java.util.List;

/*
 * OperationAnalyzerThread is responsible for analyzing the command provided by the user
 * and performing the appropriate operation on the shared catalog list.
 */
public class OperationAnalyzerThread implements Runnable {
    private List<Book> catalog;
    private String command;
    private File catalogFile;

    /*
    * Constructor for OperationAnalyzerThread.
    * @param catalog The shared list of books that the thread will operate on.
    * @param command The command provided by the user, which can be an ISBN for searching, a title for searching, or a book record for adding.
    * @param catalogFile The file representing the catalog, used for logging errors when adding a
    */
    public OperationAnalyzerThread(List<Book> catalog, String command, File catalogFile) {
        this.catalog = catalog;
        this.command = command;
        this.catalogFile = catalogFile;
    }

    /*
    * Analyzes the command and performs the appropriate operation on the catalog list.
    * If the command is a 13-digit number, it is treated as an ISBN and the searchByISBN method is called.
    * If the command contains a colon, it is treated as a book record and the addBook method is called.
    * Otherwise, it is treated as a title and the searchByTitle method is called.
    * Any exceptions that occur during the processing of the command are caught and logged to the catalog file.
    */
    @Override
    public void run() {
        System.out.println("OperationAnalyzerThread started...");
        try { 
            if (command.matches("\\d{13}"))
                LibraryBookTracker.searchByISBN(catalog, command);

            else if (command.contains(":"))
                LibraryBookTracker.addBook(catalog, command, catalogFile);
            else 
                LibraryBookTracker.searchByTitle(catalog, command);

        } catch (Exception e) {
            System.err.println("Error processing command: " + e.getMessage());
            LibraryBookTracker.logError(catalogFile, "Error processing command: " + e.getMessage());
        }
        System.out.println("OperationAnalyzerThread finished processing command.");
    }
}
