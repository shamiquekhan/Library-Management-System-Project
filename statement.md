# Project Statement — Library Management System

## Problem Statement
Libraries manage large volumes of books, members, loans, holds, fines, and reports using manual or fragmented processes. Errors in issuing books, tracking overdue items, handling holds, and reporting cause operational inefficiency and poor service. There is a need for a centralized, reliable, command-line based Library Management System that applies core Java programming concepts.

## Scope of the Project
- Three-user-role authentication (Librarian, Clerk, Member)
- Book inventory management with CRUD and CSV import/export
- Circulation workflows: issue, return, renew, hold requests (priority queue)
- Fine calculation (Rs. 5/day overdue) with automated overdue scanning
- Structured reporting (books, members, loans) and automated database backups
- Configurable via properties file; self-contained SQLite database
- Fully executable via terminal without GUI dependencies

## Target Users
- Library administrators (Librarians) needing inventory and reporting
- Library staff (Clerks) processing checkouts, returns, fines, and registrations
- Library members (Students/Faculty) borrowing books, placing holds, viewing loans/fines

## High-Level Features
- Console menu-driven interface with role-specific portals
- Object-Oriented domain model with inheritance and interfaces
- Custom exception hierarchy for robust error handling
- JDBC/SQLite persistence with ACID transactions
- Concurrent background tasks (overdue monitor + backup daemon)
- File I/O for reports, logs, CSV import, and serialized backups
