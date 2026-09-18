# Library Management System

**Programming in Java — Flipped Course Evaluated Project**  
**Student:** Shamique Khan  
**Registration No.:** 25BAI10187  
**Subject:** Programming in Java (VITyarthi Flipped Course)  
**Deadline:** Sep 18, 2026

---

## Overview

A Library Management System written in Java with two interfaces — a Swing
desktop app (default) and a terminal UI (`--cli`) — demonstrating all five
modules of the course:

| Module | Concepts Covered |
|--------|-----------------|
| **Module 1** | Java basics, control statements, arrays, strings, I/O (Scanner, file I/O) |
| **Module 2** | OOP: inheritance (`Person → Member/Staff → Librarian/Clerk`), polymorphism (`Displayable` interface, `toString/display()` overrides), encapsulation, abstraction (abstract classes), enums, Comparable |
| **Module 3** | Custom exception hierarchy (`LMSException`), try/catch/finally, multi-catch, try-with-resources, chained exceptions, file I/O (BufferedReader/Writer, PrintWriter, Properties) |
| **Module 4** | Collections: `ArrayList`, `HashMap`, `TreeMap`, `TreeSet`, `PriorityQueue`, `LinkedList` (activity log), `Comparator`, `Collections.sort` |
| **Module 5** | JDBC (SQLite via `sqlite-jdbc`), transactions (`setAutoCommit(false)`), `PreparedStatement`, concurrency: `ScheduledExecutorService` (overdue monitor + backup daemon), `synchronized` service methods, `Atomic` counters |

---

## Features

- **Three-role login**: Librarian / Clerk / Member (seeded credentials below)
- **Book management**: add, update, delete, search (title/author/subject), view all
- **Member management**: register, view, update contact, change password
- **Circulation**: issue, return, renew, place hold (priority queue), collect fines
- **Business rules**: loan limit (3), loan period (14 days), fine (Rs. 5/day), hold queue (max 2 per member), renewal limit (2)
- **Reports**: export books/members/loans to timestamped CSV/TXT in `reports/`
- **CSV import**: batch import books from `import/sample_books.csv`
- **Auto-backup**: periodic SQLite file copy to `backups/` with manifest
- **Overdue monitor**: background thread scans every 60s, updates fines, logs activity
- **Activity log**: timestamped events to `logs/lms.log` and console
- **Statistics**: books per subject (TreeMap)
- **Desktop dashboard (Swing)**: the default launch is a light-themed Java Swing desktop app — role-aware sidebar navigation, live KPI stat cards, hand-drawn subject bar chart (no chart library), recent activity and overdue-loan tables; full pages for Books, Members, Circulation, Holds, Fines, Reports and Settings. Every screen talks to the service layer only — no SQL in the UI — and all database work runs off the EDT via `SwingWorker` (`lms.ui.UiWorker`)
- **Console UI**: the original terminal workflow is still available with `./run.sh --cli` (and is used by the smoke test); on headless machines the console UI starts automatically

---

## Tech Stack

- **Language:** Java (JDK 17 or later; verified on OpenJDK 25.0.3)
- **Database:** SQLite (embedded, file-based, zero-config)
- **JDBC Driver:** `sqlite-jdbc-3.53.4.0.jar` (bundled in `lib/`)
- **Build:** Plain `javac` / `java` (no Maven/Gradle required)
- **OS:** Windows, Linux, macOS

## Design & Documentation

- **Project Report (PDF):** `PROJECT_REPORT.pdf` — 10 pages covering all required sections (cover, problem, functional/non-functional requirements, architecture, diagrams, implementation, testing, screenshots/transcript, challenges, learnings, references)
- **Design Artifacts:** `design/ARCHITECTURE.md`, `CLASS_DIAGRAM.md`, `USE_CASE.md`, `WORKFLOW.md`, `SEQUENCE.md`, `ER_DIAGRAM.md`
- **Visual Diagrams:** `images/class_diagram.png`, `architecture_diagram.png`, `sequence_diagram.png`
- **Screenshots / Transcript:** `screenshots/CONSOLE_TRANSCRIPT.md` (verified session outputs)
- **Tests:** `tests/run_smoke_test.sh` (65 automated assertions), `tests/test_cases.md`, `tests/expected_output.txt`
- **GUI self-test:** in-process driver that launches the real Swing app, clicks through every page and captures screenshots to `screenshots/gui/`

---

## Project Structure

```
Library-Management-System-Project/
├── README.md
├── PROJECT_REPORT.md
├── LICENSE
├── .gitignore
├── config/
│   └── app.properties          # Runtime configuration
├── import/
│   └── sample_books.csv        # Sample CSV for import demo
├── lib/
│   └── sqlite-jdbc-3.53.4.0.jar
├── build.sh / run.sh           # Linux/macOS scripts
├── build.bat / run.bat         # Windows scripts
├── src/
│   └── lms/
│       ├── Main.java           # Entry point: Swing GUI by default, --cli for console
│       ├── AppContext.java     # Bootstrap: config, database, services, daemons
│       ├── model/              # Person, Member, Staff, Librarian, Clerk, Book, Loan, HoldRequest, BookStatus, Displayable
│       ├── exception/          # LMSException + 10 custom exceptions
│       ├── dao/                # Database, BookDao, PersonDao, LoanDao, HoldDao
│       ├── service/            # LibraryService, DashboardService (business logic)
│       ├── reports/            # ReportGenerator (CSV/TXT export)
│       ├── concurrency/        # OverdueMonitor, BackupService (ScheduledExecutorService)
│       ├── util/               # Console, Config, DateTimeUtil, FineCalculator, ActivityLog, TablePrinter
│       └── ui/                 # Swing: LoginFrame, DashboardFrame, panels, UITheme |
│                              # Console: ConsoleUI, role consoles, DashboardConsole
└── (generated at runtime)
    ├── data/library.db
    ├── logs/lms.log
    ├── backups/
    └── reports/
```

