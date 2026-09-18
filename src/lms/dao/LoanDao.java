package lms.dao;

import lms.model.Loan;
import lms.util.Config;
import lms.util.Database;
import lms.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDao {

    private static final String BASE_SELECT = "SELECT l.id, l.book_id, l.member_id, l.issue_date, l.due_date, l.return_date, l.fine_amount, l.fine_paid, l.renewed_count, b.title AS book_title, p.name AS member_name FROM loan l JOIN book b ON b.id = l.book_id JOIN person p ON p.id = l.member_id ";

    public int insert(Connection c, Loan loan) throws SQLException {
        String sql = "INSERT INTO loan (book_id, member_id, issue_date, due_date, return_date, fine_amount, fine_paid, renewed_count) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookId());
            ps.setInt(2, loan.getMemberId());
            ps.setString(3, DateTimeUtil.format(loan.getIssueDate()));
            ps.setString(4, DateTimeUtil.format(loan.getDueDate()));
            if (loan.getReturnDate() == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, DateTimeUtil.format(loan.getReturnDate()));
            }
            ps.setDouble(6, loan.getFineAmount());
            ps.setInt(7, loan.isFinePaid() ? 1 : 0);
            ps.setInt(8, loan.getRenewedCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public Loan findById(Connection c, int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Loan findActiveByBook(Connection c, int bookId) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.book_id=? AND l.return_date IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Loan> findActiveByMember(Connection c, int memberId) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.member_id=? AND l.return_date IS NULL ORDER BY l.issue_date";
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Loan> findAllActive(Connection c) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.return_date IS NULL ORDER BY l.due_date";
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Loan> findOverdue(Connection c, LocalDate today) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.return_date IS NULL AND l.due_date < ? ORDER BY l.due_date";
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DateTimeUtil.format(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Loan> findAllUnpaidFines(Connection c) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.fine_amount > 0 AND l.fine_paid = 0 AND l.return_date IS NOT NULL ORDER BY p.name, l.due_date";
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Loan> findUnpaidFinesByMember(Connection c, int memberId) throws SQLException {
        String sql = BASE_SELECT + "WHERE l.member_id=? AND l.fine_amount > 0 AND l.fine_paid = 0 AND l.return_date IS NOT NULL";
        List<Loan> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void updateFine(Connection c, int loanId, double fine) throws SQLException {
        String sql = "UPDATE loan SET fine_amount=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, fine);
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    public void recordReturn(Connection c, int loanId, LocalDate returnDate, double fine) throws SQLException {
        String sql = "UPDATE loan SET return_date=?, fine_amount=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DateTimeUtil.format(returnDate));
            ps.setDouble(2, fine);
            ps.setInt(3, loanId);
            ps.executeUpdate();
        }
    }

    public void setNewDueDate(Connection c, int loanId, LocalDate newDue) throws SQLException {
        String sql = "UPDATE loan SET due_date=?, renewed_count = renewed_count + 1 WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, DateTimeUtil.format(newDue));
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    public void markFinePaid(Connection c, int loanId) throws SQLException {
        String sql = "UPDATE loan SET fine_paid=1 WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ps.executeUpdate();
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setMemberId(rs.getInt("member_id"));
        loan.setIssueDate(DateTimeUtil.parse(rs.getString("issue_date")));
        loan.setDueDate(DateTimeUtil.parse(rs.getString("due_date")));
        String ret = rs.getString("return_date");
        loan.setReturnDate(ret == null ? null : DateTimeUtil.parse(ret));
        loan.setFineAmount(rs.getDouble("fine_amount"));
        loan.setFinePaid(rs.getInt("fine_paid") == 1);
        loan.setRenewedCount(rs.getInt("renewed_count"));
        loan.setBookTitle(rs.getString("book_title"));
        loan.setMemberName(rs.getString("member_name"));
        return loan;
    }
}