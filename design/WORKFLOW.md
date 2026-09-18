# Workflow / Process Flow — Issue a Book

```
Member (or Clerk) selects Issue Book
         |
         v
[ConsoleUI / ClerkConsole] reads bookId + memberId
         |
         v
LibraryService.issueBook(bookId, memberId)
         |
         |--- Check Book exists? (BookDao.findById)
         |--- Check Member exists? (PersonDao.findById)
         |--- Check Book status (AVAILABLE / RESERVED?)
         |--- Check Member active loans < max
         |--- Check no duplicate active loan for this book
         |--- Check unpaid fines = 0
         |--- Check no overdue active loans
         |
         v
If all valid:
  Connection.setAutoCommit(false)
  LoanDao.insert(new Loan)
  BookDao.updateStatus(bookId, ISSUED)
  If RESERVE fulfilled: HoldDao.markFulfilled(holdId)
  Commit
         |
         v
Return Loan object + print Due Date to Clerk
```

## Sequence — Return a Book

```
Clerk selects Return Book → enter bookId
LibraryService.returnBook(bookId)
  LoanDao.findActiveByBook(bookId)
  Calculate fine = max(0, daysOverdue) * 5.0
  Transaction:
    LoanDao.recordReturn(loanId, today, fine)
    PriorityQueue of pending holds → peek first
    If hold exists:
      HoldDao.markFulfilled(holdId)
      BookDao.updateStatus(bookId, RESERVED)
    Else:
      BookDao.updateStatus(bookId, AVAILABLE)
  Commit
  Log "Book returned with fine X" / "Hold fulfilled"
```
