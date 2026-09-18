#!/usr/bin/env bash
#
# Library Management System — Reproducible Smoke Test
#
# Runs the compiled application in a temporary sandbox (fresh database),
# feeds a deterministic input sequence covering all three roles, and
# asserts the exact expected outputs. Exit code 0 = all checks passed.
#
# Usage:  ./tests/run_smoke_test.sh
#
set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
OUT=""
PASS=0
FAIL=0
WEB_PID=""
trap 'if [ -n "$OUT" ] && [ "$FAIL" -gt 0 ] && [ -f "$OUT" ]; then cp "$OUT" /tmp/lms_smoke_last_output.txt; echo "APPLICATION OUTPUT SAVED AT: /tmp/lms_smoke_last_output.txt"; fi; if [ -n "$WEB_PID" ]; then pkill -P "$WEB_PID" 2>/dev/null; kill "$WEB_PID" 2>/dev/null; fi; rm -rf "$TMP"' EXIT

IN="$TMP/input.txt"
OUT="$TMP/output.txt"

ok()   { PASS=$((PASS+1)); printf '[PASS] %s\n' "$1"; }
fail() { FAIL=$((FAIL+1)); printf '[FAIL] %s\n' "$1"; }

expect() {  # $1 = label, $2 = fixed pattern that must appear in output
  if grep -Fq -- "$2" "$OUT"; then
    ok "$1"
  else
    fail "$1 (missing expected output: $2)"
  fi
}

expect_absent() {  # $1 = label, $2 = fixed pattern that must NOT appear
  if grep -Fq -- "$2" "$OUT"; then
    fail "$1 (unexpected output found: $2)"
  else
    ok "$1"
  fi
}

expect_count() {  # $1 = label, $2 = exact expected count, $3 = fixed pattern
  local n
  n=$(grep -Fc -- "$3" "$OUT" || true)
  if [ "$n" -eq "$2" ]; then
    ok "$1"
  else
    fail "$1 (expected $2 occurrence(s) of '$3', found $n)"
  fi
}

expect_in_file() {  # $1 = label, $2 = file to search, $3 = fixed pattern
  if grep -Fq -- "$3" "$2" 2>/dev/null; then
    ok "$1"
  else
    fail "$1 (missing expected content in $2: $3)"
  fi
}

file_exists() {  # $1 = glob, $2 = label
  if ls $1 >/dev/null 2>&1; then
    ok "$2"
  else
    fail "$2 (missing file: $1)"
  fi
}

echo "===================================================="
echo " Library Management System — Smoke Test Suite"
echo "===================================================="
java -version 2>&1 | head -1 | sed 's/^/JDK: /'

if [ ! -d "$ROOT/build" ]; then
  echo "Build output not found — running ./build.sh first..."
  "$ROOT/build.sh" >/dev/null
fi

# --- Prepare a clean sandbox so the app gets a fresh database ---------
mkdir -p "$TMP"
[ -d "$ROOT/config" ] && cp -r "$ROOT/config" "$TMP/"
[ -d "$ROOT/import" ] && cp -r "$ROOT/import" "$TMP/"

# --- Deterministic input sequence -------------------------------------
# Seeded state (fresh DB): books 1-8; book 2 (Clean Code? no - Effective
# Java) and book 5 (JDBC API) issued to alice/member 3; book 5 is overdue
# by 11 days; bobby/member 4 has a pending hold on book 5.
#
# Phase A: invalid login, role-mismatch login, librarian: view books,
#          overdue loans, stats, add book (ID 9), duplicate ISBN, logout
# Phase B: clerk: return book 5 (fine Rs. 55), issue blocked by pending
#          fine, collect fine, issue book 6 to alice, issue issued book
#          to bobby (rejected), issue book 8 to bobby, logout
# Phase C: alice: my loans (2), hold own book 3 (rejected), hold book 8
#          (queued), my fines, logout
# Phase D: bobby: hold issued book 3 (queued), hold available book 2
#          (rejected), hold issued book 6 (queued), hold book 5 (limit 2
#          rejected), view holds
# Phase E: clerk: return book 8 on time (alice's hold fulfilled), double
#          return (rejected), issue reserved book 8 to alice (hold
#          consumed, 3rd loan), loan limit rejected on book 7
# Phase F: clerk: renew book 8 twice (ok), third renewal rejected
#          (limit 2), renew book 6 blocked by bobby's pending hold
# Phase G: librarian: books CSV report, CSV import (6 books), backup
# Phase H: librarian: dashboard overview, circulation, members, hold queue
#          (session end-state: 15 books, 3 active loans, 0 overdue,
#          2 pending holds for bobby), then alice: member dashboard
cat > "$IN" <<'EOF'
3
alice
wrongpass
2
alice
alice123
1
admin
admin123
5
9
10
1
978-TEST-0001
Smoke Test Book
Buffy Bot
TESTING
1
978-TEST-0001
Smoke Test Book
Buffy Bot
TESTING
0
2
clerk
clerk123
2
5
1
3
6
4
3
1
3
6
1
4
6
1
4
8
0
3
alice
alice123
3
4
3
4
8
6
0
3
bobby
bobby123
4
3
4
2
4
6
4
5
5
0
2
clerk
clerk123
2
8
2
8
1
3
8
1
3
7
0
2
clerk
clerk123
3
8
3
8
3
8
3
6
0
1
admin
admin123
12
1
11
import/sample_books.csv
13
15
1

