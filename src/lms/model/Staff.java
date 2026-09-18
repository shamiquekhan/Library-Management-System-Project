package lms.model;

public abstract class Staff extends Person {

    private double salary;

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("        Salary: %.2f%n", salary);
    }
}