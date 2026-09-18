# System Architecture Diagram

```
+------------------------------------------------------+
|                 Presentation Layer (lms.ui)          |
|  Swing desktop app (default):                        |
|    LoginFrame -> DashboardFrame + page panels        |
|    Dashboard/Books/Members/Circulation/Holds/        |
|    Fines/Reports/Settings + member pages             |
|  Terminal UI (--cli):                                |
|    ConsoleUI (Main Menu)  |  LibrarianConsole        |
|    ClerkConsole          |  MemberConsole            |
+------------------------------------------------------+
                          |
                          v
+------------------------------------------------------+
|               LibraryService (Business)              |
|  Issue / Return / Renew / Hold / Collect / Search     |
|  Synchronized methods for thread-safe mutations       |
+------------------------------------------------------+
                          |
            +-------------+-------------+
            v             v             v
+--------+--------+  +--------+--------+  +--------+--------+
|  BookDao  |  |  LoanDao  |  |  HoldDao  |
| (JDBC)    |  |  (JDBC)   |  |  (JDBC)   |
+-----------+  +-----------+  +-----------+
            v             v             v
+--------+--------+  +--------+--------+  +--------+--------+
| PersonDao|  |  Database  |  |  Config    |
| (JDBC)   |  | (SQLite)   |  | (Properties)
+-----------+  +-----------+  +------------+
                          |
                    +-----v-----+
                    | Overdue   |
                    | Monitor   |
                    | (Scheduled|
                    |  Thread)  |
                    +-----------+
                    | Backup    |
                    | Service   |
                    | (Scheduled|
                    |  Thread)  |
                    +-----------+
```

Data Flow:
1. User input via Console → LibraryService validates rules
2. LibraryService opens JDBC Connection via Database class
3. DAOs execute PreparedStatements; transactions for issue/return
4. Results mapped to domain objects → displayed via TablePrinter
5. Concurrent threads (monitor/backup) access DB independently
