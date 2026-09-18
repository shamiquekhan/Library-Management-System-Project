# Use Case Diagram (Text Description)

## Actors
- **Librarian** (Admin / Head Librarian)
- **Clerk** (Checkout / Front Desk)
- **Member** (Student / Faculty Borrower)

## Use Cases

### Librarian
- UC-L1: Add / Update / Remove Book
- UC-L2: Register Member
- UC-L3: View All Members / Books / Loans
- UC-L4: View Overdue Loans
- UC-L5: Subject Statistics
- UC-L6: Import Books from CSV
- UC-L7: Generate Reports (CSV / TXT)
- UC-L8: Backup Database
- UC-L9: Change Password

### Clerk
- UC-C1: Issue Book
- UC-C2: Return Book
- UC-C3: Renew Loan
- UC-C4: Collect Fine
- UC-C5: Search / View Catalog
- UC-C6: Update Member Contact
- UC-C7: Change Password

### Member
- UC-M1: Search Books
- UC-M2: View Catalog
- UC-M3: View My Loans
- UC-M4: Place Hold Request
- UC-M5: View My Holds / Fines
- UC-M6: Change Password

### Relationships
- Librarian and Clerk share Catalog Search / View
- Clerk uses Issue / Return which trigger Loan + Book status updates
- Member uses Hold Request which creates HoldRequest record; fulfilled when Clerk returns book with pending holds
