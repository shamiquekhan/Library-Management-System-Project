# Project Statement — Library Management System

## Problem Statement
Libraries manage a continuous cycle of cataloguing, circulation and accountability: books are added and withdrawn, members join, books are issued and returned, popular titles accumulate hold queues, overdue loans attract fines, and administrators need statistics, reports and safe backups.

Managing this cycle by hand is slow and error-prone: due dates are miscounted, fines are calculated inconsistently, hold queues are lost, and no audit trail exists. Spreadsheet-style records share none of the validation, transactional consistency or role-based access that a purpose-built system provides.

This project delivers a digital solution that:
1. Automates cataloguing, membership, circulation, holds and fine calculation with enforced business rules.
2. Provides role-appropriate interfaces for Librarian, Clerk and Member.
3. Keeps all records durable in a relational database with transactional updates.
4. Produces reports, statistics, logs and backups without manual effort.

## Scope of the Project
- Three-user-role authentication: Librarian, Clerk, Member
- Book inventory management with CRUD and CSV import
- Circulation workflows: issue, return, renew, and hold requests
- Fine calculation and overdue monitoring
- Reporting and statistics
- SQLite/JDBC persistence
- Automated database backup
- Activity logging
- Java Swing desktop interface
- Command-line interface for headless execution and automated testing

## Target Users
- Library administrators (Librarians) needing inventory and reporting
- Library staff (Clerks) processing checkouts, returns, fines, and registrations
- Library members (Students/Faculty) borrowing books, placing holds, viewing loans/fines

## High-Level Features
- Java Swing desktop interface with role-specific navigation and portals
- A command-line interface is retained for headless execution and automated testing
- Object-Oriented domain model with inheritance and interfaces
- Custom exception hierarchy for robust error handling
- JDBC/SQLite persistence with ACID transactions
- Concurrent background tasks (overdue monitor + backup daemon)
- File I/O for reports, logs, CSV import, and serialized backups
