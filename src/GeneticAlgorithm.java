import java.util.Arrays;
import java.util.Random;

public class GeneticAlgorithm {
	
	private Integer[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_students;
	private int n_time_slots;
	private Integer[][] nEe;
	private double[] fitness;	
	private Random rand = new Random();
	private boolean find;
	 
	public GeneticAlgorithm(Model model, int n_chrom) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = model.getExms().size();;
		this.n_students = model.getStuds().size();
		this.nEe = model.getnEe();	
		this.population = new Integer[n_chrom][n_exams];	
		this.fitness = new double[n_chrom];
		this.n_time_slots = model.getN_timeslots();
	}
	
	public void fit_predict() {
		this.initial_population_RANDOM();
		this.print_population();
		this.fitness();
		this.print_fitness();
		
		for(int i = 0; i<2; i++) {
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
				int exam_id = rand.nextInt(this.n_exams-1);
				recursive(population[c],exam_id, c, 0);
			}
			
		}
		
	private void recursive(Integer[] chrom, int exam_id, int indChrom, int eAssigned) {
		
		if(exam_id == this.n_exams)
			exam_id = 0;
		
		if(eAssigned < this.n_exams) {
			for(int i =0; i< this.n_time_slots; i++) {
				if(!are_conflictual(i, exam_id, chrom)) {
					if(!find) {
						chrom[exam_id] = i;
						recursive(chrom, exam_id+1, indChrom, eAssigned+1);
						chrom[exam_id] = null;
						
						//i =rand.nextInt(n_time_slots-1);
					} else break;
				}
			}
		} else if(isFeasible(chrom)) {
			find = true;
			population[indChrom] = chrom.clone();
		}
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
			  int indValue = position;
			  int count = 0;
		  
			  do {
				  
				  if(!are_conflictual(parents[i][indValue], position, childs[i])) {
					  childs[i][position] = parents[i][indValue];
					  
					  position++;
					  count = 0;
				  } else if (count>=this.n_exams) {
					  int randTime = rand.nextInt(n_time_slots);
					  if(!are_conflictual(randTime, position, childs[i])) {
						  childs[i][position] = randTime;
					  
						  position++;
						  count = 0;
					  }
				  }
				  
				  indValue++;
				  count++;
				  
				  if(position == n_exams) 
					  position = 0;
				  
				  if(indValue == n_exams) 
					  indValue = 0;
				  
			  } while(position != crossingSecStart );
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
		for (int i=0; i < n_chrom; i++) {
			System.out.println("ch" + (i+1) + ": " + fitness[i]);
		}
	}
	
	
	 
}