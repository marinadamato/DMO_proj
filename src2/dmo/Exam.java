package dmo;
import java.io.IOException;
import java.util.*;

public class Exam implements Comparable<Exam> {
	private int id;
	private int nStudents;
	private Timeslot timeslot;
	private boolean assigned;
	private ArrayList<Exam> conflicts = new ArrayList<Exam>();
	private boolean flagIsChanged = false;
	private ArrayList<String> students = new ArrayList<String>();
	private int nAvailableTime;
	
	public Exam(int id, int nStudents) {
		this.id = id;
		this.nStudents = nStudents;
		assigned = false;
	}

	public int getId() {
		return id;
	}

	public int getNStudents() {
		return nStudents;
	}
	
	public void assigned() {
		assigned = true;
	}
	
	public void unassigned() {
		assigned = false;
	}
	
	public boolean isAssigned() {
		return assigned;
	}
	
	public boolean isInConflict(Exam e) {
		if(conflicts.contains(e)) 
		{
			return true;
		}
		return false;
	}
	
	public void addConflict(Exam e) {
		conflicts.add(e);
	}
	
	public ArrayList<Exam> getConflicts() {
		return conflicts;
	}
	
	public void setTimeslot(Timeslot timeslot) {
		this.timeslot = timeslot;
	}
	
	public Timeslot getTimeslot() {
		return timeslot;
	}
	
	public void setChange() {
		flagIsChanged = true;
	}
	
	public void setNoChange() {
		flagIsChanged = false;
	}
	
	public boolean getChange() {
		return flagIsChanged;
	}

	public ArrayList<String> getStudents() {
		return students;
	}
	
	public void addStudent(String s) throws IOException {
		students.add(s);
		if (this.students.size() > this.nStudents)
			throw new IOException();
	}
	
	public void setStudents(ArrayList<String> students) {
		this.students = students;
	}
	
	public int getNAvailable() {
		return this.getNAvailable();
	}
	
	public void incNAvailable() {
		this.nAvailableTime++;
	}
	
	public void decAvailable() {
		this.nAvailableTime--;
	}
	
	public int compareTo(Exam e) {
		if(this.getNAvailable()<e.getNAvailable()) {
			return 1; 
		}else if(this.getNAvailable()>e.getNAvailable()) {
			return -1;
		}
		else {
			if(this.getConflicts().size()>e.getConflicts().size()) {
				return 1;
			}
			else if(this.getConflicts().size()<e.getConflicts().size()) {
				return -1;
			}
			else {
				if(this.getNStudents()>=e.getNStudents()) {
					return 1;
				}
				else
					return -1;
			}
		}
	}
}
