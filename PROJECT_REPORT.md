# 1. Cover Page

**Library Management System**

**Project Report — Programming in Java (Flipped Course)**

| | |
|---|---|
| **Student** | Shamique Khan |
| **Registration Number** | 25BAI10187 |
| **Programme** | B.Tech — Artificial Intelligence and Machine Learning |
| **Course** | Programming in Java (Flipped Course) |
| **Platform** | VITyarthi |
| **Academic Year** | 2026–2027 |
| **Repository** | https://github.com/shamiquekhan/Library-Management-System |
| **Submission Date** | September 2026 |

---

# 2. Introduction

This project implements a **Library Management System (LMS)** as a Java desktop application developed for the *Programming in Java* flipped course. The default interface is a **Java Swing GUI** (light-themed dashboard with role-aware navigation); the original terminal workflow remains available via `--cli`. The system supports three user roles (Librarian, Clerk, Member), persists all data in an embedded SQLite database through JDBC, and runs two scheduled background services (overdue monitoring and automated backup) using the Concurrency APIs.

Every major topic of the five course modules is applied in a real domain: Java fundamentals and file I/O, object-oriented design, a custom exception hierarchy, the Collections Framework, and JDBC transactions alongside scheduled concurrency — plus the GUI concepts of event handling, layout managers and `SwingWorker` concurrency. The application is built and launched entirely from the command line (`./build.sh`, `./run.sh`) with no external database server and no web/frontend framework, so an evaluator can build and run it immediately after cloning.

---

# 3. Problem Statement

Libraries manage a continuous cycle of cataloguing, circulation and accountability: books are added and withdrawn, members join, books are issued and returned, popular titles accumulate hold queues, overdue loans attract fines, and administrators need statistics, reports and safe backups.

Managing this cycle by hand is slow and error-prone: due dates are miscounted, fines are calculated inconsistently, hold queues are lost, and no audit trail exists. Spreadsheet-style records share none of the validation, transactional consistency or role-based access that a purpose-built system provides.

This project delivers a digital solution that:

1. Automates cataloguing, membership, circulation, holds and fine calculation with enforced business rules.
2. Provides role-appropriate interfaces for Librarian, Clerk and Member.
3. Keeps all records durable in a relational database with transactional updates.
4. Produces reports, statistics, logs and backups without manual effort.

---

# 4. Objectives

- Apply Java syntax, control structures, strings, arrays and I/O fundamentals in a realistic domain.
- Design an object-oriented model using inheritance, abstraction, polymorphism, encapsulation, interfaces and enums.
- Implement a custom checked exception hierarchy with validation and error propagation at every layer.
- Use file I/O (`BufferedReader`/`BufferedWriter`, `PrintWriter`, `Properties`) for configuration, reports, logs, backup manifests and CSV import.
- Select and justify Collections Framework structures (List, Map, Set, Queue, Comparator) for each data-management need.
- Build a JDBC application over SQLite using `PreparedStatement`, generated keys and explicit transactions.
- Demonstrate concurrency with `ScheduledExecutorService` daemon tasks and `synchronized` service methods.
- Deliver a reproducible automated test suite alongside the manual test evidence.

---

# 5. Functional Requirements

The system provides five major functional modules, exceeding the required minimum of three. Each requirement lists its inputs, processing and outputs.

### FR-01 — Authentication & Role Management
- **Input:** username, password, selected role (Librarian / Clerk / Member).
- **Processing:** credential verification against the `person` table, role match enforcement, password change with old-password verification.
- **Output:** role-specific console interface; `InvalidCredentialsException` on failure.

### FR-02 — Book Catalog Management
- **Input:** ISBN, title, author, subject; search keyword; CSV file path.
- **Processing:** add / update / delete with duplicate-ISBN and status checks (issued or reserved books cannot be deleted); search by title, author or subject; batch CSV import with per-record validation.
- **Output:** updated catalog, table listings, import summary (`added` / `skipped` counts).

### FR-03 — Circulation Management
- **Input:** book ID, member ID.
- **Processing:** issue (loan limit 3, no unpaid fines, no overdue loans, hold-queue enforcement on reserved books), return (fine calculation at Rs. 5/day, hold fulfilment via `PriorityQueue` ordered by request date), renew (max 2 renewals, blocked by overdue status or pending holds), place hold (max 2 pending holds per member, not for available or self-borrowed books), collect fine.
- **Output:** loan records with due dates, fine amounts, hold queue positions, payment confirmations.

