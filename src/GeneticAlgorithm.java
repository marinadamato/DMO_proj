import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.*;
import java.util.Comparator;

public class GeneticAlgorithm {
	
	private Integer[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_students;
	private int n_time_slots;
	private int[][] nEe;
	private double[] fitness;		
	private Random rand;
	private boolean found;
	private Integer[] chromosome;
	private int nLoop;
	private int returnBack;
	private List<Integer> sortedExmToSchedule;
	private TabuSearch ts;
	private double bestBenchmark;
	private Integer[] bestSolution;
	 
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
		
		
		
		
		
		this.rand  = new Random();
		this.ts = new TabuSearch(this.model);
		this.bestBenchmark = Double.MIN_VALUE;
	}
	
	public boolean existYet(Integer[] chrom) {
		
		for(Integer[] c : population)
			if(Arrays.equals(c, chrom))
				return true;
		
		return false;
	}
	
	public void fit_predict() {
		this.getSortedExmToScheduleByNumStudent();
		this.initial_population_RANDOM();
		this.print_population();
		this.fitness();
		this.print_banchmark();
		this.getSortedExmToScheduleByNumStudent();
		int i = 0;
		
		// crossover fino a scadenza dei 180/300 secondi
		while(true) {
			System.out.print("\n"+ i++ +"th Iteration - Time: "+(System.currentTimeMillis()-model.timeStart)/1000+" second\n");
			
			this.crossover();
			this.fitness();
			// this.print_population();
			// this.print_banchmark();
			

		  if((System.currentTimeMillis()-model.timeStart) > (180*1000)) { // termino il programma dopo 300s 
			  System.out.print("\nBest Bench: "+1/bestBenchmark+/*"\nBest Solution: "+Arrays.toString(bestSolution)+*/"\n");

			this.print_population();
			this.print_banchmark();
			  System.exit(1);
		  }
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
		
	/**
	 * Sort Exams by the number of students enrolled it, to try to assign exams first with 
	 * the biggest average of student in conflict
	 * 
	 */
	private void getSortedExmToScheduleByNumStudent() {
		HashMap<Integer,Double> exmStuds = new HashMap<Integer, Double>();
		
		for(int i = 0; i<this.n_exams;i++)
			exmStuds.put(i, (double) Arrays.stream(nEe[i]).filter( c -> c>0 ).count());//Arrays.stream(nEe[i]).average().getAsDouble());
		
		this.sortedExmToSchedule = exmStuds.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .map(Map.Entry::getKey)
			    .collect(Collectors.toList());
		
		this.sortedExmToSchedule.add(-1);
		
	}
	
	
	private void initial_population_RANDOM() {
		
			for(int c=0; c<n_chrom; c++) {
				do {
				found = false;
				
				chromosome = new Integer[this.n_exams];
				nLoop = 0;
				
				doRecursive(chromosome,0,sortedExmToSchedule.get(0), this.n_exams);
				
				Collections.swap(sortedExmToSchedule, 0, c); 
				// per generare soluzioni il più possibili diverse dopo la creazione 
				// di una soluzine inverto l'ordine di due esami
				
				
				} while(!isFeasible(chromosome) || existYet(chromosome));
				
				population[c] = chromosome.clone();
			}
			
		}
	
	
	/**
	 * Recursive method to generate population with getBestPath Method 
	 * and getSortedExmToScheduleByNumStudent method
	 * @param chrom
	 * @param step
	 * @param exam_id
	 * @param numExamsNotAssignedYet
	 */
	private void doRecursive(Integer[] chrom,int step, int exam_id, int numExamsNotAssignedYet) {
		
		if(numExamsNotAssignedYet > 0 && exam_id>-1) { // finchè non termino gli esami da schedulare
			if(chrom[exam_id]!=null) { // se l'esame ha già assegnato un suo timeslot
				doRecursive(chrom, step+1, sortedExmToSchedule.get(step+1), numExamsNotAssignedYet);
				
				if(returnBack>0 ) {
					returnBack--; 
					return;
				}
			} else {
				for(int i : getBestPath(chrom,exam_id)) { //timeslot
					if(!found) {
						if(!are_conflictual(i, exam_id, chrom)) {
							chrom[exam_id] = i;
							doRecursive(chrom, step+1, sortedExmToSchedule.get(step+1), numExamsNotAssignedYet-1);
							chrom[exam_id] = null;
							
							if(returnBack>0 ) {
								returnBack--; 
								return;
							} else 
								nLoop++;
			
						}
					} else return;
				}
				
				if(!found)
					nLoop++; // every time i fail a complete for cycle
				
				if(nLoop > n_exams && !found)  {
					returnBack = (int) (step*Math.random()); // number of time that i have to go back
					//System.out.print("Step "+ step + " returnBack "+returnBack+ " \n");
					nLoop = 0;
				} 
			}
			
		} else {
			found = true;
			chromosome = chrom.clone();
			//System.out.print("Found ");
		}
	}
	
	
	// metodo per generare l'ordine dei timeslot da inserire nell'esame (indice start), 
	// come nell'order crossover in cui si tenta di inserire un valore nel gene dopo il taglio della sezione,
	// prendendo in ordine i successivi valori finchè non è un valore accettabile
	private int[] getPath(Integer[] parent, int start)	{ 
		//Integer[] path = new Integer[this.n_time_slots];
		List<Integer> path = new ArrayList<Integer>();
		
		for(int k = start; k<this.n_exams; k++) {
			if(!path.contains(parent[k]))
				path.add(parent[k]);
			else if(path.size()==this.n_time_slots)
				break;
			
			if(k==this.n_exams-1)
				k = 0;
		}
		
		return path.stream().mapToInt(i->i).toArray();

	}
	
	// metodo per trovare una soluzione feasible dopo il crossing section
	private void doRecursiveCrossover(Integer[] parent, Integer[] chrom,int step, int exam_id, int numExamsNotAssignedYet) {
		
		if(numExamsNotAssignedYet > 0 && exam_id>-1) {
			if(chrom[exam_id]!=null) 
				doRecursiveCrossover(parent, chrom, step+1, sortedExmToSchedule.get(step+1), numExamsNotAssignedYet);
			else {
			
				for(int i : getPath(parent, exam_id) ) {//Integer i : getBestPath(chrom)) {
					if(!found) {
						if(!are_conflictual(i, exam_id, chrom) ) {
							chrom[exam_id] = i;
							doRecursiveCrossover(parent, chrom, step+1, sortedExmToSchedule.get(step+1), numExamsNotAssignedYet-1);
							chrom[exam_id] = null;
							
							if(returnBack>0 ) {
								returnBack--; 
								return;
							} else 
								nLoop++; // every time i fail to assign i time slot to exam_id
							
			
						}
					} else return;
				}
				
				if(!found)
					nLoop++; // every time i fail a complete for cycle
			}
			
			if(nLoop > n_exams && !found)  {
				returnBack = (int) (step*Math.random()); // number of time that i have to go back
				//System.out.print("Step "+ step + " returnBack "+returnBack+ " \n");
				nLoop = 0;
			} 
			
			
		} else {
			found = true;
			chromosome = chrom.clone();
			//System.out.print("Found ");
		}
	}
	
	
	/**
	 * Find the best order path to schedule timeslot in base al numero totale di studenti che 
	 * sostengono esami già schedulati in un timeslot. L'idea è di cercare prima di schedulare, se possibile, 
	 * un esame nei timeslot più affollati in modo da riservare i restanti timeslot agli esami più conflittuali
	 * @param chrom
	 * @return list of sorted timeslot by the number of students enrolled in the exam assigned yet
	 */
	public List<Integer> getBestPath(Integer[] chrom, int exam) {
		List<Integer> path;
		HashMap<Integer,Integer> numStudentTimeSlot = new HashMap<Integer, Integer>();
		
		for(int k=1; k<=this.n_time_slots;k++)
			numStudentTimeSlot.put(k, 0);
		
		for(int i=0; i<this.n_exams; i++) {
			if( chrom[i] != null ) {
				int numStud = (numStudentTimeSlot.get(chrom[i]) + model.getExms().get(i).getNumber_st_enr());
				numStudentTimeSlot.replace(chrom[i], numStud);
			}
		} 
		
		/*int dist;
        int penalty;
        
        for(int t = 1; t<=this.n_time_slots; t++) {
        	chrom[exam] = t;
        	penalty=0;
        	
	        for (int i=0; i<this.n_exams; i++){
	        	if(chrom[i]!=null && exam!=i) {
	        		dist = Math.abs(chrom[exam]-chrom[i]);
	        		if(dist<=5)
	        			penalty += (Math.pow(2, 5-dist)*model.getnEe()[exam][i]);
	        	}
	        }
	        
	        numStudentTimeSlot.replace(t, penalty);
        } */
		
		path =  numStudentTimeSlot.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .map(Map.Entry::getKey)
			    .collect(Collectors.toList());
		
		// if i just started and my chromosome is empty, i generate a random path
		if(numStudentTimeSlot.values().stream().mapToInt(Integer::intValue).sum() == 0)
			Collections.shuffle(path);
		
		return path;
	}
	

	/**
	 * This method computes fitness for each chromosomes
	 */
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
						penalty += (Math.pow(2, (5-distance))* this.nEe[e1][e2]);
					}
				}
				
			}	
			penalty = penalty / this.n_students;
			this.fitness[c] =  1 / penalty;	
		}			
	}
	
	
		/**
		 * This method computes fitness for a chromosome
		 * @param chrom
		 * @return
		 */
		private double getChromFitness(Integer[] chrom) {
			double penalty = 0;		
			int distance = 0;
			
			for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
					for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
						distance = Math.abs(chrom[e1] - chrom[e2]);
						if(distance <= 5) {
							penalty += ( Math.pow(2, 5-distance) * this.nEe[e1][e2]);
						}
					}
					
				penalty = penalty / this.n_students;
			}	

			return  (1 / penalty);	
		}
		

		
	private void crossover() {
		
		// mi segno il miglior benchmark trovato
		if(Arrays.stream(fitness).filter(c -> c>0).max().getAsDouble() > bestBenchmark)
			bestBenchmark = Arrays.stream(fitness).filter(c -> c>0).max().getAsDouble();
		
		/*for(int i=0; i<this.n_chrom; i++)
			  if(getChromFitness(population[i])>=bestBenchmark) 
				  bestSolution = population[i].clone();*/
				  
		
		 System.out.print("Best Bench: "+1/bestBenchmark+/*"\nBest Solution: "+Arrays.toString(bestSolution)+*/"\n");
		
		
		int indParent1 = 0, indParent2 = 0;
		double minValueP1 = Double.MAX_VALUE, minValueP2 = Double.MAX_VALUE;
		Integer[][] parents = new Integer[2][n_exams];
		
		// Find the two worst fitness in my population
		  for(int i=0;i<this.n_chrom;i++){
			  if(fitness[i] < minValueP1){
				  minValueP1 = fitness[i];
				  indParent1 = i;
				}
		  }
		  parents[0] = population[rand.nextInt(n_chrom)].clone();
		  
		  for(int i=0;i<this.n_chrom;i++){
			  if(fitness[i] < minValueP2 && indParent1!=i && !Arrays.equals(parents[0],population[i]) ){
				  minValueP2 = fitness[i];
				  indParent2 = i;
				}
		  }
		  parents[1] = population[rand.nextInt(n_chrom)].clone(); 
		  
		  // Calculate a random crossing section
		  int crossingSecStart = rand.nextInt(n_exams);
		  int crossingSecEnd = (int) ((n_exams-crossingSecStart-1)*Math.random() + crossingSecStart);
		  Integer[][] childs = new Integer[2][n_exams];
		  
		  // System.out.print("Crossing Section: " + crossingSecStart + " - " + crossingSecEnd + "\n");
		  
		  
		  // Swap crossing section between two chromosome 
		  for(int i = crossingSecStart; i <= crossingSecEnd; i++) {
			  childs[0][i] = parents[1][i];
			  childs[1][i] = parents[0][i];
		  }
		
		  // Order Crossover modified 
		  for(int i=0; i<2; i++) {
			  getSortedExmToScheduleByNumStudent();
			  int k = 0; // contatore di ricorsioni fallite
			  
			  do {
				  int numExamsNotAssignedYet = (this.n_exams-(crossingSecEnd+1-crossingSecStart));
				  found = false;
				  chromosome = new Integer[this.n_exams];
				  
				  // va testato se è meglio la ricorsione del crossover o usare la stessa per generare le soluzioni iniziali
				  doRecursive(childs[i],0,sortedExmToSchedule.get(0), numExamsNotAssignedYet);
				  
				  // se la mia ricorsione è fallita ed è uscita dal ciclo, provo a modificare l'ordine di due esami
				  Collections.swap(sortedExmToSchedule, 0, k++); 
				  
				  if(k > this.n_exams) // se ho fallito più del numero esami, abbandono sezione di taglio e ne provo un'altra
					  return;
			  
			  } while(!isFeasible(chromosome) || existYet(chromosome)) ;
			
			  childs[i] = chromosome.clone();
			  

		  } 
		  
		 
		  
		  // rapporto tra la fitness media e la fitness massima
		  this.fitness();
		  double rapp =  (Arrays.stream(this.fitness).average().getAsDouble() // da teoria libro
				  /Arrays.stream(this.fitness).max().getAsDouble());
		  
		  if(rapp>0.95) { // se è prossimo ad 1, eseguo tabusearch (vanno testati altri valori)
			  System.out.print("\nTS");
			  childs[0] = ts.run(childs[0]).clone();
			  childs[1] = ts.run(childs[1]).clone();
		  } 
		  
		  population[indParent1] = childs[0].clone();
		  population[indParent2] = childs[1].clone();
		  
	}
	
	private boolean isFeasible(Integer[] chrom) {
		 for(int e = 0; e<this.n_exams; e++) {
			  if(chrom[e] == null || are_conflictual(chrom[e], e, chrom))
				  return false;
		  }
		 
		return true;
	}
	
	
	/**
	 * Take the exam that has the worst weight in the fitness formula
	 * @param chrome
	 * @return
	 */
	/*private List<Integer> getBadExams(Integer[] chrome ) {
		List<Integer> idBadExams ;
		HashMap<Integer,Double> sortExam = new HashMap<Integer, Double>();
		int distance;
		double penalty;
		
		for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
			penalty = 0;
			
			for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
				distance = Math.abs(chrome[e1] - chrome[e2]);
				if(distance <= 5) {
					penalty += (Math.pow(2, (5-distance)) * this.nEe[e1][e2]);
				}
			}

			sortExam.put(e1, penalty);
		}
		
		idBadExams = sortExam.entrySet().stream()
	    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
	    .map(Map.Entry::getKey)
	    .collect(Collectors.toList());
		
		return idBadExams;
	}*/
	
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
			System.out.println("Fitness" + (i+1) + ": " + 1/fitness[i]);
		}
	}
	
	private void print_banchmark() {

		System.out.println("Banchmark: ");
		for (int i=0; i < n_chrom; i++) {
			System.out.println("Banchmark" + (i+1) + ": " + model.computePenalty(population[i]));
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