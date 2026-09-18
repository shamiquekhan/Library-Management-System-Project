package lms.dao;

import lms.model.HoldRequest;
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

public class HoldDao {

    public int insert(Connection c, HoldRequest h) throws SQLException {
        String sql = "INSERT INTO hold_request (book_id, member_id, request_date, fulfilled) VALUES (?,?,?,0)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getBookId());
            ps.setInt(2, h.getMemberId());
            ps.setString(3, DateTimeUtil.format(h.getRequestDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<HoldRequest> findPendingByBook(Connection c, int bookId) throws SQLException {
        String sql = "SELECT h.*, b.title AS book_title, p.name AS member_name FROM hold_request h "
            + "JOIN book b ON b.id = h.book_id JOIN person p ON p.id = h.member_id "
            + "WHERE h.book_id=? AND h.fulfilled=0 ORDER BY h.request_date, h.id";
        List<HoldRequest> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public HoldRequest firstPendingByBook(Connection c, int bookId) throws SQLException {
        String sql = "SELECT h.*, b.title AS book_title, p.name AS member_name FROM hold_request h "
            + "JOIN book b ON b.id = h.book_id JOIN person p ON p.id = h.member_id "
            + "WHERE h.book_id=? AND h.fulfilled=0 ORDER BY h.request_date, h.id LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<HoldRequest> findByMember(Connection c, int memberId) throws SQLException {
        String sql = "SELECT h.*, b.title AS book_title, p.name AS member_name FROM hold_request h "
            + "JOIN book b ON b.id = h.book_id JOIN person p ON p.id = h.member_id "
            + "WHERE h.member_id=? ORDER BY h.request_date";
        List<HoldRequest> list = new ArrayList<>();
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

    public List<HoldRequest> findAllPending(Connection c) throws SQLException {
        String sql = "SELECT h.*, b.title AS book_title, p.name AS member_name FROM hold_request h "
            + "JOIN book b ON b.id = h.book_id JOIN person p ON p.id = h.member_id "
            + "WHERE h.fulfilled=0 ORDER BY h.book_id, h.request_date, h.id";
        List<HoldRequest> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean hasPending(Connection c, int memberId, int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hold_request WHERE member_id=? AND book_id=? AND fulfilled=0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int countPendingByMember(Connection c, int memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hold_request WHERE member_id=? AND fulfilled=0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void markFulfilled(Connection c, int holdId) throws SQLException {
        String sql = "UPDATE hold_request SET fulfilled=1 WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, holdId);
            ps.executeUpdate();
        }
    }

    private HoldRequest mapRow(ResultSet rs) throws SQLException {
        HoldRequest h = new HoldRequest();
        h.setId(rs.getInt("id"));
        h.setBookId(rs.getInt("book_id"));
        h.setMemberId(rs.getInt("member_id"));
        h.setRequestDate(DateTimeUtil.parse(rs.getString("request_date")));
        h.setFulfilled(rs.getInt("fulfilled") == 1);
        h.setBookTitle(rs.getString("book_title"));
        h.setMemberName(rs.getString("member_name"));
        return h;
    }
}