### FR-04 — Reporting & Statistics
- **Input:** current database records.
- **Processing:** export books and members as CSV, active loans as TXT with timestamped filenames; aggregate per-subject counts in a `TreeMap`.
- **Output:** `reports/books_report_<timestamp>.csv`, `reports/members_report_<timestamp>.csv`, `reports/loans_report_<timestamp>.txt`, on-screen statistics.

### FR-05 — Automated Services & Administration
- **Input:** configured intervals (`overdue.check.seconds`, `backup.interval.seconds`), member registration data.
- **Processing:** scheduled overdue scans refresh fines on late loans; scheduled backups copy the SQLite file and append a manifest; member registration with username-uniqueness enforcement.
- **Output:** updated fine records, `backups/library-backup-<timestamp>.db` + `backup-manifest.txt`, timestamped activity log entries.

---

# 6. Non-Functional Requirements

### 6.1 Performance
Common operations (book search, member lookup, issue, return, reporting) complete in milliseconds. SQL indexes exist on `loan(member_id)` and `hold_request(book_id)`; in-memory aggregation uses `HashMap`/`TreeMap`; SQLite runs embedded with zero server overhead.

### 6.2 Reliability
Circulation operations run inside JDBC transactions: `setAutoCommit(false)`, `commit()` on success, `rollback()` on failure, so an issue or return can never leave a partially updated database. All streams and connections are managed with try-with-resources.

### 6.3 Security
Access is role-separated: Librarian, Clerk and Member each see only their own menu. All SQL uses `PreparedStatement` (no string concatenation). Password storage in plaintext is a documented limitation (Section 16), with hashing listed as a future enhancement.

### 6.4 Usability
Menu-driven CLI with numbered choices, range-validated numeric input (re-prompts on invalid values), blank-keeps-current-value updates, descriptive `[ok]` / `[x]` messages for every operation, and seeded demo data so a first run is immediately explorable.

### 6.5 Maintainability
Layered package structure — `ui` → `service` → `dao` → database — with cross-cutting utilities (`Config`, `ActivityLog`, `FineCalculator`) isolated in `lms.util`. Each layer depends only on the layer below, so components can be understood, tested and modified independently.

### 6.6 Error Handling
A custom checked exception hierarchy rooted at `LMSException` with 10 domain-specific subclasses (`BookNotFoundException`, `MemberNotFoundException`, `BookNotAvailableException`, `LoanLimitExceededException`, `FinePendingException`, `HoldNotAllowedException`, `InvalidCredentialsException`, `DuplicateEntryException`, `OperationNotAllowedException`, `OperationFailedException`). Chaining via `initCause` preserves the underlying `SQLException`; multi-catch (`IOException | SQLException`) is used in CSV import; the UI layer prints messages without exposing stack traces.

### 6.7 Logging & Monitoring
Every significant event (login, issue, return, renewal, hold, fine, import, backup, monitor scans) is written to `logs/lms.log` with a timestamp and echoed to the console. The overdue monitor logs each scan with the number of overdue loans.

### 6.8 Resource Efficiency
Both background services run as daemon threads on a single-threaded `ScheduledExecutorService`, so they never block the console loop and the JVM exits cleanly.

---

# 7. System Architecture

Three-layer architecture with cross-cutting services:

```
┌──────────────────────────────────────────────┐
│  Console UI   ConsoleUI, LibrarianConsole,   │
│               ClerkConsole, MemberConsole    │
├──────────────────────────────────────────────┤
│  LibraryService  business rules, validation, │
│                  synchronized operations     │
├──────────────────────────────────────────────┤
│  DAO Layer   BookDao, PersonDao, LoanDao,    │
│              HoldDao (JDBC / PreparedStatement)
├──────────────────────────────────────────────┤
│  SQLite database (embedded file)             │
└──────────────────────────────────────────────┘
    Cross-cutting: Config, ActivityLog, ReportGenerator,
    OverdueMonitor, BackupService, FineCalculator, DateTimeUtil
```

Each layer communicates only with the layer beneath it. The UI catches `LMSException` and prints messages; the service layer contains all business rules; the DAO layer contains all SQL. `Database.open(config)` provides connections with `PRAGMA foreign_keys=ON`.

### Package structure

