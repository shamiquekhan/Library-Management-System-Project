package lms.model;

import java.time.LocalDate;

public class HoldRequest implements Displayable, Comparable<HoldRequest> {

    private int id;
    private int bookId;
    private int memberId;
    private LocalDate requestDate;
    private boolean fulfilled;

    private String bookTitle;
    private String memberName;

    public HoldRequest() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public int compareTo(HoldRequest o) {
        int r = this.requestDate.compareTo(o.requestDate);
        if (r != 0) {
            return r;
        }
        return Integer.compare(this.id, o.id);
    }

    @Override
    public void display() {
        String status = fulfilled ? "FULFILLED - book kept at desk for you" : "PENDING";
        System.out.printf("  Hold #%d: %s -> %s  Requested: %s  Status: %s%n", id, bookTitle, memberName, requestDate, status);
    }
}