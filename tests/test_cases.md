# Test Cases — Library Management System

Reproducible, automated smoke test covering all three roles, circulation
rules, fines, holds, renewals, reports, import and backup.

> The application has exactly two interfaces: the **Swing desktop app**
> (default) and the **terminal UI** (`--cli`). This suite verifies the
> terminal interface end-to-end; the desktop app is verified separately by
> an in-process GUI self-test (see `screenshots/gui/`).

## How to run

```bash
./tests/run_smoke_test.sh
```

The script builds the project if needed, runs the application in a clean
temporary sandbox with a **fresh database**, feeds a deterministic input
sequence, and asserts exact expected outputs. It exits `0` when every
check passes and non-zero otherwise (saving the raw console output to
`/tmp/lms_smoke_last_output.txt` for debugging).

## Verification environment

| Item    | Value                                   |
| ------- | --------------------------------------- |
| OS      | Linux (Ubuntu 26.04)                    |
| JDK     | OpenJDK 25.0.3                          |
| Date    | 2026-09-18                              |
| Command | `./tests/run_smoke_test.sh`             |
| Result  | **65 checks / 65 passed, exit code 0**  |

## Test case matrix

### Authentication & roles

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC01 | Librarian login `admin` / `admin123`            | Librarian menu opens                           | PASS   |
| TC02 | Clerk login with wrong password                 | Rejected: `Incorrect password.`                | PASS   |
| TC03 | Member login with clerk credentials (role mismatch) | Rejected: role-mismatch message            | PASS   |
| TC04 | Clerk / Member login with valid credentials     | Role-specific menu opens                       | PASS   |

### Book management

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC16 | Librarian: view all books / search              | 8 seeded books listed with status              | PASS   |
| TC17 | Add new book (valid data)                       | `Book added with ID 9.`                        | PASS   |
| TC18 | Add book with duplicate ISBN                    | Rejected: `ISBN already exists: 978-TEST-0001` | PASS   |
| TC15 | Librarian: subject statistics                   | Per-subject counts, total = 8                  | PASS   |

### Circulation — issue

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC04 | Issue available book                            | Loan created with due date (+14 days)          | PASS   |
| TC06 | Issue book already issued                       | Rejected: book is issued to another member     | PASS   |
| TC06 | Issue to member already holding 3 loans         | Rejected: `Loan limit reached (3 books).`      | PASS   |
| TC11 | Issue RESERVED book to first hold owner         | Loan created; hold consumed (PriorityQueue)    | PASS   |

### Circulation — return & fines

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC05 | Return overdue book (seeded 11 days late)       | Fine Rs. 55.00 recorded (11 × Rs. 5)           | PASS   |
| TC05 | Return book on time                             | `Returned on time, no fine.`                   | PASS   |
| TC19 | Return a book that is not issued                | Rejected: `This book is not currently issued.` | PASS   |
| TC07 | Issue while unpaid fine pending                 | Rejected: `Pending fine of Rs. 55.00`          | PASS   |
| TC08 | Clerk collects pending fine                     | `Collected Rs. 55.00.`                         | PASS   |

### Circulation — renewal & holds

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC14 | Renew active loan                               | New due date issued                            | PASS   |
| TC14 | Third renewal (limit = 2)                       | Rejected: `Renewal limit (2) reached`          | PASS   |
| TC14 | Renewal while hold pending                      | Rejected: `Renewal blocked — 1 hold request(s)`| PASS   |
| TC11 | Place hold on issued book                       | Queued: `You are number 1 in the queue.`       | PASS   |
| TC12 | Hold on book already on loan to self            | Rejected: member already has this book         | PASS   |
| TC13 | Hold on available book                          | Rejected: borrow directly instead              | PASS   |
| TC13 | Third concurrent hold per member (limit = 2)    | Rejected: hold limit message                   | PASS   |

### Dashboards

Executed as Phase H of the scripted session, after reports/import/backup
(session end-state: 15 books — 11 available, 3 issued, 1 reserved; 3 active
loans, none overdue; 2 pending holds for bobby on books 3 and 6).

| ID   | Input / Action                                  | Expected Result                                      | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------------- | ------ |
| TC24 | Librarian: Dashboard > Overview                 | KPI block + status/subject charts rendered once      | PASS   |
| TC24 | Overview KPIs                                   | 15 books, 2 members, 3 active (0 overdue)            | PASS   |
| TC24 | Status chart                                    | AVAILABLE 11 / ISSUED 3 / RESERVED 1                 | PASS   |
| TC24 | Librarian: Dashboard > Circulation Drill-Down   | Snapshot counts + attention table, rendered once     | PASS   |
| TC24 | Attention table ordering                        | Overdue (if any) first, then soonest due dates       | PASS   |
| TC24 | Librarian: Dashboard > Members Overview         | Both members listed with loan/fine columns           | PASS   |
| TC24 | Librarian: Dashboard > Hold Queue               | Grouped by book: `(ID 3):` and `(ID 6):`, once each  | PASS   |
| TC25 | Member: My Dashboard                            | KPI block + MY LOANS table rendered once             | PASS   |
| TC25 | Member KPIs                                     | Own loans/fines only; 15-book catalog line           | PASS   |

### Reporting, import, backup, logging

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| TC20 | Generate books CSV report                       | File created in `reports/`                     | PASS   |
| TC21 | Import `import/sample_books.csv`                | `6 book(s) imported.`                          | PASS   |
| TC22 | Backup database now                             | Timestamped `.db` copy in `backups/`           | PASS   |
| TC23 | Runtime artefacts                               | `data/library.db` + `logs/lms.log` created     | PASS   |

### Robustness

| ID   | Input / Action                                  | Expected Result                                | Status |
| ---- | ----------------------------------------------- | ---------------------------------------------- | ------ |
| —    | Full scripted session                           | No unhandled `Exception in thread` anywhere    | PASS   |
| —    | Exit from main menu                             | Clean shutdown: `Goodbye!`                     | PASS   |

## Manual scenarios additionally verified during development

The automated suite executes the high-frequency paths. The following
manual checks were also performed against the running CLI:

- `Console.readInt` range/prompt re-ask on non-numeric and out-of-range input
- Delete book blocked while issued or reserved (`OperationNotAllowedException`)
- CSV import skips already-present ISBNs and reports skipped count
- Overdue monitor logs its scan every 60 s (`logs/lms.log`, `[log]` echo)
- Backup service writes `backups/backup-manifest.txt`
- Change password flow (old password verified) and re-login with new password

## Reference output

`tests/expected_output.txt` contains the verified output of a full
passing run (2026-09-18, OpenJDK 25.0.3, Linux).