```
lms
├── Main              entry point, bootstrapping
├── model/     (10)   Person hierarchy, Book, Loan, HoldRequest, BookStatus, Displayable
├── exception/ (11)   LMSException + 10 subclasses
├── dao/       (4)    BookDao, PersonDao, LoanDao, HoldDao
├── service/   (1)    LibraryService
├── reports/   (1)    ReportGenerator
├── concurrency/(2)   OverdueMonitor, BackupService
├── util/      (7)    Console, Config, Database, DateTimeUtil, FineCalculator, ActivityLog, TablePrinter
└── ui/        (4)    ConsoleUI, LibrarianConsole, ClerkConsole, MemberConsole
```

**Total: 41 Java source files.**

---

# 8. Design Diagrams

The diagrams below are included as text for the report; rendered versions are maintained in `design/*.md` and `images/` in the repository.

### 8.1 Use Case Diagram

```
Actors: Librarian, Clerk, Member

Librarian ---- Add/Update/Remove Book, Import CSV, Register Member,
               View Members/Loans/Overdue, Subject Statistics,
               Generate Reports, Backup Database, Change Password
Clerk -------  Issue Book, Return Book, Renew Loan, Collect Fine,
               Register Member, Search/View Books, View Member Loans
               & Fines, Update Member Contact, Change Password
Member ------  Search/View Books, View My Loans, Place Hold,
               View My Holds, View My Fines, Change Password
```

Shared: all three actors authenticate (Login); Clerk and Member share catalog search. Return Book «extends» Hold Fulfilment: when a returned book has pending holds, the earliest hold is marked fulfilled and the book is reserved at the desk.

### 8.2 Workflow Diagram

```
Start → Login → role?
  ├─ Librarian → manage catalog/members → reports/backup → logout
  ├─ Clerk     → issue ⇄ return → renew → collect fine → logout
  └─ Member    → search → place hold → view loans/fines → logout

Return branch: book overdue? → calculate fine → record fine (unpaid)
               holds pending?  → fulfil earliest hold → status RESERVED
               otherwise       → status AVAILABLE
```

### 8.3 Sequence Diagram — Issue Book (primary flow)

```
Clerk      ConsoleUI     LibraryService        BookDao/PersonDao/LoanDao/HoldDao   SQLite
  │  ids      │                │                        │                          │
  ├──────────►│ issueBook()    │                        │                          │
  │           ├───────────────►│ findById(bookId)       │                          │
  │           │                ├───────────────────────►│ SELECT                   │
  │           │                │◄──── Book ─────────────┤                          │
  │           │                │  validate: exists, member, status, hold queue,    │
  │           │                │  loan limit, fines, overdue                       │
  │           │                │  BEGIN (setAutoCommit false)                      │
  │           │                ├─ insert loan ─► update book ISSUED ─► fulfil hold   │
  │           │                │◄──────────── commit() ─────────────────────────────┤
  │◄──────────┤ "[ok] Issued. Due on ..."          (rollback on any SQLException)    │
```

### 8.4 Class Diagram

```
Displayable (interface)                Comparable<Book> / Comparable<HoldRequest>
     ▲                                        ▲
Person (abstract)                      Book   HoldRequest
  ├── Member                            └──────── Loan (Displayable)
  └── Staff (abstract)
        ├── Librarian (officeNo)
        └── Clerk (deskNo)

LibraryService ──uses──► BookDao, PersonDao, LoanDao, HoldDao
ConsoleUI / *Console ──uses──► LibraryService
BookStatus (enum): AVAILABLE | ISSUED | RESERVED
```

Polymorphism: `display()` / `toString()` are overridden across the hierarchy; `TablePrinter` renders heterogeneous model objects through the `Displayable` contract. `Book` orders naturally by title; `HoldRequest` orders by request date.

### 8.5 ER Diagram

```
PERSON                      BOOK                        LOAN
──────                      ────                        ─────
id (PK)                     id (PK)                     id (PK)
name                        isbn (UNIQUE)               book_id (FK → BOOK.id)
phone                       title                       member_id (FK → PERSON.id)
address                     author                      issue_date
username (UNIQUE)           subject                     due_date
password                    status                      return_date
role                        (AVAILABLE | ISSUED         fine_amount
salary                        | RESERVED)               fine_paid
desk_no / office_no                                     renewed_count

HOLD_REQUEST
────────────
id (PK)
book_id (FK → BOOK.id)
member_id (FK → PERSON.id)
request_date
fulfilled (0/1)

Relationships: PERSON 1—N LOAN; BOOK 1—N LOAN; PERSON 1—N HOLD_REQUEST; BOOK 1—N HOLD_REQUEST
Indexes: loan(member_id), hold_request(book_id)
```

