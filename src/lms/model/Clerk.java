package lms.model;

public class Clerk extends Staff {

    private int deskNo;

    public int getDeskNo() {
        return deskNo;
    }

    public void setDeskNo(int deskNo) {
        this.deskNo = deskNo;
    }

    @Override
    public String getRole() {
        return "CLERK";
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("        Desk: %d%n", deskNo);
    }
}