package dmo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Random;

public class Parallel implements Runnable{
		
		private int n_timeslots;
		private int[][] conflicts;
		private long start;
		private long end;
		//private Map<Integer, Exam> exm = new TreeMap<Integer, Exam>();
		private LinkedHashMap<Integer, Exam> randomExams = new LinkedHashMap<Integer, Exam>();
		private LinkedHashMap<Integer, Exam> exms = new LinkedHashMap<Integer, Exam>();
		private Timeslot timeslots[];
		private int toSchedule;
		private ArrayList<Exam> unassigned = new ArrayList<Exam>();
		private ArrayList<TLelement> feasibleTL = new ArrayList<TLelement>();
		private int minConflict = Integer.MAX_VALUE, prevConflicts = Integer.MAX_VALUE;
		private int TLsize = 100;
		private int n_iteration = 0;
		private int[] solution;
		private int minExamWithoutTimeslot;
		
	public Parallel(int n_timeslots, HashMap<Integer, Exam> exams, int[][] conflicts, long start, long end) {
		this.n_timeslots = n_timeslots;
		//this.timeslotAvailable = new TimeSlot[T];
		//this.exm = convertToTreeMap(exams);
		//this.exams = new LinkedHashMap<Integer, Exam>();
		//this.conflict = new int[E][E][PENALTIES];
		this.conflicts = conflicts;
		this.start = start;
		this.end = end * 1000000000 - exams.size() * 100000000 / 3; 
		timeslots = new Timeslot[n_timeslots];
		for (int i=0; i<n_timeslots; i++)
			timeslots[i] = new Timeslot(i+1, exams.size());
		toSchedule=exams.size();
		this.solution=new int[exams.size()];
		this.minExamWithoutTimeslot=exams.size();
		buildExamMap(exams);
		this.unassigned=new ArrayList<Exam>(exms.values());
		ArrayList<Integer> list = new ArrayList<>(exms.keySet());
		Collections.shuffle(list);
		list.forEach(k->randomExams.put(k, exms.get(k)));
		buildExamsConflicts();
		/*setTimeSlot();
		
		this.solution = new int[E];
		tabooList = new TabooList(100);
		optimizationTabooList = new TabooList(100);
		
		numberExamsWithoutTimeslot = E;
		minExamWithoutTimeslot = E;*/
	}
	
	@Override
	public void run() {
	
		initialSol();
		
		// Then, for the not taken exams, find the first feasible solution
		feasibleSol();
		
		// Saving the current solution
		saveSolution();

		if(isFeasible()) 
			System.out.println("trovata");
		else
			System.out.println("schifo");

		// When a feasible solution is found, optimize it
		//optimization();
	}
	
	/*public static <K, V> Map<K, V> convertToTreeMap(HashMap<K, V> hashMap) 
    { 
        TreeMap<K, V> treeMap = hashMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, 
                      												Map.Entry::getValue,
                      												(oldValue, newValue) -> newValue, 
                      												TreeMap::new)); 
  
        return treeMap; 
    } */
	private void buildExamMap(Map<Integer, Exam> exams) {
		for (Integer id : exams.keySet()) {
			Exam exam = exams.get(id);
			Exam e = new Exam(id, exam.getNStudents());
			this.exms.put(id, e);
		}
	}
	
	private void buildExamsConflicts() {
		for (Exam e1 : this.randomExams.values()) {
			int id1 = e1.getId() - 1;
			for (Exam e2 : this.randomExams.values()) {
				int id2 = e2.getId() - 1;
				if (conflicts[id1][id2] > 0) {
					if (!e1.isInConflict(e2)) {
						e1.addConflict(e2);;
					}
					if (!e2.isInConflict(e1)) {
						e2.addConflict(e1);
					}
				}
			}
		}
	}
	
	public void initialSol() {
		for(int i=0; i<n_timeslots; i++) {
			for(Exam e:randomExams.values()) {
				if(!e.isAssigned()) {
					if(timeslots[i].isConflict(e.getId()) == false) {
						timeslots[i].addExam(e);
						e.setTimeslot(timeslots[i]);
						toSchedule--;
						unassigned.remove(e);
					}
				}
			}
		}
	}
	
	public void feasibleSol() {
		while(!unassigned.isEmpty() && (System.nanoTime()-start)<end) {
			if(n_iteration==1000)
				changeSol();
				
			Collections.sort(unassigned);	
			insertUnassigned();
			updateControl();
			if (minExamWithoutTimeslot < prevConflicts) {
				prevConflicts = minExamWithoutTimeslot;
			}
			/*if(toSchedule<prevConflicts) {
				prevConflicts = toSchedule;
				n_iteration+=-300;
				if(toSchedule<5)
					n_iteration+=-5000;
			}*/
			n_iteration++;
		}
	}
	
