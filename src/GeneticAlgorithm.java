import java.util.Arrays;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.*;
import java.util.Collections;
import java.util.Map.Entry.*;
import java.util.stream.Collectors.*;
import java.util.Comparator;

public class GeneticAlgorithm {
	
	private Integer[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_students;
	private int n_time_slots;
	private Integer[][] nEe;
	private Map<Integer, Integer> n_conflict_for_exam = new HashMap<>();
	private Map<Integer, Integer> sorted;
	private double[] fitness;		
	private Random rand;
	private boolean find;
	private Integer[] chromosome;
	 
	public GeneticAlgorithm(Model model, int n_chrom) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = model.getExms().size();
		this.n_students = model.getStuds().size();
		this.nEe = model.getnEe();	
		this.population = new Integer[n_chrom][n_exams];	
		this.fitness = new double[n_chrom];
		this.n_time_slots = model.getN_timeslots();
		this.n_exams = model.getExms().size();
		this.n_students = model.getStuds().size();
		this.nEe = model.getnEe();	
		this.population = new Integer[n_chrom][n_exams];	
		this.fitness = new double[n_chrom];
		this.n_time_slots = model.getN_timeslots();
		this.rand  = new Random();
	}
	
	public void fit_predict() {
		this.initial_population_RANDOM();
		this.print_population();
		this.fitness();
		this.print_fitness();
		
		for(int i = 1; i<10; i++) {
			System.out.print("\n\n"+ i +"th Iteration \n");
			this.crossover();
			this.print_population();
			this.fitness();
			this.print_fitness();
		}

	}
	
	private boolean are_conflictual(int time_slot, int exam_id, Integer[] chrom) {		
		for(int e = 0; e < this.n_exams; e++) {
			if(e != exam_id && chrom[e]!=null) {
				if(chrom[e] == time_slot && this.nEe[e][exam_id] != 0) {
					return true;
				}
			}
		}
		return false;
	}

	// it doesn't work with instance01, it runs too much
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
			//System.out.print(isFeasible(population[c]))	;
		}
	}
	
	
	// it is lightspeed, but it makes infeasible solutions
		private void initial_population_RANDOM() {
			/*Random rand = new Random();
			int n_time_slots = model.getN_timeslots();
			
			for(int c=0; c<n_chrom; c++) {
				for(int e=0; e < n_exams; e++) {
					population[c][e] = rand.nextInt(n_time_slots);
				}
			}*/
			
			for(int c=0; c<n_chrom; c++) {
				find = false;
				int exam_id = rand.nextInt(this.n_exams);
				chromosome = new Integer[this.n_exams];
				
				recursive(population[c],exam_id, this.n_exams);
				population[c] = chromosome.clone();
			}
			
		}
		
	private void recursive(Integer[] chrom, int exam_id, int numExamsNotAssignedYet) {
		
		if(exam_id == this.n_exams)
			exam_id = 0;
		
		if(numExamsNotAssignedYet > 0) {
			// Try to assign every time-slot at this exam
			for(int i =0; i< this.n_time_slots; i++) {
				if(!are_conflictual(i, exam_id, chrom)) {
					if(!find) {
						chrom[exam_id] = i;
						recursive(chrom, exam_id+1, numExamsNotAssignedYet-1);
						chrom[exam_id] = null;
					} else break;
				}
			}
		} else if(isFeasible(chrom)) {
			find = true;
			chromosome = chrom.clone();
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
		/*sorted = n_conflict_for_exam.entrySet().stream()
		 * .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		 * .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, HashMap::new));		
		 */
	}
	
	// This method computes fitness for each chromosomes
	private void fitness() {
		double penalty;		
		int distance;
		 
		for(int c=0; c < n_chrom; c++) { // For each chroms
			penalty = 0;
			distance = 0;
			
			for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
				for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
					distance = Math.abs(population[c][e1] - population[c][e2]);
					if(distance <= 5) {
						penalty += (2^(5-distance) * this.nEe[e1][e2]);
					}
				}
				
			}	
			penalty = penalty / this.n_students;
			this.fitness[c] =  1 / penalty;	
		}			
	}
		
	
	private void crossover() {
		int indParent1 = 0, indParent2 = 0;
		double minValueP1 = fitness[0], minValueP2 = fitness[0];
		Integer[][] parents = new Integer[2][n_exams];
		
		
		  for(int i=0;i<fitness.length;i++){
			  if(fitness[i] < minValueP1){
				  minValueP1 = fitness[i];
				  indParent1 = i;
				}
		  }
		  parents[0] = population[indParent1];
		  
		  for(int i=0;i<fitness.length;i++){
			  if(fitness[i] < minValueP2 && indParent1!=i && parents[0] != population[i] ){
				  minValueP2 = fitness[i];
				  indParent2 = i;
				}
		  }
		  parents[1] = population[indParent2];
		  
		  int crossingSecStart = rand.nextInt(n_exams-1);
		  int crossingSecEnd = rand.nextInt(n_exams-crossingSecStart-1) + crossingSecStart;
		  Integer[][] childs = new Integer[2][n_exams];
		  System.out.print("Crossing Section: " + crossingSecStart + " - " + crossingSecEnd + "\n");
		  
		  // Swap crossing section two chromosome 
		  for(int i = crossingSecStart; i <= crossingSecEnd; i++) {
			  childs[0][i]= parents[1][i];
			  childs[1][i] = parents[0][i];
		  }
		  
		  // Order Crossover modificato
		  
		  for(int i=0; i<2; i++) {
			  
			  int position = crossingSecEnd+1;
			  //int indValue = position;
			  int numExamsNotAssignedYet = (this.n_exams-(position-crossingSecStart));
			  find = false;
			  chromosome = new Integer[this.n_time_slots];
			  
			  recursive(childs[i],position, numExamsNotAssignedYet);
			  childs[i] = chromosome.clone();		  
		  }
		  
		  if(isFeasible(childs[0])) 
			  population[indParent1] = childs[0];
		  
		  if(isFeasible(childs[1]))
			  population[indParent2] = childs[1];
		
	}
	
	private boolean isFeasible(Integer[] chrom) {
		 for(int e = 0; e<this.n_exams; e++) {
			  if(are_conflictual(chrom[e], e, chrom))
				  return false;
		  }
		 
		return true;
	}
	
	private void print_population() {
		int count = 1;
		System.out.println("Population: ");
		for(Integer[] chrom : population) {
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

	public Integer[][] getPopulation() {
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