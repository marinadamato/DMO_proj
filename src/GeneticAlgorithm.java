import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
	
	private int[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private Integer[][] nEe;
	private int[] fitness;
	 
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
		this.compute_initial_population();
		this.print_population();
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
	
	private void compute_initial_population() {		
		Random rand = new Random();
		int time_slot, n_time_slots = model.getN_timeslots();
		
		for(int c=0; c<n_chrom; c++) {
			time_slot = rand.nextInt(n_time_slots);
			for(int e=0; e < n_exams; e++) {
				while( are_conflictual(time_slot, e, population[c])) {
					time_slot++;
				}
				population[c][e] = time_slot;
				time_slot++;
			}
		}
	}
	
	private void compute_fitness() {
		
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
