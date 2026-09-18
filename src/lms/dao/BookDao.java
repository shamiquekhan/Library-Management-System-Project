package lms.dao;

import lms.model.Book;
import lms.model.BookStatus;
import lms.util.Config;
import lms.util.Database;
import lms.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    public int insert(Connection c, Book b) throws SQLException {
        String sql = "INSERT INTO book (isbn, title, author, subject, status) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getSubject());
            ps.setString(5, b.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void update(Connection c, Book b) throws SQLException {
        String sql = "UPDATE book SET isbn=?, title=?, author=?, subject=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getIsbn());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getSubject());
            ps.setInt(5, b.getId());
            ps.executeUpdate();
        }
    }

    public boolean delete(Connection c, int id) throws SQLException {
        String sql = "DELETE FROM book WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public Book findById(Connection c, int id) throws SQLException {
        String sql = "SELECT * FROM book WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Book> findAll(Connection c) throws SQLException {
        String sql = "SELECT * FROM book ORDER BY title";
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Book> findByStatus(Connection c, BookStatus status) throws SQLException {
        String sql = "SELECT * FROM book WHERE status=? ORDER BY title";
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void updateStatus(Connection c, int bookId, BookStatus status) throws SQLException {
        String sql = "UPDATE book SET status=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    public boolean isbnExists(Connection c, String isbn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM book WHERE isbn=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Book> search(Connection c, String column, String keyword) throws SQLException {
        String sql = "SELECT * FROM book WHERE " + column + " LIKE ? ORDER BY title";
        List<Book> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setSubject(rs.getString("subject"));
        b.setStatus(BookStatus.valueOf(rs.getString("status")));
        return b;
    }
}