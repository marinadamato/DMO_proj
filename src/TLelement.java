public class TLelement {
    private Integer e;
    private int timeslot;

    public TLelement(Integer e, int timeslot) {
        this.e = e;
        this.timeslot = timeslot;
    }

    public Integer getE() {
        return e;
    }
    public int getTimeslot() {
        return timeslot;
    }
    public void setE(Integer e) {
        this.e = e;
    }
    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }
    public String toString() {
    	return this.e + " in timeslot " + this.timeslot + "\n";
    }
}