	private void updateControl() {
		if (toSchedule < minExamWithoutTimeslot) {
			minExamWithoutTimeslot = toSchedule;
			n_iteration = 0;
			if (minExamWithoutTimeslot < prevConflicts + 3) {
				n_iteration = -1000;
			}
			if (minExamWithoutTimeslot <= 3) {
				n_iteration = -10000;
			}
			if (minExamWithoutTimeslot <= 1) {
				n_iteration = -20000;
			}
		}
	}
	
	public void insertUnassigned() {
		int nConflicts;
		Timeslot actualTS= null;
		//ArrayList<Exam> newConflicts = new ArrayList<Exam>();
		ArrayList<Exam> setUnassigned = new ArrayList<Exam>();
		for(Exam e:unassigned) {
			for(int i=0; i<n_timeslots; i++) {
				nConflicts = timeslots[i].getNConflicts(e.getId());
				TLelement el = new TLelement(e.getId(), timeslots[i].getId());
				//TODO: ottimizzare per la funzione obiettivo
				if(nConflicts<minConflict && (!feasibleTL.contains(el) || (toSchedule + nConflicts -1) < prevConflicts)) {
					minConflict = nConflicts;
					actualTS = timeslots[i];
				}
			}
			//newConflicts.clear();
			//for(Exam slotExam:actualTS.getExams()) {
			for(int i=0; i<actualTS.getExams().size(); i++) {
				if(actualTS.getExams().get(i).isInConflict(e)) {
					//newConflicts.add(actualTS.getExams().get(i));
					TLelement tl = new TLelement(actualTS.getExams().get(i).getId(), actualTS.getId());	
					toSchedule++;
					this.addTLelement(feasibleTL, tl);
					setUnassigned.add(actualTS.getExams().get(i));
					actualTS.removeExam(actualTS.getExams().get(i));
				}
			}
			actualTS.addExam(e);
			e.setTimeslot(actualTS);
			toSchedule--;
			minConflict = Integer.MAX_VALUE;
		}
		
		unassigned.clear();
		setUnassigned.forEach(el -> unassigned.add(el));
	}
	
	public void changeSol() {
		Random rand = new Random();
		ArrayList<Exam> l1 = new ArrayList<>();
		ArrayList<Exam> l2 = new ArrayList<>();
		int id1, id2;
		
		do {	
			id1 = rand.nextInt(n_timeslots);
			id2 = rand.nextInt(n_timeslots);
			l1.clear();
			l2.clear();
			for(Exam e:timeslots[id1].getExams()) {
				if(!timeslots[id2].isConflict(e.getId())) {
					l1.add(e);
					this.addTLelement(feasibleTL, new TLelement(e.getId(), timeslots[id1].getId()));
				}
			}
			for(Exam e:timeslots[id2].getExams()) {
				if(!timeslots[id1].isConflict(e.getId())) {
					l2.add(e);
					this.addTLelement(feasibleTL, new TLelement(e.getId(), timeslots[id2].getId()));
				}
			}
		}while(l1.size()!=0 && l2.size()!=0);
		
		for(Exam e:l1) {
			timeslots[id2].addExam(e);
			e.setTimeslot(timeslots[id2]);
			timeslots[id1].removeExam(e);
		}
		for(Exam e:l2) {
			timeslots[id1].addExam(e);
			e.setTimeslot(timeslots[id1]);
			timeslots[id2].removeExam(e);
		}
		
		l1.clear();
		l2.clear();
	}
	
	public void addTLelement(ArrayList<TLelement> tl, TLelement t) {
		  tl.add(t);
		  if(tl.size()>TLsize)
		   tl.remove(0);
	 }
	
	public boolean isFeasible() {
		for(int i = 0; i < randomExams.size(); ++i) {
			if(solution[i] < 1 || solution[i] > n_timeslots) // Invalid timeslot
				return false;
			for(int j = 0; j < randomExams.size(); ++j) {
				if(solution[j] < 1 || solution[j] > n_timeslots)  // Invalid timeslot
					return false;
				if(solution[i] == solution[j] && conflicts[i][j] > 0) // Exam in the same timeslot and students in conflict
					return false;
			}
		}
		return true;
	}

	
	private void saveSolution() {
		for (Exam e : randomExams.values()) {
			solution[e.getId() - 1] = e.getTimeslot().getId();
		}
	}
	
	
}