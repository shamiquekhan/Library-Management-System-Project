package lms.model;

public class Member extends Person {

    public Member() {
    }

    public Member(int id, String name, String phone, String address, String username, String password) {
        super(id, name, phone, address, username, password);
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }
}