# Screenshots / Console Session Transcript

The application has two interfaces: a Swing desktop GUI (default) and this
terminal UI (`--cli`). The GUI's captured runs live in `screenshots/gui/`;
this file documents the terminal interface with verified transcripts.

## Session 1 — Librarian Login & Report (Verified)
```
[ok] Welcome, Ravi Kumar (LIBRARIAN).
----- LIBRARIAN MENU -----
Enter choice: 12  (Generate Reports)
Report: 1. Books CSV  2. Members CSV  3. Active Loans TXT  Choice: 1
[ok] Report written to reports/books_report_20260914-214124.csv
```

## Session 2 — Clerk Issue & Return with Fine (Verified)
```
[ok] Welcome, Priya Singh (CLERK).
Enter choice: 1  (Issue a Book)
Member ID: 3
Book ID: 6
[ok] Issued. Due on 2026-09-28.

Enter choice: 2  (Return a Book)
Book ID: 5
[ok] Returned with fine Rs. 55.00 (collect at 'Collect Fine').

[log] Book 5 returned by member 3 with fine Rs. 55.00
[log] Hold fulfilled for member 4 — book reserved at desk.
```

## Session 3 — Member Holds (Verified)
```
[ok] Welcome, Bobby Das (MEMBER).
Enter choice: 5  (My Hold Requests)
  1     JDBC API Tutorial and Reference  Bobby Das  2026-09-12  FULFILLED - book at desk
```

## Session 4 — Backup Trigger (Verified)
```
Enter choice: 13  (Backup Database Now)
[ok] Backup created: backups/library-backup-20260914-214552.db (books=8, activeLoans=1, members=2)
```
