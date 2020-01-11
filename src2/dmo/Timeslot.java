package dmo;

import java.util.ArrayList;

public class Timeslot {
		private int id;
		private int nConflicts[];
		private ArrayList<Exam> exams = new ArrayList<>();
		
		public Timeslot(int id, int nExams) {
			this.id=id;
			nConflicts = new int[nExams];
		}
		
		public void addExam(Exam e) {
			exams.add(e);
			e.getConflicts().forEach(c -> addConflict(c.getId()));
		}
		
		public void removeExam(Exam e) {
			exams.remove(e);
			e.getConflicts().forEach(c -> removeConflict(c.getId()));
		}
		
		public boolean isConflict(int x) {
			if(nConflicts[x-1]!=0) {
				return true;
			}
			return false;
		}
		
		public void addConflict(int x) {
			nConflicts[x-1]++;
		}
		
		public void removeConflict(int x) {
			nConflicts[x-1]--;
		}
		
		public int getNConflicts(int x) {
			return nConflicts[x-1];
		}
		
		public int getId() {
			return this.id;
		}
		
		public ArrayList<Exam> getExams() {
			return this.exams;
		}
		
}
