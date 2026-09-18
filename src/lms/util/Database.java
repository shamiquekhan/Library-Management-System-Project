package lms.util;

import lms.dao.BookDao;
import lms.dao.HoldDao;
import lms.dao.LoanDao;
import lms.dao.PersonDao;
import lms.model.Book;
import lms.model.BookStatus;
import lms.model.Clerk;
import lms.model.HoldRequest;
import lms.model.Librarian;
import lms.model.Loan;
import lms.model.Member;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class Database {

    private final Config config;

    public Database(Config config) {
        this.config = config;
    }

    public static Connection open(Config config) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Place sqlite-jdbc jar in lib/.");
        }
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + config.getDbPath());
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
        }
        return c;
    }

    public void init() throws SQLException {
        try (Connection c = open(config); Statement st = c.createStatement()) {
            st.execute(PERSON_DDL);
            st.execute(BOOK_DDL);
            st.execute(LOAN_DDL);
            st.execute(HOLD_DDL);
            st.execute("CREATE INDEX IF NOT EXISTS idx_loan_member ON loan(member_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_hold_book ON hold_request(book_id)");
        }
    }

    public boolean isEmpty() throws SQLException {
        try (Connection c = open(config); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM book")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    public boolean seedDemoData() throws SQLException {
        try (Connection c = open(config)) {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM book")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }

            PersonDao personDao = new PersonDao();
            BookDao bookDao = new BookDao();
            LoanDao loanDao = new LoanDao();
            HoldDao holdDao = new HoldDao();

            Librarian librarian = new Librarian();
            librarian.setName("Ravi Kumar");
            librarian.setPhone("9840011122");
            librarian.setAddress("Library Block A");
            librarian.setUsername("admin");
            librarian.setPassword("admin123");
            librarian.setSalary(60000);
            librarian.setOfficeNo(101);
            personDao.insert(c, librarian);

            Clerk clerk = new Clerk();
            clerk.setName("Priya Singh");
            clerk.setPhone("9840022233");
            clerk.setAddress("Library Block B");
            clerk.setUsername("clerk");
            clerk.setPassword("clerk123");
            clerk.setSalary(28000);
            clerk.setDeskNo(5);
            personDao.insert(c, clerk);

            Member alice = new Member();
            alice.setName("Alice Fernandes");
            alice.setPhone("9840033344");
            alice.setAddress("Hostel A, Room 101");
            alice.setUsername("alice");
            alice.setPassword("alice123");
            int aliceId = personDao.insert(c, alice);

            Member bobby = new Member();
            bobby.setName("Bobby Das");
            bobby.setPhone("9840044455");
            bobby.setAddress("Hostel B, Room 205");
            bobby.setUsername("bobby");
            bobby.setPassword("bobby123");
            int bobbyId = personDao.insert(c, bobby);

            String[][] books = {
                {"9780132350884", "Clean Code", "Robert C. Martin", "PROGRAMMING"},
                {"9780134685991", "Effective Java", "Joshua Bloch", "PROGRAMMING"},
                {"9780596009205", "Head First Java", "Kathy Sierra", "PROGRAMMING"},
                {"9780073525624", "Database System Concepts", "Abraham Silberschatz", "DATABASES"},
                {"9780321173840", "JDBC API Tutorial and Reference", "Maydene Fisher", "DATABASES"},
                {"9780132876201", "Computer Networking", "James Kurose", "NETWORKING"},
                {"9781119800361", "Operating System Concepts", "Abraham Silberschatz", "OPERATING SYSTEMS"},
                {"9781292401133", "Artificial Intelligence A Modern Approach", "Stuart Russell", "ARTIFICIAL INTELLIGENCE"}
            };
            int[] bookIds = new int[books.length];
            for (int i = 0; i < books.length; i++) {
                Book b = new Book(0, books[i][0], books[i][1], books[i][2], books[i][3], BookStatus.AVAILABLE);
                bookIds[i] = bookDao.insert(c, b);
            }

            LocalDate today = LocalDate.now();
            Loan normal = new Loan(bookIds[2], aliceId, today.minusDays(10), today.plusDays(4));
            loanDao.insert(c, normal);
            bookDao.updateStatus(c, bookIds[2], BookStatus.ISSUED);

            Loan overdue = new Loan(bookIds[4], aliceId, today.minusDays(25), today.minusDays(11));
            loanDao.insert(c, overdue);
            bookDao.updateStatus(c, bookIds[4], BookStatus.ISSUED);

            HoldRequest hold = new HoldRequest();
            hold.setBookId(bookIds[4]);
            hold.setMemberId(bobbyId);
            hold.setRequestDate(today.minusDays(2));
            holdDao.insert(c, hold);
            return true;
        }
    }

    private static final String PERSON_DDL = "CREATE TABLE IF NOT EXISTS person ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "name TEXT NOT NULL,"
        + "phone TEXT,"
        + "address TEXT,"
        + "username TEXT UNIQUE,"
        + "password TEXT,"
        + "role TEXT NOT NULL,"
        + "salary REAL,"
        + "desk_no INTEGER,"
        + "office_no INTEGER"
        + ")";

    private static final String BOOK_DDL = "CREATE TABLE IF NOT EXISTS book ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "isbn TEXT UNIQUE NOT NULL,"
        + "title TEXT NOT NULL,"
        + "author TEXT NOT NULL,"
        + "subject TEXT NOT NULL,"
        + "status TEXT NOT NULL"
        + ")";

    private static final String LOAN_DDL = "CREATE TABLE IF NOT EXISTS loan ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "book_id INTEGER NOT NULL,"
        + "member_id INTEGER NOT NULL,"
        + "issue_date TEXT NOT NULL,"
        + "due_date TEXT NOT NULL,"
        + "return_date TEXT,"
        + "fine_amount REAL NOT NULL DEFAULT 0,"
        + "fine_paid INTEGER NOT NULL DEFAULT 0,"
        + "renewed_count INTEGER NOT NULL DEFAULT 0,"
        + "FOREIGN KEY (book_id) REFERENCES book(id),"
        + "FOREIGN KEY (member_id) REFERENCES person(id)"
        + ")";

    private static final String HOLD_DDL = "CREATE TABLE IF NOT EXISTS hold_request ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "book_id INTEGER NOT NULL,"
        + "member_id INTEGER NOT NULL,"
        + "request_date TEXT NOT NULL,"
        + "fulfilled INTEGER NOT NULL DEFAULT 0,"
        + "FOREIGN KEY (book_id) REFERENCES book(id),"
        + "FOREIGN KEY (member_id) REFERENCES person(id)"
        + ")";
}