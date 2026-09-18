# Class Diagram (Text Representation)

## Inheritance Hierarchy

```
            [Displayable] (interface)
                  /
            [Person] (abstract)
            /        \
    [Member]      [Staff] (abstract)
                     /      \
            [Librarian]    [Clerk]

[Book] implements Displayable, Comparable<Book>
[Loan] implements Displayable
[HoldRequest] implements Displayable, Comparable<HoldRequest>
[BookStatus] enum (AVAILABLE, ISSUED, RESERVED)
```

## Key Class Relationships

- **LibraryService** uses **BookDao**, **PersonDao**, **LoanDao**, **HoldDao** (composition)
- **LibraryService** depends on **Config** and **Database**
- **ConsoleUI** uses **LibraryService** and **BackupService** (composition)
- **ReportGenerator** uses **Config** and writes files via **BufferedWriter**
- **OverdueMonitor** / **BackupService** use **Config** + **LoanDao** / **Database**
- **ActivityLog** is a static utility; used across service layer
- **TablePrinter** formats collections for console output

## Main Packages

- `lms.model` — domain entities
- `lms.dao` — JDBC persistence
- `lms.service` — business rules (synchronized)
- `lms.reports` — file-based reporting
- `lms.concurrency` — background threads
- `lms.util` — helpers, config, logging
- `lms.ui` — Swing desktop app (LoginFrame, DashboardFrame, page panels) and console menus