---

## Testing

The project ships with a reproducible, automated smoke test that runs the
application in a clean sandbox (fresh database) with a deterministic scripted
session covering all three roles, circulation rules, fines, holds, renewals,
reports, CSV import and backup:

```bash
./tests/run_smoke_test.sh
```

- **65 assertions** verified — exit code `0` on success, non-zero on failure
- `tests/test_cases.md` — full test case matrix with results
- `tests/expected_output.txt` — verified output of a passing run
- `screenshots/CONSOLE_TRANSCRIPT.md` — verified manual session transcripts
- `screenshots/gui/` — captured runs of the Swing desktop app (sign-in, dashboards, all pages, member portal)

---

## Prerequisites

- **JDK 17 or later** (verified on OpenJDK 25.0.3)
- No external build tools or database servers needed — everything is self-contained

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/shamiquekhan/Library-Management-System-Project.git
cd Library-Management-System-Project
```

### 2. Build

**Linux / macOS:**
```bash
./build.sh
```

**Windows:**
```cmd
build.bat
```

This compiles all sources to `build/` using the bundled SQLite JDBC driver.

### 3. Run

**Linux / macOS:**
```bash
./run.sh            # Swing desktop app (default)
./run.sh --cli      # original console UI
```

**Windows:**
```cmd
run.bat             :: Swing desktop app (default)
run.bat --cli       :: original console UI
```

Or manually from the project root:
```bash
java -cp "build:lib/*" lms.Main          # GUI
java -cp "build:lib/*" lms.Main --cli    # console
```

> **Important:** Always run from the project root directory so relative paths (`data/`, `config/`, `import/`) resolve correctly. On machines without a display the console UI starts automatically.

---

## Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Librarian | `admin` | `admin123` |
| Clerk | `clerk` | `clerk123` |
| Member | `alice` | `alice123` |
| Member | `bobby` | `bobby123` |

> On first run the database is created at `data/library.db` and seeded with demo data (8 books, 2 staff, 2 members, 2 active loans — one overdue, one hold request). Check `logs/lms.log` for activity.

---

## Sample Usage Walkthrough

1. **Start the app** → Swing sign-in window appears (add `--cli` for the terminal menu)
2. **Login as Clerk** (`clerk` / `clerk123`)
3. **Issue a Book** → enter Member ID `3` (alice) and Book ID `6` (Computer Networking) → success
4. **Return a Book** → enter Book ID `5` (JDBC API, overdue) → shows fine Rs. 55.00
5. **Collect Fine** → enter Member ID `3` → collects Rs. 55.00
5. **Place Hold** (as Member `bobby`) → Book ID `5` now reserved → position 1
6. **Login as Librarian** → **Subject Statistics** → shows counts per subject
7. **Generate Reports** → books CSV, members CSV, loans TXT in `reports/`
8. **Backup Database Now** → creates timestamped copy in `backups/`
9. **Wait ~60 seconds** → overdue monitor logs scan to console & `logs/lms.log`

---

## Configuration (`config/app.properties`)

| Key | Default | Description |
|-----|---------|-------------|
| `db.path` | `data/library.db` | SQLite database file |
| `loan.period.days` | `14` | Loan period in days |
| `max.loans.per.member` | `3` | Max concurrent loans per member |
| `fine.per.day` | `5.0` | Fine per overdue day |
| `overdue.check.seconds` | `60` | Overdue monitor interval |
| `backup.interval.seconds` | `300` | Backup service interval |
| `dir.reports` | `reports` | Reports output directory |
| `dir.backups` | `backups` | Backup output directory |
| `dir.logs` | `logs` | Activity log directory |
| `seed.demo.data` | `true` | Seed demo data on first run |

The file is created automatically on first run with defaults. Edit to customise behaviour.

---

## Reports & Outputs

- `reports/books_report_<timestamp>.csv` — all books
- `reports/members_report_<timestamp>.csv` — all members
- `reports/loans_report_<timestamp>.txt` — active loans with status/fine
- `backups/library-backup-<timestamp>.db` — SQLite file copies
- `backups/backup-manifest.txt` — manifest of backups
- `logs/lms.log` — timestamped activity log (also echoed to console)

---

## Screenshots

The application has exactly **two interfaces**: the Swing desktop app
(default `./run.sh`) and the terminal UI (`./run.sh --cli`). No web,
HTML or browser interface exists or is required.

- `screenshots/gui/` — real captures of the desktop app: sign-in screen,
  librarian dashboard with live KPIs, Books, Members, Circulation, Holds,
  Fines, Reports, Settings, member dashboard, My Loans / My Holds / My Fines
- `screenshots/CONSOLE_TRANSCRIPT.md` — verified terminal session transcripts
  (login menu, librarian portal, circulation, member portal, reports, backup)

---

## License

MIT License — see `LICENSE` file.

---

## Author

**Shamique Khan** (25BAI10187)  
Programming in Java — VITyarthi Flipped Course