This schema matches the DDL in `lms.util.Database` exactly. Single-table inheritance is used for `person` (role discriminator with nullable `salary`/`desk_no`/`office_no`), which keeps the DAO layer simple.

---

# 9. Design Decisions & Rationale

| Decision | Rationale |
|---|---|
| Console-based UI | The evaluation requires command-line executability; a menu-driven CLI is fully testable via scripted input. |
| SQLite (embedded) | Zero-config file database; evaluator runs the project with no server installation. |
| JDBC (plain) | Direct application of Module 5; no ORM obscures the SQL being demonstrated. |
| `PreparedStatement` everywhere | SQL-injection-safe parameterised queries and correct JDBC practice. |
| DAO pattern | Separates persistence from business rules; each DAO owns its table's SQL. |
| Service layer | Centralises all business rules (loan limits, fines, holds) in one auditable class. |
| Explicit transactions | Issue/return touch multiple tables; commit/rollback guarantees consistency. |
| `PriorityQueue<HoldRequest>` ordered by request date | Implements first-requested-first-served hold fulfilment naturally via `Comparable`; more expressive than sorting a list manually. |
| `ScheduledExecutorService` daemons | Demonstrates concurrency through meaningful tasks (overdue fines, backups) rather than artificial worker loops. |
| `synchronized` service methods | Makes mutating circulation operations thread-safe against the background monitors. |
| Custom checked exceptions | Callers must handle domain failures explicitly; error messages stay user-facing while causes chain for diagnosis. |
| `Properties` configuration | Loan period, fine rate, intervals and directories are tunable without recompilation. |
| `.gitignore` for runtime dirs | `data/`, `logs/`, `backups/`, `reports/`, `build/` stay out of version control; the JDBC driver JAR is tracked. |

---

# 10. Course Module Mapping

| Module | Course Topics | Evidence in Project |
|---|---|---|
| **Module 1** | Java basics, packages, control statements, arrays, strings, I/O fundamentals | `Main`, `Console`, `Config`, `TablePrinter`; `switch`, `do-while`, enhanced `for`, `String.format`, `Scanner` |
| **Module 2** | Classes, objects, inheritance, abstraction, polymorphism, encapsulation, interfaces, enums | `Person` → `Member` / `Staff` → `Librarian` / `Clerk`; `Displayable` interface; `BookStatus` enum; `Comparable` on `Book`/`HoldRequest`; overridden `toString`/`display()` |
| **Module 3** | Exception handling (try/catch/finally, multi-catch, throws, custom exceptions), I/O (streams, readers/writers, `Properties`) | `LMSException` + 10 subclasses; try-with-resources on JDBC and files; chained exceptions (`initCause`); `Properties` load/store; `BufferedReader`/`BufferedWriter`, `PrintWriter`; CSV parsing |
| **Module 4** | Collections: List, Map, Set, Queue, Comparator, utility classes | `ArrayList` (books, loans, members), `HashMap` (subject counts), `TreeMap` (sorted statistics), `TreeSet` (distinct subjects), `PriorityQueue<HoldRequest>` ordered by request date (hold fulfilment), `LinkedList` (activity ring buffer, max 50), `Collections.sort` + `Comparator` |
| **Module 5** | JDBC (DriverManager, Connection, PreparedStatement, ResultSet, transactions), concurrency (ExecutorService, synchronized, daemon threads) | `Database.open()`, `PreparedStatement` exclusively, `setAutoCommit(false)` transactions in issue/return, `Statement.RETURN_GENERATED_KEYS`; `OverdueMonitor` + `BackupService` as `ScheduledExecutorService` daemons; `synchronized` on `LibraryService` mutating methods |

---

# 11. Implementation Details

### 11.1 Object-Oriented Design (Module 2)
`Person` and `Staff` are abstract; `Member`, `Librarian` and `Clerk` are concrete. All fields are private with accessor methods. `Book`, `Loan` and `HoldRequest` implement `Displayable` for uniform console rendering; `Book` and `HoldRequest` implement `Comparable` for natural ordering. `BookStatus` is an enum guarding all state transitions.

