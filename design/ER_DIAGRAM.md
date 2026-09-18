# Database / Storage Design (ER Diagram Description)

## Entities & Relationships

- **Person** (1) ---< (N) **Loan** (borrower links)
- **Person** (1) ---< (N) **HoldRequest** (holder links)
- **Book** (1) ---< (N) **Loan** (book links)
- **Book** (1) ---< (N) **HoldRequest** (book links)

## Attributes

**Person:** id (PK), name, phone, address, username, password, role, salary, desk_no, office_no
**Book:** id (PK), isbn (unique), title, author, subject, status (AVAILABLE/ISSUED/RESERVED)
**Loan:** id (PK), book_id (FK), member_id (FK), issue_date, due_date, return_date, fine_amount, fine_paid, renewed_count
**HoldRequest:** id (PK), book_id (FK), member_id (FK), request_date, fulfilled (0/1)

## Design Decisions
- Single `person` table with role discriminator (instead of separate Borrower/Staff/Borrower tables) to simplify DAO layer and demonstrate inheritance mapping
- `status` column on `book` tracks circulation state (AVAILABLE / ISSUED / RESERVED) rather than deriving solely from active loans — improves query performance and supports hold-reserved state
- `fulfilled` flag on `hold_request` distinguishes completed vs pending holds
- `fine_amount` and `fine_paid` on `loan` track payments per loan; total outstanding computed by summing unpaid records
