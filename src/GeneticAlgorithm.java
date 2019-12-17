import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
	
	private int[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_students;
	private int n_time_slots;
	private Integer[][] nEe;
	private double[] fitness;	
	private Random rand = new Random();
	 
	public GeneticAlgorithm(Model model, int n_chrom) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = model.getExms().size();;
		this.n_students = model.getStuds().size();
		this.nEe = model.getnEe();	
		this.population = new int[n_chrom][n_exams];	
		this.fitness = new double[n_chrom];
		this.n_time_slots = model.getN_timeslots();
	}
	
	public void fit_predict() {
		this.initial_population();
		this.print_population();
		this.fitness();
		this.print_fitness();
		
		this.crossover();

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
	
	private void initial_population() {	
		int time_slot = n_time_slots;
		
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
	
	// This method computes fitness for each chromosomes
	private void fitness() {
		float penalty = 0;		
		int distance = 0;
		 
		for(int c=0; c < n_chrom; c++) { // For each chroms
			for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
				for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
					distance = Math.abs(population[c][e1] - population[c][e2]);
					if(distance <= 5) {
						penalty += (2^(5-distance) * this.nEe[e1][e2]);
					}
				}
				penalty = (float) penalty / this.n_students;
				this.fitness[c] =  1 / penalty;				
			}
			
		}
		
	}
	
	private void crossover() {
		int indParent1 = 0, indParent2 = 0;
		double minValueP1 = fitness[0], minValueP2 = fitness[0];
		int[][] parents = new int[2][n_exams];
		
		
		  for(int i=0;i<fitness.length;i++){
			  if(fitness[i] < minValueP1){
				  minValueP1 = fitness[i];
				  indParent1 = i;
				}
		  }
		  
		  parents[0] = population[indParent1];
		  
		  for(int i=0;i<fitness.length;i++){
			  if(fitness[i] < minValueP2 && fitness[i] != minValueP1){
				  minValueP2 = fitness[i];
				  indParent2 = i;
				}
		  }
		  
		  parents[1] = population[indParent2];
		  
		  int crossingSecStart = rand.nextInt(n_exams);
		  int crossingSecEnd = rand.nextInt(n_exams-crossingSecStart) + crossingSecStart;
		  int[][] childs = new int[2][n_exams];
		  // System.out.print(crossingSecStart + " - " + crossingSecEnd );
		  
		  // Swap crossing section two chromosome 
		  for(int i = crossingSecStart; i <= crossingSecEnd; i++) {
			  childs[0][i]= parents[1][i];
			  childs[1][i] = parents[0][i];
		  }
		  
		  // Order Crossover modificato
		  
		  for(int i=0; i<2; i++) {
			  
			  int position = crossingSecEnd+1;
			  int indValue = position;
			  int count = 0;
		  
			  while(position != crossingSecStart ) {
				  if(position == n_exams) 
					  position = 0;
				  
				  if(indValue == n_exams) 
					  indValue = 0;
				  
				  if(!are_conflictual(parents[i][indValue], position, childs[i]) || count>this.n_exams) {
					  childs[i][position] = parents[i][indValue];
					  
					  position++;
					  count = 0;
				  }
				  
				  indValue++;
				  count++;
			  } 
		  }
		  
		  if(isFeasible(childs[0])) 
			  population[indParent1] = childs[0];
		  
		  if(isFeasible(childs[1]))
			  population[indParent2] = childs[1];
		
	}
	
	private boolean isFeasible(int[] chrom) {
		 for(int e = 0; e<this.n_exams; e++) {
			  if(are_conflictual(chrom[e], e, chrom))
				  return false;
		  }
		 
		return true;
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
		for (int i=0; i < n_chrom; i++) {
			System.out.println("ch" + (i+1) + ": " + fitness[i]);
		}
	}
	
	
	 
}