### 11.2 Exception Handling (Module 3)
`LMSException` is the checked root; 10 subclasses specialise it. Service methods declare `throws` with the specific types they can raise. `OperationFailedException` wraps `SQLException` with `initCause`. `LibraryService.importBooks()` uses multi-catch (`IOException | SQLException`). The UI layer catches `LMSException` once per flow and prints the message.

### 11.3 File I/O (Module 3)
`Config` loads/stores `config/app.properties` via `Properties` with `FileInputStream`/`FileOutputStream` (defaults written on first run). `ReportGenerator` writes timestamped CSV/TXT reports through `BufferedWriter`/`FileWriter`. `ActivityLog` appends timestamped lines to `logs/lms.log`. `BackupService` copies the database file with `Files.copy` and appends to `backup-manifest.txt` via `PrintWriter`. `importBooks()` reads CSV rows via `BufferedReader`, skipping headers, blanks and duplicate ISBNs.

### 11.4 Collections Framework (Module 4)
Each structure is chosen for the job it does: `ArrayList` for ordered listings, `HashMap` for O(1) statistic accumulation, `TreeMap` for sorted output, `TreeSet` for distinct subjects, `PriorityQueue` (natural ordering by request date) to pick the earliest pending hold on return, `LinkedList` as a bounded recent-activity ring buffer, and `Comparator`/`Collections.sort` where view-level ordering differs from natural ordering.

### 11.5 JDBC & Transactions (Module 5)
The driver is loaded with `Class.forName("org.sqlite.JDBC")` (with a friendly error if the JAR is missing). Every query uses `PreparedStatement`; inserts retrieve auto-generated keys. Issue and return run inside explicit transactions:

```java
c.setAutoCommit(false);
try {
    loanDao.insert(c, loan);
    bookDao.updateStatus(c, bookId, BookStatus.ISSUED);
    if (consumedHold != null) holdDao.markFulfilled(c, consumedHold.getId());
    c.commit();
} catch (SQLException e) {
    c.rollback();
    throw new OperationFailedException("Issue failed, changes rolled back.", e);
} finally {
    c.setAutoCommit(true);
}
```

### 11.6 Concurrency (Module 5)
`OverdueMonitor` runs every `overdue.check.seconds` (default 60): it refreshes fines on overdue loans and logs the scan count. `BackupService` runs every `backup.interval.seconds` (default 300): it copies the SQLite file and updates the manifest. Both use a single-threaded `ScheduledExecutorService` with daemon threads. Mutating `LibraryService` methods (`issueBook`, `returnBook`, `renewLoan`, `placeHold`, `collectFine`) are `synchronized`, so monitor scans and console operations never interleave unsafely.

---

# 12. Screenshots / Results

The application offers two interfaces — a Swing desktop GUI (default) and a terminal UI (`--cli`); results are captured as console transcripts and GUI screenshots (`screenshots/gui/`).

### 12.1 Main menu and librarian statistics (verified output)

```
========== LIBRARY MANAGEMENT SYSTEM ==========
  1. Login as Librarian
  2. Login as Clerk
  3. Login as Member
  0. Exit
Enter choice: 1
Username: admin
Password: admin123
[ok] Welcome, Ravi Kumar (LIBRARIAN).

----- LIBRARIAN MENU (Ravi Kumar) -----
  1. Add Book
  ...
 10. Subject Statistics
Enter choice: 10
  Books per subject:
   ARTIFICIAL INTELLIGENCE   1
   DATABASES                 2
   ...
  Total books: 8
```

### 12.2 Circulation with fine and hold fulfilment (verified output)

```
----- CLERK MENU (Priya Singh) -----
Enter choice: 2        (Return a Book)
Book ID: 5
[ok] Returned with fine Rs. 55.00 (collect at 'Collect Fine').
  [log] Book 5 returned by member 3 with fine Rs. 55.00
  [log] Hold fulfilled for member 4 — book reserved at desk.

Enter choice: 4        (Collect Fine)
Member ID: 3
[ok] Collected Rs. 55.00.
```

### 12.3 Automated test-suite result (verified output)