2
3
4
5
6
0
0
3
alice
alice123
8
0
0
0
EOF

# --- Run the application against the scripted input -------------------
echo "Running application with scripted input..."
(cd "$TMP" && java -cp "$ROOT/build:$ROOT/lib/*" lms.Main --cli < "$IN" > "$OUT" 2>&1)

echo "----------------------------------------------------"

# --- Global sanity ------------------------------------------------------
expect_absent "No unhandled JVM exceptions" "Exception in thread"
expect        "Application exits cleanly"   "Goodbye! - Shamique Khan (25BAI10187)"

# --- Phase A: authentication -------------------------------------------
expect "TC02  Invalid password rejected"            "[x] Incorrect password."
expect "TC03  Role-mismatch login rejected"         "[x] This account does not belong to the clerk login."
expect "TC01  Valid librarian login"                "[ok] Welcome, Ravi Kumar (LIBRARIAN)."

# --- Phase A: librarian catalog & stats --------------------------------
expect "TC16  View all books (seeded titles)"       "Clean Code"
expect "TC16  Seeded book count shown"              "Total: 8 book(s)."
expect "TC14  Overdue loan listed"                  "OVERDUE"
expect "TC15  Subject statistics rendered"          "Books per subject:"
expect "TC15  Statistics total shown"               "Total books: 8"
expect "TC17  Add book succeeds"                    "[ok] Book added with ID 9."
expect "TC18  Duplicate ISBN rejected"              "[x] ISBN already exists: 978-TEST-0001"

# --- Phase B: clerk circulation & fines --------------------------------
expect "TC04  Valid clerk login"                    "[ok] Welcome, Priya Singh (CLERK)."
expect "TC05  Overdue return calculates fine"       "[ok] Returned with fine Rs. 55.00"
expect "TC07  Issue blocked while fine pending"     "[x] Pending fine of Rs. 55.00 must be cleared before issuing."
expect "TC08  Fine collected"                       "[ok] Collected Rs. 55.00."
expect "TC04  Issue available book succeeds"        "[ok] Issued. Due on"
expect "TC06  Issue of issued book rejected"        "'Computer Networking' is currently issued to another member."

# --- Phase C: member alice (holds, fines, loans) ------------------------
expect "TC09  Valid member login"                   "[ok] Welcome, Alice Fernandes (MEMBER)."
expect "TC10  Member loan view"                     "Total: 2 loan(s)."
expect "TC12  Hold on borrowed book rejected"       "[x] This member already has this book on loan."
expect "TC11  Hold on issued book queued"           "[ok] Hold placed. You are number 1 in the queue."
expect "TC13  Member fine view"                     "Recorded unpaid fine: Rs. 0.00"

# --- Phase D: member bobby (hold rules & limit) --------------------------
expect "TC09  Second member login"                  "[ok] Welcome, Bobby Das (MEMBER)."
expect "TC13  Hold on available book rejected"      "[x] Book is available — borrow it directly instead of placing a hold."
expect "TC11  Member hold queued"                   "[ok] Hold placed. You are number 1 in the queue."
expect "TC13  Hold limit enforced"                  "[x] Hold limit reached (maximum 2 pending holds per member)."

