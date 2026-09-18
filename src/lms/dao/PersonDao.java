package lms.dao;

import lms.model.Clerk;
import lms.model.Librarian;
import lms.model.Member;
import lms.model.Person;
import lms.util.Config;
import lms.util.Database;
import lms.util.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PersonDao {

    public int insert(Connection c, Person p) throws SQLException {
        String sql = "INSERT INTO person (name, phone, address, username, password, role, salary, desk_no, office_no) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setString(3, p.getAddress());
            ps.setString(4, p.getUsername());
            ps.setString(5, p.getPassword());
            ps.setString(6, p.getRole());
            if (p instanceof Librarian) {
                ps.setDouble(7, ((Librarian) p).getSalary());
                ps.setNull(8, Types.INTEGER);
                ps.setInt(9, ((Librarian) p).getOfficeNo());
            } else if (p instanceof Clerk) {
                ps.setDouble(7, ((Clerk) p).getSalary());
                ps.setInt(8, ((Clerk) p).getDeskNo());
                ps.setNull(9, Types.INTEGER);
            } else {
                ps.setNull(7, Types.DOUBLE);
                ps.setNull(8, Types.INTEGER);
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public Person findByUsername(Connection c, String username) throws SQLException {
        String sql = "SELECT * FROM person WHERE username=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public Person findById(Connection c, int id) throws SQLException {
        String sql = "SELECT * FROM person WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Person> findAllMembers(Connection c) throws SQLException {
        String sql = "SELECT * FROM person WHERE role='MEMBER' ORDER BY name";
        List<Person> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean usernameExists(Connection c, String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM person WHERE username=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void updatePassword(Connection c, int id, String newPassword) throws SQLException {
        String sql = "UPDATE person SET password=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateMemberProfile(Connection c, int id, String phone, String address) throws SQLException {
        String sql = "UPDATE person SET phone=?, address=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, address);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    private Person mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        Person p;
        switch (role) {
            case "LIBRARIAN":
                Librarian lib = new Librarian();
                lib.setOfficeNo(rs.getInt("office_no"));
                lib.setSalary(rs.getDouble("salary"));
                p = lib;
                break;
            case "CLERK":
                Clerk cl = new Clerk();
                cl.setDeskNo(rs.getInt("desk_no"));
                cl.setSalary(rs.getDouble("salary"));
                p = cl;
                break;
            default:
                p = new Member();
                break;
        }
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));
        p.setUsername(rs.getString("username"));
        p.setPassword(rs.getString("password"));
        return p;
    }
}