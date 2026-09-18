package lms.model;

public class Librarian extends Staff {

    private int officeNo;

    public int getOfficeNo() {
        return officeNo;
    }

    public void setOfficeNo(int officeNo) {
        this.officeNo = officeNo;
    }

    @Override
    public String getRole() {
        return "LIBRARIAN";
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("        Office: %d%n", officeNo);
    }
}