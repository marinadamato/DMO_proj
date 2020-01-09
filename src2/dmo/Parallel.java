package dmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Parallel implements Runnable{
		
		private int n_timeslots;
		private int[][] conflicts;
		private long start;
		private long end;
		private Map<Integer, Exam> exm = new TreeMap<Integer, Exam>();
		private LinkedHashMap<Integer, Exam> randomExms = new LinkedHashMap<Integer, Exam>();
		private Timeslot timeslots[];
		
	public Parallel(int n_timeslots, HashMap<Integer, Exam> exams, int[][] conflicts, long start, long end) {
		this.n_timeslots = n_timeslots;
		//this.timeslotAvailable = new TimeSlot[T];
		this.exm = convertToTreeMap(exams);
		//this.exams = new LinkedHashMap<Integer, Exam>();
		//this.conflict = new int[E][E][PENALTIES];
		this.conflicts = conflicts;
		this.start = start;
		this.end = end * 1000000000 - exams.size() * 100000000 / 3; 
		timeslots = new Timeslot[n_timeslots];
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
		// Set random parameters
		//randomizeExams();
		ArrayList<Integer> list = new ArrayList<>(exm.keySet());
		Collections.shuffle(list);
		list.forEach(k->randomExms.put(k, exm.get(k)));
		
		// First of all, assign all the exam in a greedy way
		//greedyAssignment();
		
		// Then, for the not taken exams, find the first feasible solution
		//feasibleSearch();
		
		// Saving the current solution
		//saveSolution();

		//if(! isFeasibile()) 
			//return;

		// When a feasible solution is found, optimize it
		//optimization();
	}
	
	public static <K, V> Map<K, V> convertToTreeMap(HashMap<K, V> hashMap) 
    { 
        TreeMap<K, V> treeMap = hashMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, 
                      												Map.Entry::getValue,
                      												(oldValue, newValue) -> newValue, 
                      												TreeMap::new)); 
  
        return treeMap; 
    } 
	
	public void initializeSol() {
		for (int i=0; i<n_timeslots; i++)
			timeslots[i] = new Timeslot(i+1, exm.size());
		
	}

}