```
====================================================
 Library Management System — Smoke Test Suite
====================================================
JDK: openjdk version "25.0.3" 2026-09-17
Running application with scripted input...
----------------------------------------------------
[PASS] TC01  Valid librarian login
[PASS] TC05  Overdue return calculates fine
[PASS] TC08  Fine collected
...
Result: 86 passed, 0 failed
====================================================
ALL TESTS PASSED
```

Reproduce with `./tests/run_smoke_test.sh` (see Section 13). The repository also contains `screenshots/gui/` with captures of the Swing application (sign-in, librarian dashboard, books, members, circulation, holds, fines, reports, settings and the member portal).

---

# 13. Testing Approach

Testing is two-layered: a **reproducible automated smoke test** plus documented **manual scenarios**.

### 13.1 Automated smoke test

`tests/run_smoke_test.sh` runs the compiled application in a temporary sandbox with a fresh database, feeds a deterministic scripted session covering all three roles, and asserts exact expected outputs (65 checks). It exits `0` only when every assertion passes; on failure the raw console output is preserved for diagnosis. `tests/test_cases.md` documents the full matrix; `tests/expected_output.txt` holds a verified passing run.

```bash
./tests/run_smoke_test.sh
# Result: 86 passed, 0 failed — ALL TESTS PASSED (exit code 0)
```

### 13.2 Test case matrix (executed 2026-09-17, OpenJDK 25.0.3, Linux)

| ID | Input / Action | Expected Result | Status |
|---|---|---|---|
| TC01 | Valid librarian login | Librarian menu opens | PASS |
| TC02 | Clerk login, wrong password | `Incorrect password.` | PASS |
| TC03 | Role-mismatch login | Role-mismatch rejection | PASS |
| TC04 | Issue available book | Loan created, due +14 days | PASS |
| TC05 | Return book 11 days overdue | Fine Rs. 55.00 (11 × Rs. 5) | PASS |
| TC05 | Return on time | No fine | PASS |
| TC06 | Issue already-issued book | Rejected (issued to another member) | PASS |
| TC06 | Issue at loan limit (3) | `Loan limit reached (3 books).` | PASS |
| TC07 | Issue while fine pending | `Pending fine of Rs. 55.00` | PASS |
| TC08 | Collect fine | `Collected Rs. 55.00.` | PASS |
| TC09 | Valid clerk / member logins | Role menus open | PASS |
| TC10 | Member views loans | 2 active loans listed | PASS |
| TC11 | Hold on issued book | Queued, position 1 | PASS |
| TC11 | Reserved book issued to first hold owner | Hold consumed, loan created | PASS |
| TC12 | Hold own borrowed book | Rejected | PASS |
| TC13 | Hold available book | Rejected (borrow directly) | PASS |
| TC13 | Third hold per member | Hold limit rejection | PASS |
| TC14 | Renew active loan | New due date | PASS |
| TC14 | Third renewal | `Renewal limit (2) reached` | PASS |
| TC14 | Renewal with pending hold | `Renewal blocked — 1 hold request(s)` | PASS |
| TC15 | Subject statistics | Sorted counts, total 8 | PASS |
| TC16 | View all books | 8 seeded books listed | PASS |
| TC17 | Add book | `Book added with ID 9.` | PASS |
| TC18 | Duplicate ISBN | `ISBN already exists: 978-TEST-0001` | PASS |
| TC19 | Double return | `This book is not currently issued.` | PASS |
| TC20 | Books CSV report | File created in `reports/` | PASS |
| TC21 | CSV import (6 rows) | `6 book(s) imported.` | PASS |
| TC22 | Backup now | Timestamped backup in `backups/` | PASS |
| TC23 | Runtime artefacts | `data/library.db`, `logs/lms.log` created | PASS |
| TC24 | Dashboard overview (KPIs, charts) | 15 books, 3 active loans, status/subject charts | PASS |
| TC24 | Circulation / Members / Hold Queue drill-downs | Attention table, member loans/fines, grouped holds | PASS |
| TC25 | Member: My Dashboard | Own loans + fines KPIs, MY LOANS table | PASS |
| — | Full session | No unhandled exceptions; clean exit | PASS |

### 13.3 Manual scenarios additionally verified

Negative-input handling of `Console.readInt` (non-numeric, out-of-range → re-prompt); delete blocked for issued/reserved books; CSV import skipping duplicate ISBNs with a reported count; overdue monitor scan entries in `logs/lms.log`; backup manifest append; password change followed by re-login with the new password.

---

# 14. Challenges Faced

