import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.*;
import java.util.Collections;
import java.util.Map.Entry.*;
import java.util.stream.Collectors.*;
import java.util.Comparator;

public class GeneticAlgorithm {
	
	private int[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private Integer[][] nEe;
	private int[] fitness;
	private Map<Integer, Integer> n_conflict_for_exam = new HashMap<>();
	private Map<Integer, Integer> sorted;
	 
	public GeneticAlgorithm(Model model, int n_chrom, int n_exams) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = n_exams;
		this.nEe = model.getnEe();		
		this.population = new int[n_chrom][n_exams];	
		this.fitness = new int[n_chrom];
	}
	
	public void fit_predict() {
		this.initial_population();
		this.print_population();
		this.initial_population_alternative();
	}
	
	private boolean are_conflictual(int time_slot, int exam_id, int[] chrom) {		
		for(int e = 0; e < this.n_exams; e++) {
			if(e != exam_id) {
				if(chrom[e] == time_slot && this.nEe[e][exam_id] != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void initial_population() {		
		Random rand = new Random();
		int time_slot, n_time_slots = model.getN_timeslots();
		
		for(int c=0; c<n_chrom; c++) {
			time_slot = rand.nextInt(n_time_slots);
			for(int e=0; e < n_exams; e++) {
				while( are_conflictual(time_slot, e, population[c])) {
					time_slot++;
					// 
					if(time_slot >= n_time_slots) {
						time_slot = time_slot % n_time_slots;
					}
				}
				population[c][e] = time_slot;
				time_slot++;
				if(time_slot >= n_time_slots) {
					time_slot = time_slot % n_time_slots;
				}
			}
		}
	}
	
	public void initial_population_alternative() {
		for(int i = 0; i < this.n_exams; i++) {
			int count = 0;
			for(int j = 0; j < this.n_exams; j++) {
				if(i != j) {
					if(this.nEe[i][j] != 0) {
						count++;
					}
				}
			}
			n_conflict_for_exam.put(i, count);
		}
		sorted = n_conflict_for_exam.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, HashMap::new));		
	}
	
	// This method computes fitness for each chromosomes
	private void fitness() {
		double penalty = 0;		
		int distance = 0, count = 0;
		 
		for(int c=0; c < n_chrom; c++) { // For each chroms
			for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
				for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
					distance = Math.abs(population[c][e1] - population[c][e2]);
					if(distance <= 5) {
						// count += (2^(5-distance) * this.nEe[e1][e2])/
					}
				}
				
			}	
		}
	}
	
	private void crossover() {
		
	}
	
	private void print_population() {
		int count = 1;
		System.out.println("Population: ");
		for(int[] chrom : population) {
			System.out.println("Chrom " + count + ": " + Arrays.toString(chrom));
			count++;
		}
	}
	
	
	
	 
}
