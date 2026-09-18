package lms.model;

public class Book implements Displayable, Comparable<Book> {

    private int id;
    private String isbn;
    private String title;
    private String author;
    private String subject;
    private BookStatus status;

    public Book() {
    }

    public Book(int id, String isbn, String title, String author, String subject, BookStatus status) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.subject = subject;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    @Override
    public int compareTo(Book o) {
        return this.title.compareToIgnoreCase(o.title);
    }

    @Override
    public void display() {
        System.out.printf("  ID: %-4d  ISBN: %-16s  Title: %-34s  Author: %-22s  Subject: %-24s  Status: %s%n",
            id, isbn, title, author, subject, status);
    }

    @Override
    public String toString() {
        return title + " by " + author + " (" + subject + ")";
    }
}