# Sequence Diagram — Issue Book (Simplified)

```
Clerk Console          LibraryService          BookDao          LoanDao          HoldDao
     |                      |                    |                |                |
     |--- issueBook(bookId, memberId) ------------>|                |                |
     |                      |                    |                |                |
     |                      |--- findById(c,bookId) >-------------->|                |                |
     |                      |                    |                |                |
     |                      |<--- Book (or null) ---------------------|                |                |
     |                      |                    |                |                |
     |                      |--- findById(c,memberId) >-------------->|                |                |
     |                      |                    |                |                |
     |                      |--- findActiveByMember(c,memberId) >---------------->|                |                |
     |                      |                    |                |                |
     |                      |<--- List<Loan> ----------------------|                |                |
     |                      |                    |                |                |
     |                      |--- findPendingByBook(c,bookId) >-----------------------------|                |                |
     |                      |                    |                |                |
     |                      | (if RESERVED + first hold == member) >--- return HoldRequest ---|                |                |
     |                      |                    |                |                |
     |                      |--- insert(c,new Loan) >------------------------------------------>|                |                |
     |                      |                    |                |                |
     |                      |--- updateStatus(c,bookId,ISSUED) >------------------------------>|                |                |
     |                      |                    |                |                |
     |                      |--- markFulfilled(c,holdId) >--------------------------------------|                |                |
     |                      |                    |                |                |
     |                      |--- commit() >----(Connection)              |                |                |
     |                      |                    |                |                |
     |<--- Loan (with id, dueDate) -------------|                |                |                |
     |                      |                    |                |                |
```

This sequence demonstrates:
- Layered interaction (UI → Service → DAO → DB)
- Multiple DAO coordination within a transaction
- Conditional logic based on hold queue and book status
- Return of result with new due date to UI
