public class Book {
    private String title;
    private String author;
    private String isbn;
    private int numCopies;

    public Book(String title, String author, String isbn, int numCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.numCopies = numCopies;
    }
    
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getNumCopies() {
        return numCopies;
    }
    
    //this method is for displaying the book information in a readable format when printed
    @Override
    public String toString() {
        return String.format("(title: %-30s) (author: %-20s) (ISBN: %-13s)  %d", title, author, isbn, numCopies);
    }

    //this method is for writing the book information back to the file in the same format as it requires
    public String toFileString() {
        return title + ":" + author + ":" + isbn + ":" + numCopies;
    }
}
