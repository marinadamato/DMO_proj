import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
	
	private int[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_students;
	private Integer[][] nEe;
	private double[] fitness;
	 
	public GeneticAlgorithm(Model model, int n_chrom) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = model.getExms().size();
		this.n_students = model.getStuds().size();
		this.nEe = model.getnEe();	
		this.population = new int[n_chrom][n_exams];	
		this.fitness = new double[n_chrom];
	}
	
	public void fit_predict() {
		this.initial_population_RANDOM();
		this.print_population();
		this.fitness();
		this.print_fitness();
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
	
	// it doesn't work with instance01, it runs too much
	private void initial_population() {		
		Random rand = new Random();
		int time_slot, n_time_slots = model.getN_timeslots();
		
		for(int c=0; c<n_chrom; c++) {
			time_slot = rand.nextInt(n_time_slots);
			for(int e=0; e < n_exams; e++) {
				while( are_conflictual(time_slot, e, population[c])) {
					time_slot++;
					// This line makes time_slot to be between 0 and n_time_slots
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
	
	// it is lightspeed, but it makes infeasible solutions
	private void initial_population_RANDOM() {
		Random rand = new Random();
		int n_time_slots = model.getN_timeslots();
		
		for(int c=0; c<n_chrom; c++) {
			for(int e=0; e < n_exams; e++) {
				population[c][e] = rand.nextInt(n_time_slots);
			}
		}
	}
	
	// This method computes fitness for each chromosomes
	private void fitness() {
		double penalty = 0;		
		int distance = 0;
		 
		for(int c=0; c < n_chrom; c++) { // For each chroms
			for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
				for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
					distance = Math.abs(population[c][e1] - population[c][e2]);
					if(distance <= 5) {
						penalty += (2^(5-distance) * this.nEe[e1][e2]);
					}
				}
				penalty = (double) penalty / this.n_students;
				this.fitness[c] = 1 / penalty;				
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
	
	private void print_fitness() {
		System.out.println("Fitness: ");
		for (int i=0; i < n_chrom; i++) {
			System.out.println("ch" + (i+1) + ": " + fitness[i]);
		}
	}

	public int[][] getPopulation() {
		return population;
	}

	public int getN_chrom() {
		return n_chrom;
	}

	public int getN_exams() {
		return n_exams;
	}

	public int getN_students() {
		return n_students;
	}

	public double[] getFitness() {
		return fitness;
	}
	
	
	 
}