1. **Maintaining database consistency during circulation.** Issue and return update several tables (loan, book status, holds). *Solution:* explicit JDBC transactions with commit/rollback so partial updates are impossible.
2. **Preventing invalid library operations.** Many rules interact (limits, fines, holds, status). *Solution:* centralising all rules in `LibraryService` and expressing every failure as a specific custom exception the UI can print meaningfully.
3. **Running periodic tasks without blocking the CLI.** *Solution:* `ScheduledExecutorService` with daemon threads for the overdue monitor and backup service, plus `synchronized` service methods to make shared-state mutations thread-safe.
4. **Keeping UI, business logic and persistence decoupled.** *Solution:* the layered `ui → service → dao` structure with model classes shared through interfaces (`Displayable`).
5. **Making testing reproducible.** Manual testing alone left no executable evidence. *Solution:* a sandboxed scripted smoke test with 65 assertions and a machine-checkable exit code, plus an in-process GUI self-test that drives the Swing app and captures screenshots.
6. **Keeping runtime artefacts out of the repository.** *Solution:* configurable output directories (`data/`, `logs/`, `backups/`, `reports/`) and `.gitignore` rules, while tracking the bundled JDBC driver.

---

# 15. Learnings & Key Takeaways

- Object-oriented design pays off across a whole application: the `Person` hierarchy and `Displayable` interface let the UI treat all model objects uniformly.
- JDBC is more than queries — transactions (`setAutoCommit`, `commit`, `rollback`) are what make multi-table operations trustworthy.
- Choosing the right collection matters: `PriorityQueue` ordered by request date expresses hold fulfilment directly; `TreeMap` yields sorted statistics for free.
- A custom checked exception hierarchy produces far clearer errors than blanket `catch (Exception)`, while chaining preserves the root cause.
- `ScheduledExecutorService` with daemon threads is a clean way to run background work in a CLI application that must exit promptly.
- Layered packaging (`ui → service → dao`) made the automated test suite straightforward to build, because each layer could be exercised through stable interfaces.
- Configuration via `Properties` separated behaviour from code and enabled sandboxed test runs.
- A reproducible test script converts "it worked when I tried it" into evidence anyone can re-run in seconds.
- Git and GitHub — with meaningful commits and ignored runtime artefacts — are part of deliverable quality, not an afterthought.

---

# 16. Limitations

- **Plaintext passwords** — acceptable for this educational scope, unsuitable for production (hashing is the top future enhancement).
- **Single-branch library** — no multi-branch support or `branch_id` dimension.
- **No reservation expiry** — a fulfilled hold waits at the desk indefinitely.
- **No notifications** — members learn about hold fulfilment or fines only at the console.
- **File-copy backups** — a copy taken mid-transaction could be momentarily inconsistent; SQLite `VACUUM INTO` would guarantee hot-backup consistency.
- **Console-only UI** — intentional per evaluation requirements, but limits accessibility.

---

# 17. Future Enhancements

- Hash passwords with PBKDF2/BCrypt and add session tokens.
- Multi-branch support with branch-level catalog separation.
- Email/SMS notifications for overdue reminders and hold fulfilment.
- Reservation expiry with automatic release of uncollected holds.
- Consistent hot backups via SQLite `VACUUM INTO`.
- A unit-test layer (JUnit) complementing the end-to-end smoke suite.
- Optional REST API layer for integration beyond the console.

---

# 18. Conclusion

This Library Management System demonstrates an integrated application of all five modules of the *Programming in Java* course in one coherent, runnable product: language fundamentals and I/O, object-oriented design, a disciplined exception hierarchy, purposeful use of the Collections Framework, and JDBC persistence with real transactions alongside scheduled concurrency. Beyond features, the project emphasises engineering discipline — a layered architecture, a reproducible 41-assertion test suite, honest documentation of limitations, and clean separation of runtime artefacts from source. The code is original work written specifically for this evaluation.

---

# 19. References

1. VITyarthi — *Programming in Java* course materials, Modules 1–5.
2. Oracle — The Java™ Tutorials: Collections, JDBC Database Access, Concurrency.
3. Xerial — `sqlite-jdbc` 3.53.4.0 driver documentation.
4. Java SE API documentation — `java.time`, `java.sql`, `java.util.concurrent`.
5. SQLite documentation — file format, locking and `VACUUM INTO`.
