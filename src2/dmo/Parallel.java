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
		private Timeslot timeslots[];
		private int toSchedule;
		private ArrayList<Exam> unassigned = new ArrayList<Exam>();
		private ArrayList<TLelement> feasibleTL = new ArrayList<TLelement>();
		private int minConflict = Integer.MAX_VALUE, prevConflicts = Integer.MAX_VALUE;
		private int TLsize = 100;
		
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
		ArrayList<Integer> list = new ArrayList<>(exams.keySet());
		Collections.shuffle(list);
		list.forEach(k->randomExams.put(k, exams.get(k)));
		this.unassigned=(ArrayList<Exam>) exams.values();
		/*buildExamMap(exams);
		buildExamsConflicts();
		setTimeSlot();
		
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
		//feasibleSearch();
		
		// Saving the current solution
		//saveSolution();

		//if(! isFeasibile()) 
			//return;

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
	
	public void initialSol() {
		for(int i=0; i<n_timeslots; i++) {
			for(Exam e:randomExams.values()) {
				if(!e.isAssigned()) {
					if(timeslots[i].isConflict(e.getId()) == false) {
						timeslots[i].addExam(e);
						e.setTimeslot(timeslots[i]);
						toSchedule--;
						unassigned.remove(e);
						Collections.sort(unassigned);
					}
				}
			}
		}
	}
	
	public void feasibleSol() {
		while(toSchedule!=0 && (System.nanoTime()-start)<end) {
			insertUnassigned();
		}
	}
	
	public void insertUnassigned() {
		int nConflicts;
		Timeslot actualTS=null;
		ArrayList<Exam> newConflicts = new ArrayList<Exam>();
		ArrayList<Exam> setUnassigned = new ArrayList<Exam>();
		for(Exam e:unassigned) {
			for(int i=0; i<n_timeslots; i++) {
				nConflicts = timeslots[i].getNConflicts(e.getId());
				TLelement el = new TLelement(e.getId(), timeslots[i].getId());
				//TODO: ottimizzare per la funzione obiettivo
				if((nConflicts<minConflict && !feasibleTL.contains(el)) || (toSchedule + nConflicts -1 < prevConflicts)) {
					minConflict = nConflicts;
					actualTS = timeslots[i];
				}
			}
			newConflicts.clear();
			for(Exam slotExam:actualTS.getExams()) {
				if(slotExam.isInConflict(e)) {
					newConflicts.add(slotExam);
					actualTS.removeExam(slotExam);
					toSchedule++;
					this.addTLelement(feasibleTL, new TLelement(slotExam.getId(), actualTS.getId()));
					setUnassigned.add(slotExam);
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
			id1 = rand.nextInt(n_timeslots+1);
			id2 = rand.nextInt(n_timeslots+1);
			l1.clear();
			l2.clear();
			for(Exam e:timeslots[id1-1].getExams()) {
				if(!timeslots[id2-1].isConflict(e.getId())) {
					l1.add(e);
					this.addTLelement(feasibleTL, new TLelement(e.getId(), timeslots[id1-1].getId()));
				}
			}
			for(Exam e:timeslots[id2-1].getExams()) {
				if(!timeslots[id1-1].isConflict(e.getId())) {
					l2.add(e);
					this.addTLelement(feasibleTL, new TLelement(e.getId(), timeslots[id2-1].getId()));
				}
			}
		}while(l1.size()!=0 && l2.size()!=0);
		
		for(Exam e:l1) {
			timeslots[id2-1].addExam(e);
			e.setTimeslot(timeslots[id2-1]);
			timeslots[id1-1].removeExam(e);
		}
		for(Exam e:l2) {
			timeslots[id1-1].addExam(e);
			e.setTimeslot(timeslots[id1-1]);
			timeslots[id2-1].removeExam(e);
		}
		
		l1.clear();
		l2.clear();
	}
	
	public void addTLelement(ArrayList<TLelement> tl, TLelement t) {
		  tl.add(t);
		  if(tl.size()>TLsize)
		   tl.remove(0);
		 }
	
	

}