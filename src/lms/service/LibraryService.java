package lms.service;

import lms.dao.BookDao;
import lms.dao.HoldDao;
import lms.dao.LoanDao;
import lms.dao.PersonDao;
import lms.exception.*;
import lms.model.Book;
import lms.model.BookStatus;
import lms.model.HoldRequest;
import lms.model.Loan;
import lms.model.Member;
import lms.model.Person;
import lms.util.ActivityLog;
import lms.util.Config;
import lms.util.Database;
import lms.util.DateTimeUtil;
import lms.util.FineCalculator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class LibraryService {

    private final Config config;
    private final BookDao bookDao = new BookDao();
    private final PersonDao personDao = new PersonDao();
    private final LoanDao loanDao = new LoanDao();
    private final HoldDao holdDao = new HoldDao();

    private static final int MAX_RENEWALS = 2;

    public LibraryService(Config config) {
        this.config = config;
    }

    public Person login(String username, String password, String expectedRole) throws InvalidCredentialsException, LMSException {
        try (Connection c = Database.open(config)) {
            Person person = personDao.findByUsername(c, username);
            if (person == null) {
                throw new InvalidCredentialsException("No account found with username: " + username);
            }
            if (!person.getPassword().equals(password)) {
                throw new InvalidCredentialsException("Incorrect password.");
            }
            if (!person.getRole().equals(expectedRole)) {
                throw new InvalidCredentialsException("This account does not belong to the " + expectedRole.toLowerCase() + " login.");
            }
            ActivityLog.log("Login: " + username + " as " + expectedRole);
            return person;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error during login.", e);
        }
    }

    public Book getBookById(int id) throws BookNotFoundException, LMSException {
        try (Connection c = Database.open(config)) {
            Book book = bookDao.findById(c, id);
            if (book == null) {
                throw new BookNotFoundException("No book found with ID " + id);
            }
            return book;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching book.", e);
        }
    }

    public List<Book> getAllBooks() throws LMSException {
        try (Connection c = Database.open(config)) {
            return bookDao.findAll(c);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching books.", e);
        }
    }

    public List<Book> searchBooks(String field, String keyword) throws LMSException {
        try (Connection c = Database.open(config)) {
            String column;
            switch (field.toLowerCase()) {
                case "title":
                    column = "title";
                    break;
                case "author":
                    column = "author";
                    break;
                case "subject":
                    column = "subject";
                    break;
                default:
                    column = "title";
            }
            return bookDao.search(c, column, keyword);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error during search.", e);
        }
    }

    public int addBook(Book b) throws DuplicateEntryException, LMSException {
        try (Connection c = Database.open(config)) {
            if (bookDao.isbnExists(c, b.getIsbn())) {
                throw new DuplicateEntryException("ISBN already exists: " + b.getIsbn());
            }
            int id = bookDao.insert(c, b);
            ActivityLog.log("Book added: " + b.getTitle() + " (ID " + id + ")");
            return id;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while adding book.", e);
        }
    }

    public void updateBook(Book b) throws BookNotFoundException, DuplicateEntryException, LMSException {
        try (Connection c = Database.open(config)) {
            Book existing = bookDao.findById(c, b.getId());
            if (existing == null) {
                throw new BookNotFoundException("No book found with ID " + b.getId());
            }
            if (!existing.getIsbn().equals(b.getIsbn()) && bookDao.isbnExists(c, b.getIsbn())) {
                throw new DuplicateEntryException("ISBN already exists: " + b.getIsbn());
            }
            bookDao.update(c, b);
            ActivityLog.log("Book updated: ID " + b.getId());
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while updating book.", e);
        }
    }

    public void deleteBook(int id) throws BookNotFoundException, OperationNotAllowedException, LMSException {
        try (Connection c = Database.open(config)) {
            Book book = bookDao.findById(c, id);
            if (book == null) {
                throw new BookNotFoundException("No book found with ID " + id);
            }
            if (book.getStatus() != BookStatus.AVAILABLE) {
                throw new OperationNotAllowedException("Cannot delete a book that is issued or reserved.");
            }
            Loan active = loanDao.findActiveByBook(c, id);
            if (active != null) {
                throw new OperationNotAllowedException("Cannot delete a book with an active loan.");
            }
            bookDao.delete(c, id);
            ActivityLog.log("Book deleted: ID " + id);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while deleting book.", e);
        }
    }

    public int registerMember(Member m) throws DuplicateEntryException, LMSException {
        try (Connection c = Database.open(config)) {
            if (personDao.usernameExists(c, m.getUsername())) {
                throw new DuplicateEntryException("Username already taken: " + m.getUsername());
            }
            int id = personDao.insert(c, m);
            ActivityLog.log("Member registered: " + m.getName() + " (ID " + id + ")");
            return id;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while registering member.", e);
        }
    }

    public List<Person> getAllMembers() throws LMSException {
        try (Connection c = Database.open(config)) {
            return personDao.findAllMembers(c);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching members.", e);
        }
    }

    public Person getMember(int id) throws MemberNotFoundException, LMSException {
        try (Connection c = Database.open(config)) {
            Person p = personDao.findById(c, id);
            if (p == null || !p.getRole().equals("MEMBER")) {
                throw new MemberNotFoundException("No member found with ID " + id);
            }
            return p;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching member.", e);
        }
    }

    public void updateMemberProfile(int id, String phone, String address) throws MemberNotFoundException, LMSException {
        try (Connection c = Database.open(config)) {
            Person p = personDao.findById(c, id);
            if (p == null || !p.getRole().equals("MEMBER")) {
                throw new MemberNotFoundException("No member found with ID " + id);
            }
            personDao.updateMemberProfile(c, id, phone, address);
            ActivityLog.log("Member profile updated: ID " + id);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while updating member profile.", e);
        }
    }

    public void changePassword(int personId, String oldPassword, String newPassword) throws InvalidCredentialsException, LMSException {
        try (Connection c = Database.open(config)) {
            Person p = personDao.findById(c, personId);
            if (p == null) {
                throw new InvalidCredentialsException("No account found with ID " + personId);
            }
            if (!p.getPassword().equals(oldPassword)) {
                throw new InvalidCredentialsException("Current password is incorrect.");
            }
            personDao.updatePassword(c, personId, newPassword);
            ActivityLog.log("Password changed for user ID " + personId);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while changing password.", e);
        }
    }

    public synchronized Loan issueBook(int bookId, int memberId) throws BookNotFoundException, MemberNotFoundException,
            BookNotAvailableException, LoanLimitExceededException, FinePendingException,
            HoldNotAllowedException, OperationNotAllowedException, LMSException {

        try (Connection c = Database.open(config)) {
            Book book = bookDao.findById(c, bookId);
            if (book == null) {
                throw new BookNotFoundException("No book found with ID " + bookId);
            }
            Person member = personDao.findById(c, memberId);
            if (member == null || !member.getRole().equals("MEMBER")) {
                throw new MemberNotFoundException("No member found with ID " + memberId);
            }

            if (book.getStatus() == BookStatus.ISSUED) {
                throw new BookNotAvailableException("'" + book.getTitle() + "' is currently issued to another member.");
            }

            HoldRequest consumedHold = null;
            if (book.getStatus() == BookStatus.RESERVED) {
                HoldRequest first = holdDao.firstPendingByBook(c, bookId);
                if (first == null) {
                } else if (first.getMemberId() != memberId) {
                    throw new BookNotAvailableException("'" + book.getTitle() + "' is reserved for member ID " + first.getMemberId() + " (hold fulfilled).");
                } else {
                    consumedHold = first;
                }
            }

            List<Loan> active = loanDao.findActiveByMember(c, memberId);
            if (active.size() >= config.getMaxLoansPerMember()) {
                throw new LoanLimitExceededException("Loan limit reached (" + config.getMaxLoansPerMember() + " books). Return a book first.");
            }
            for (Loan ln : active) {
                if (ln.getBookId() == bookId) {
                    throw new OperationNotAllowedException("This member already has this book on loan.");
                }
            }

            double unpaid = totalUnpaidFines(c, memberId);
            if (unpaid > 0) {
                throw new FinePendingException(String.format("Pending fine of Rs. %.2f must be cleared before issuing.", unpaid));
            }

            for (Loan ln : active) {
                if (ln.isOverdue()) {
                    throw new FinePendingException("Member has overdue loan(s) — return them first.");
                }
            }

            LocalDate today = DateTimeUtil.today();
            Loan loan = new Loan(bookId, memberId, today, today.plusDays(config.getLoanPeriodDays()));

            c.setAutoCommit(false);
            try {
                int loanId = loanDao.insert(c, loan);
                loan.setId(loanId);
                bookDao.updateStatus(c, bookId, BookStatus.ISSUED);
                if (consumedHold != null) {
                    holdDao.markFulfilled(c, consumedHold.getId());
                }
                c.commit();
            } catch (SQLException e) {
                try { c.rollback(); } catch (SQLException ignored) {}
                throw new OperationFailedException("Issue failed, changes rolled back.", e);
            } finally {
                c.setAutoCommit(true);
            }

            ActivityLog.log("Book " + bookId + " issued to member " + memberId + ", due " + loan.getDueDate());
            return loan;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while issuing book.", e);
        }
    }

    public synchronized double returnBook(int bookId) throws BookNotFoundException, OperationNotAllowedException, LMSException {
        try (Connection c = Database.open(config)) {
            Book book = bookDao.findById(c, bookId);
            if (book == null) {
                throw new BookNotFoundException("No book found with ID " + bookId);
            }
            Loan loan = loanDao.findActiveByBook(c, bookId);
            if (loan == null) {
                throw new OperationNotAllowedException("This book is not currently issued.");
            }

            LocalDate today = DateTimeUtil.today();
            double fine = FineCalculator.calculate(loan.getDueDate(), today, config.getFinePerDay());

            HoldRequest nextHold = null;
            c.setAutoCommit(false);
            try {
                loanDao.recordReturn(c, loan.getId(), today, fine);
                PriorityQueue<HoldRequest> queue = new PriorityQueue<>(holdDao.findPendingByBook(c, bookId));
                nextHold = queue.peek();
                if (nextHold != null) {
                    holdDao.markFulfilled(c, nextHold.getId());
                    bookDao.updateStatus(c, bookId, BookStatus.RESERVED);
                } else {
                    bookDao.updateStatus(c, bookId, BookStatus.AVAILABLE);
                }
                c.commit();
            } catch (SQLException e) {
                try { c.rollback(); } catch (SQLException ignored) {}
                throw new OperationFailedException("Return failed, changes rolled back.", e);
            } finally {
                c.setAutoCommit(true);
            }

            ActivityLog.log("Book " + bookId + " returned by member " + loan.getMemberId() + (fine > 0 ? " with fine " + FineCalculator.money(fine) : ""));
            if (nextHold != null) {
                ActivityLog.log("Hold fulfilled for member " + nextHold.getMemberId() + " — book reserved at desk.");
            }
            return fine;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while returning book.", e);
        }
    }

    public synchronized LocalDate renewLoan(int bookId) throws BookNotFoundException, OperationNotAllowedException,
            HoldNotAllowedException, FinePendingException, LMSException {

        try (Connection c = Database.open(config)) {
            Loan loan = loanDao.findActiveByBook(c, bookId);
            if (loan == null) {
                throw new OperationNotAllowedException("This book is not currently issued.");
            }
            if (loan.getDueDate().isBefore(DateTimeUtil.today())) {
                throw new OperationNotAllowedException("Loan is overdue; return the book and pay the fine first.");
            }
            if (loan.getRenewedCount() >= MAX_RENEWALS) {
                throw new OperationNotAllowedException("Renewal limit (" + MAX_RENEWALS + ") reached for this loan.");
            }
            List<HoldRequest> pending = holdDao.findPendingByBook(c, bookId);
            if (!pending.isEmpty()) {
                throw new HoldNotAllowedException("Renewal blocked — " + pending.size() + " hold request(s) pending for this book.");
            }
            double unpaid = totalUnpaidFines(c, loan.getMemberId());
            if (unpaid > 0) {
                throw new FinePendingException("Pending fine of Rs. " + String.format("%.2f", unpaid) + " must be cleared before renewing.");
            }
            LocalDate newDue = DateTimeUtil.today().plusDays(config.getLoanPeriodDays());
            loanDao.setNewDueDate(c, loan.getId(), newDue);
            ActivityLog.log("Loan " + loan.getId() + " renewed. New due date: " + newDue);
            return newDue;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while renewing loan.", e);
        }
    }

    public synchronized int placeHold(int bookId, int memberId) throws BookNotFoundException, MemberNotFoundException,
            HoldNotAllowedException, LMSException {

        try (Connection c = Database.open(config)) {
            Book book = bookDao.findById(c, bookId);
            if (book == null) {
                throw new BookNotFoundException("No book found with ID " + bookId);
            }
            Person member = personDao.findById(c, memberId);
            if (member == null || !member.getRole().equals("MEMBER")) {
                throw new MemberNotFoundException("No member found with ID " + memberId);
            }

            if (book.getStatus() == BookStatus.AVAILABLE) {
                throw new HoldNotAllowedException("Book is available — borrow it directly instead of placing a hold.");
            }
            Loan active = loanDao.findActiveByBook(c, bookId);
            if (active != null && active.getMemberId() == memberId) {
                throw new HoldNotAllowedException("This member already has this book on loan.");
            }
            if (holdDao.hasPending(c, memberId, bookId)) {
                throw new HoldNotAllowedException("This member already has a pending hold for this book.");
            }
            if (holdDao.countPendingByMember(c, memberId) >= 2) {
                throw new HoldNotAllowedException("Hold limit reached (maximum 2 pending holds per member).");
            }

            HoldRequest hold = new HoldRequest();
            hold.setBookId(bookId);
            hold.setMemberId(memberId);
            hold.setRequestDate(DateTimeUtil.today());
            holdDao.insert(c, hold);

            int position = holdDao.findPendingByBook(c, bookId).size();
            ActivityLog.log("Hold placed: member " + memberId + " for book " + bookId + " (position " + position + ")");
            return position;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while placing hold.", e);
        }
    }

    public List<HoldRequest> getMemberHolds(int memberId) throws LMSException {
        try (Connection c = Database.open(config)) {
            return holdDao.findByMember(c, memberId);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching holds.", e);
        }
    }

    public List<HoldRequest> getAllPendingHolds() throws LMSException {
        try (Connection c = Database.open(config)) {
            return holdDao.findAllPending(c);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching hold requests.", e);
        }
    }

    public List<Loan> getMemberLoans(int memberId) throws LMSException {
        try (Connection c = Database.open(config)) {
            return loanDao.findActiveByMember(c, memberId);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching member loans.", e);
        }
    }

    public double getOutstandingFines(int memberId) throws LMSException {
        try (Connection c = Database.open(config)) {
            List<Loan> unpaid = loanDao.findUnpaidFinesByMember(c, memberId);
            double total = 0;
            for (Loan l : unpaid) {
                total += l.getFineAmount();
            }
            return total;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while calculating outstanding fines.", e);
        }
    }

    public synchronized double collectFine(int memberId) throws MemberNotFoundException, OperationNotAllowedException, LMSException {
        try (Connection c = Database.open(config)) {
            Person member = personDao.findById(c, memberId);
            if (member == null || !member.getRole().equals("MEMBER")) {
                throw new MemberNotFoundException("No member found with ID " + memberId);
            }
            List<Loan> unpaid = loanDao.findUnpaidFinesByMember(c, memberId);
            if (unpaid.isEmpty()) {
                throw new OperationNotAllowedException("No pending fine for member " + memberId);
            }
            double total = 0;
            for (Loan l : unpaid) {
                loanDao.markFinePaid(c, l.getId());
                total += l.getFineAmount();
            }
            ActivityLog.log("Fine collected: " + FineCalculator.money(total) + " from member " + memberId);
            return total;
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while collecting fine.", e);
        }
    }

    public List<Loan> getActiveLoans() throws LMSException {
        try (Connection c = Database.open(config)) {
            return loanDao.findAllActive(c);
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching active loans.", e);
        }
    }

    public List<Loan> getOverdueLoans() throws LMSException {
        try (Connection c = Database.open(config)) {
            return loanDao.findOverdue(c, DateTimeUtil.today());
        } catch (SQLException e) {
            throw new OperationFailedException("Database error while fetching overdue loans.", e);
        }
    }

    public Map<String, Integer> subjectStats() throws LMSException {
        List<Book> books = getAllBooks();
        Map<String, Integer> counts = new HashMap<>();
        for (Book b : books) {
            counts.merge(b.getSubject(), 1, Integer::sum);
        }
        return new TreeMap<>(counts);
    }

    public int importBooks(String csvPath) throws OperationFailedException {
        java.io.File f = new java.io.File(csvPath);
        if (!f.exists()) {
            throw new OperationFailedException("CSV file not found: " + csvPath);
        }
        int added = 0, skipped = 0;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.toLowerCase().startsWith("isbn")) continue;
                String[] p = line.split(",", 4);
                if (p.length < 4) { skipped++; continue; }
                try (Connection c = Database.open(config)) {
                    if (bookDao.isbnExists(c, p[0].trim())) { skipped++; continue; }
                    bookDao.insert(c, new Book(0, p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), BookStatus.AVAILABLE));
                    added++;
                }
            }
        } catch (java.io.IOException | SQLException e) {
            throw new OperationFailedException("Failed reading CSV: " + e.getMessage(), e);
        }
        ActivityLog.log("CSV import: " + added + " added, " + skipped + " skipped (" + csvPath + ")");
        return added;
    }

    private double totalUnpaidFines(Connection c, int memberId) throws SQLException {
        List<Loan> unpaid = loanDao.findUnpaidFinesByMember(c, memberId);
        double total = 0;
        for (Loan l : unpaid) {
            total += l.getFineAmount();
        }
        return total;
    }
}