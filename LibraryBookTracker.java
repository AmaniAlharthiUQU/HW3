import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LibraryBookTracker {
    private static int validRecords = 0;
    private static int searchResults = 0;
    private static int booksAdded = 0;
    private static int errorsEncountered = 0;


    public static void main(String[] args) {
        
        try {

            if (args.length < 2) {
                throw new InsufficientArgumentsException("Tow arguments required");
            }

            String fileName = args[0];
            String command = args[1];

            if (!fileName.endsWith(".txt")) {
                throw new InvalidFileNameException("File name must end with .txt");
            }

            
            File file = new File(fileName);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
            file.createNewFile();
            }
            
            List<Book> catalog = new ArrayList<>();
            Thread fileReaderThread = new Thread(new FileReaderThread(catalog, file));
            Thread operationAnalyzerThread = new Thread(new OperationAnalyzerThread(catalog, command, file));

            fileReaderThread.start();
            fileReaderThread.join();

            validRecords = catalog.size();

            operationAnalyzerThread.start();
            operationAnalyzerThread.join();
   
        }
        catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            errorsEncountered++;
            logError(new File(args[0]), e.getMessage());
        }
        finally {
            System.out.println("Valid records processed: " + validRecords);
            System.out.println("Search results found: " + searchResults);
            System.out.println("Books added: " + booksAdded);
            System.out.println("Errors encountered: " + errorsEncountered);
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    
    /*
    This method reads book entries from a specified file and returns a list of Book objects.
    Each line in the file is expected to be in the format: Title:Author:ISBN:NumberOfCopies
    @param file The file to read book entries from.
    @return A list of Book objects read from the file.
    @throws IOException If an I/O error occurs while reading the file.
    @throws MalformedBookEntryException If a line in the file does not conform to the expected format.
    @throws InvalidISBNException If a line contains an ISBN that is not a 13-digit number.
    */
    public static List<Book> readBooks(File file) throws IOException, MalformedBookEntryException, InvalidISBNException {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length != 4) {
                    throw new MalformedBookEntryException("Malformed book entry: " + line);
                }
                String title = parts[0].trim(); // .trim() to remove leading and trailing whitespace
                String author = parts[1].trim();
                String isbn = parts[2].trim();
                if (!isbn.matches("\\d{13}")) { 
                    throw new InvalidISBNException("Invalid ISBN: " + isbn);
                }
                int numCopies = 0;
                try {
                    numCopies = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException e) {
                    throw new MalformedBookEntryException("Invalid number of copies: " + parts[3].trim());
                }
                books.add(new Book(title, author, isbn, numCopies));
                validRecords++;
            }
        }
        return books;
    }


    /*
    This method searches for books in the provided list by their ISBN number.
    @param books The list of books to search through.
    @param isbn The ISBN number to search for.
    @return The number of books found with the specified ISBN.
    @throws DuplicateISBNException If more than one book is found with the same ISBN, indicating a data integrity issue in the catalog.
    */
    public static int searchByISBN(List<Book> books, String isbn) throws DuplicateISBNException {
        System.out.printf("%-30s %-20s %-15s %-5s%n", "Title", "Author", "ISBN", "Copies");

        Book foundBook = null;
        int results = 0;
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
              if (foundBook != null) {
                throw new DuplicateISBNException("Duplicate ISBN found: " + isbn);
              }
               foundBook = book;
               results++;

            }
       }
       if (foundBook != null) {
            System.out.printf("%-30s %-20s %-15s %-5d%n", foundBook.getTitle(), foundBook.getAuthor(), foundBook.getIsbn(), foundBook.getNumCopies());   
            searchResults++; 
        } else {
             System.out.println("No book found with ISBN: " + isbn);
      }
        return results;
    }
    

    /*
    This method searches for books in the provided list by their title, allowing for partial matches.
    @param books The list of books to search through.
    @param title The title or partial title to search for.
    @return The number of books found that match the specified title.
    */
    public static int searchByTitle(List<Book> books, String title) {
        System.out.printf("%-30s %-20s %-15s %-5s%n", "Title", "Author", "ISBN", "Copies");
        int results = 0;
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title.toLowerCase())) {
                 System.out.printf("%-30s %-20s %-15s %-5d%n",
                        book.getTitle(), book.getAuthor(), book.getIsbn(), book.getNumCopies());
                searchResults++;
                results++;
            }
        }
        if (results == 0) {
            System.out.println("No book found with title: " + title);
        }
        return results;
    }


    /*
    This method adds a new book to the provided list of books based on a record string, and then updates the catalog file with the new list of books.
    @param books The list of books to add the new book to.
    @param record The string containing the book information in the format: Title:Author:ISBN:NumberOfCopies
    @param file The catalog file to update with the new list of books.
    @return The number of books added (1 if successful, 0 otherwise).
    @throws IOException If an I/O error occurs while writing to the file.
    @throws MalformedBookEntryException If the record string does not conform to the expected format or contains invalid data.
    @throws InvalidISBNException If the ISBN in the record string is not a 13-digit number.
    */
    public static int addBook(List<Book> books, String record, File file) throws IOException, MalformedBookEntryException, InvalidISBNException {
        String[] parts = record.split(":");
        if (parts.length != 4) throw new MalformedBookEntryException("Malformed book entry: " + record);

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        String copies = parts[3].trim();

        if (title.isEmpty() || author.isEmpty()) throw new MalformedBookEntryException("Title and author cannot be empty");
        if (!isbn.matches("\\d{13}")) throw new InvalidISBNException("ISBN must be a 13-digit number");
        if (!copies.matches("\\d+") || Integer.parseInt(copies) <= 0)
            throw new MalformedBookEntryException("Number of copies must be greater than 0");

        books.add(new Book(title, author, isbn, Integer.parseInt(copies)));
        booksAdded++;
        Collections.sort(books, Comparator.comparing(Book::getTitle));

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (Book b : books) {
            writer.write(b.toFileString());
            writer.newLine();
        }
        writer.close();

        System.out.println("Book added successfully: " + record);
        int numCopies = Integer.parseInt(copies);
        System.out.printf("%-30s %-20s %-15s %-5d%n", title, author, isbn, numCopies);
        return 1;
    }


    /*
    This method logs error messages to an "errors.log" file located in the same directory as the catalog file. Each log entry includes a timestamp and the error message.
    @param catalogFile The catalog file whose directory will be used to store the log file. 
    @param message The error message to log.
    */
    public static void logError(File catalogFile, String message) {
        try {
            File logFile = new File(catalogFile.getParent(), "errors.log");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = dtf.format(LocalDateTime.now());
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
                bw.write("[" + timestamp + "] " + message);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write log.");
        }
    }
}

    

