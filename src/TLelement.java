public class TLelement {
    private Exam e;
    private int timeslot;

    public TLelement(Exam e, int timeslot) {
        this.e = e;
        this.timeslot = timeslot;
    }

    public Exam getE() {
        return e;
    }
    public int getTimeslot() {
        return timeslot;
    }
    public void setE(Exam e) {
        this.e = e;
    }
    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

}
