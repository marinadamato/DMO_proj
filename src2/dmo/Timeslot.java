package dmo;

public class Timeslot {
		private int id;
		private int nConflicts[];
		
		public Timeslot(int id, int nExams) {
			this.id=id;
			nConflicts = new int[nExams];
		}
}
