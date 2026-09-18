package lms.model;

import lms.util.Console;

public abstract class Person implements Displayable {

    private int id;
    private String name;
    private String phone;
    private String address;
    private String username;
    private String password;

    public Person() {
    }

    public Person(int id, String name, String phone, String address, String username, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.username = username;
        this.password = password;
    }

    public abstract String getRole();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void display() {
        System.out.printf("  [%s] ID: %-4d  Name: %-20s  Phone: %-12s  Address: %s%n", getRole(), id, name, phone, address);
    }

    @Override
    public String toString() {
        return name + " (" + getRole().toLowerCase() + ", id " + id + ")";
    }
}