# --- Phase E: clerk returns & loan limit --------------------------------
expect "TC05  On-time return has no fine"           "[ok] Returned on time, no fine."
expect "TC19  Double return rejected"               "[x] This book is not currently issued."
expect "TC11  Reserved book issued to hold winner"  "[ok] Issued. Due on"
expect "TC06  Loan limit enforced"                  "[x] Loan limit reached (3 books). Return a book first."

# --- Phase F: renewals ---------------------------------------------------
expect "TC14  Renewal succeeds"                     "[ok] Renewed. New due date"
expect "TC14  Renewal limit enforced"               "[x] Renewal limit (2) reached for this loan."
expect "TC14  Renewal blocked by pending hold"      "[x] Renewal blocked — 1 hold request(s) pending for this book."

# --- Phase G: reports, import, backup -----------------------------------
expect "TC20  CSV report generated"                 "[ok] Report written to reports/"
expect "TC21  CSV import processes records"         "[ok] 6 book(s) imported."
expect "TC22  Database backup created"              "[ok] Backup created: backups/"

# --- Phase H: dashboards (staff + member) --------------------------------
# Session end-state at this point: 8 seeded books + 1 added in Phase A
# + 6 imported in Phase G = 15 books (11 available, 3 issued, 1 reserved —
# the seeded overdue copy returned in Phase B was reserved for bobby's
# fulfilled hold); 3 active loans, none overdue; 2 pending holds (bobby,
# books 3 and 6).
# Option 1 renders the full ANSI dashboard (DashboardUI), options 2-6 are
# the drill-downs, then alice's member dashboard (option 8 in her menu).
expect_count "TC24  ANSI banner rendered once"          1 "LIVE FROM SQLITE"
expect       "TC24  Overview shows total books"         "TOTAL BOOKS"
expect       "TC24  Overview books value"               "TOTAL BOOKS            15"
expect       "TC24  Overview members value"            "MEMBERS                2"
expect       "TC24  Overview active loans value"       "ACTIVE LOANS           3"
expect       "TC24  Overview overdue value"            "OVERDUE                0"
expect       "TC24  Overview fines value"              "FINES ACCRUING         Rs. 0.00"
expect       "TC24  Status line shows available"       "AVAILABLE : 11"
expect       "TC24  Status line shows issued"          "ISSUED : 3"
expect       "TC24  Status line shows reserved"        "RESERVED : 1"
expect       "TC24  Subject chart shows programming"   "PROGRAMMING"
expect       "TC24  Attention table lists due loans"    "DUE IN 4D"
expect       "TC24  Members table shows alice"         "Alice Fernandes"
expect       "TC24  Members table shows bobby"         "Bobby Das"
expect_count "TC24  Circulation drill-down rendered once" 1 "CIRCULATION SNAPSHOT"
expect       "TC24  Circulation snapshot counts"        "Active loans: 3   Overdue: 0   Due within 3 days: 0"
expect_count "TC24  Hold queue rendered once"           1 "HOLD QUEUE (pending, in priority order)"
expect       "TC24  Hold queue groups by book"          "(ID 3):"
expect       "TC24  Hold queue lists second book"       "(ID 6):"
expect_count "TC25  Member dashboard rendered once"     1 "MY LIBRARY DASHBOARD"
expect       "TC25  Member dashboard library line"      "15 book(s) in catalog, 2 member(s)"
expect       "TC25  Member dashboard loans line"        "My Loans:           3   (0 overdue, 0 due in <=3 days)"
expect       "TC25  Member dashboard fines line"        "My Fines:        Rs. 0.00 accruing, Rs. 0.00 recorded unpaid"
expect_count "TC25  Member loans table rendered once"   1 "MY LOANS"

# --- Runtime artefacts ----------------------------------------------------
file_exists "$TMP"/reports/books_report_*.csv     "TC20  Report file exists on disk"
file_exists "$TMP"/backups/library-backup-*.db    "TC22  Backup file exists on disk"
file_exists "$TMP"/data/library.db                "TC23  SQLite database created"
file_exists "$TMP"/logs/lms.log                   "TC23  Activity log written"

echo "----------------------------------------------------"
echo "Result: $PASS passed, $FAIL failed"
echo "===================================================="

if [ "$FAIL" -eq 0 ]; then
  echo "ALL TESTS PASSED"
  exit 0
else
  exit 1
fi
