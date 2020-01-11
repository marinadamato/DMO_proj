import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.*;
import java.util.Comparator;

public class GeneticAlgorithm implements Runnable {

	private Integer[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private int n_time_slots;
	private int[][] nEe;
	private double[] penalty;
	private Random rand;
	private boolean found;
	private Integer[] chromosome;
	private int nLoop;
	private int returnBack;
	private List<Integer> sortedExmsToSchedule;
	private List<Integer> ExmsToSchedule;
	private TabuSearch ts;
	private int counter_iteration;
	private long lastBenchFound;
	private long tlim;
	private int minimum_cut;

	public GeneticAlgorithm(Model model, int n_chrom,long tlim) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = model.getExms().size();
		this.nEe = model.getnEe();
		this.population = new Integer[n_chrom][n_exams];
		this.penalty = new double[n_chrom];
		this.n_time_slots = model.getN_timeslots();
		this.rand = new Random();
		this.ts = new TabuSearch(this.model);
		this.tlim=tlim;
		this.minimum_cut=(int) Math.round(this.n_exams * 0.1);
	}

	public boolean existYet(Integer[] chrom) {

		for (Integer[] c : population)
			if (Arrays.equals(c, chrom))
				return true;

		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.initial_population();
		double optPenalty = Double.MAX_VALUE;

		while (true) {
			this.crossover();
			this.calculatePenaltyPop();
			
			// rapporto tra la fitness media e la fitness massima
			double ratio = (Arrays.stream(this.penalty).min().getAsDouble())
					/Arrays.stream(this.penalty).average().getAsDouble(); // da teoria libro
						
						
			if (Arrays.stream(this.penalty).min().getAsDouble() < optPenalty) {
				optPenalty = model.getOptPenalty();
				lastBenchFound = System.currentTimeMillis();
			}

			if (ratio > 0.997  &&  (System.currentTimeMillis() - lastBenchFound) > (90 * 1000)  ) {// da teoria libro
				for (int c = 0; c<this.n_chrom/2;c++) { //: getIndexBadChroms()) {
					do {
						found = false;

						chromosome = new Integer[this.n_exams];
						nLoop = 0;

						doRecursive(chromosome, 0, sortedExmsToSchedule.get(0), this.n_exams);

						Collections.swap(sortedExmsToSchedule, 0, c);

					} while (!isFeasible(chromosome) || existYet(chromosome));
					
					population[c] = chromosome.clone();
				}
				
				lastBenchFound = Long.MAX_VALUE;
			}
		}

	}

	/**
	 * Sort Exams by the number of students enrolled it, to try to assign exams
	 * first with the biggest average of student in conflict
	 * 
	 */
	private void getSortedExmToScheduleByNumStudent() {
		HashMap<Integer, Double> exmStuds = new HashMap<Integer, Double>();

		for (int i = 0; i < this.n_exams; i++)
			exmStuds.put(i, (double) Arrays.stream(nEe[i]).filter(c -> c > 0).count());// Arrays.stream(nEe[i]).average().getAsDouble());

		this.ExmsToSchedule = exmStuds.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		this.ExmsToSchedule.add(-1);

	}
	
	private void initial_population() {
		this.getSortedExmToScheduleByNumStudent();
		this.sortedExmsToSchedule = new ArrayList<Integer>(ExmsToSchedule);

		for (int c = 0; c < n_chrom; c++) {
			do {
				found = false;

				chromosome = new Integer[this.n_exams];
				nLoop = 0;

				doRecursive(chromosome, 0, sortedExmsToSchedule.get(0), this.n_exams);

				Collections.swap(sortedExmsToSchedule, 0, c);
				// per generare soluzioni il più possibili diverse dopo la creazione
				// di una soluzine inverto l'ordine di due esami

			} while (!isFeasible(chromosome) || existYet(chromosome));
			
			
			model.isNewOpt(chromosome);
			
			population[c] = chromosome.clone();
		}

	}

	/**
	 * Recursive method to generate population with getBestPath Method and
	 * getSortedExmToScheduleByNumStudent method
	 * 
	 * @param chrom
	 * @param step
	 * @param exam_id
	 * @param numExamsNotAssignedYet
	 */
	private void doRecursive(Integer[] chrom, int step, int exam_id, int numExamsNotAssignedYet) {

		if (numExamsNotAssignedYet > 0 && exam_id > -1) { // finchè non termino gli esami da schedulare
			if (chrom[exam_id] != null) { // se l'esame ha già assegnato un suo timeslot
				doRecursive(chrom, step + 1, sortedExmsToSchedule.get(step + 1), numExamsNotAssignedYet);

				if (returnBack > 0) {
					returnBack--;
					return;
				}
				
			} else {
				for (int i : getBestPath(chrom)) { // timeslot
					if (!found) {
						if (!model.areConflictual(i, exam_id, chrom)) {
							chrom[exam_id] = i;
							doRecursive(chrom, step + 1, sortedExmsToSchedule.get(step + 1), numExamsNotAssignedYet - 1);
							chrom[exam_id] = null;

							if (returnBack > 0) {
								returnBack--;
								return;
							} 

						}
					} else
						return;
				}

				if (!found)
					nLoop++; // every time i fail a complete for cycle

				if (nLoop > n_exams/2 && !found) {
					returnBack = (int) (step * Math.random());
					nLoop = 0;
				}
			}

		} else {
			found = true;
			chromosome = chrom.clone();
		}
	}

	/**
	 * Find the best order path to schedule timeslot in base al numero totale di
	 * studenti che sostengono esami già schedulati in un timeslot. L'idea è di
	 * cercare prima di schedulare, se possibile, un esame nei timeslot più
	 * affollati in modo da riservare i restanti timeslot agli esami più
	 * conflittuali
	 * 
	 * @param chrom
	 * @return list of sorted timeslot by the number of students enrolled in the
	 *         exam assigned yet
	 */
	public List<Integer> getBestPath(Integer[] chrom) {
		List<Integer> path;
		HashMap<Integer, Integer> numStudentTimeSlot = new HashMap<Integer, Integer>();

		for (int k = 1; k <= this.n_time_slots; k++)
			numStudentTimeSlot.put(k, 0);

		for (int i = 0; i < this.n_exams; i++) {
			if (chrom[i] != null) {
				int numStud = (numStudentTimeSlot.get(chrom[i]) + model.getExms().get(i).getNumber_st_enr());
				numStudentTimeSlot.replace(chrom[i], numStud);
			}
		}

		path = numStudentTimeSlot.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.map(Map.Entry::getKey).collect(Collectors.toList());

		// if i just started and my chromosome is empty, i generate a random path
		if (numStudentTimeSlot.values().stream().mapToInt(Integer::intValue).sum() == 0)
			Collections.shuffle(path);

		return path;
	}

	/**
	 * This method computes penalty for each chromosomes
	 */
	private void calculatePenaltyPop() {

		for (int c = 0; c < n_chrom; c++) { // For each chroms
			this.penalty[c] = model.computePenalty(population[c]);
		}
	}

	private void crossover() {
		int crossingSecStart ;
		int crossingSecEnd ;
		Integer[] parent = new Integer[n_exams];
		Integer[] child = new Integer[n_exams];
		int indWorstParent = 0; double worstValuePop = Double.MIN_VALUE;

		// Search the two worst fitness in my population
		for (int i = 0; i < this.n_chrom; i++) {
			if (penalty[i] > worstValuePop) {
				worstValuePop = penalty[i];
				indWorstParent = i;
			}
		}
		
		parent = population[rand.nextInt(n_chrom)].clone();

		// Calculate a random crossing section
		crossingSecStart = rand.nextInt(n_exams- this.minimum_cut);
		crossingSecEnd = (int) (rand.nextInt(n_exams - crossingSecStart ) +crossingSecStart);
		
		// copy crossing section two chromosome
		for (int i = crossingSecStart; i <= crossingSecEnd; i++) 
			child[i] = parent[i];
		
		// Order Crossover modified
		int k = 0; // contatore di ricorsioni fallite
		this.sortedExmsToSchedule = new ArrayList<Integer>(ExmsToSchedule);
		
		do {
			int numExamsNotAssignedYet = (this.n_exams - (crossingSecEnd + 1 - crossingSecStart));
			found = false;
			chromosome = new Integer[this.n_exams];
			nLoop = 0;

			doRecursive(child, 0, sortedExmsToSchedule.get(0), numExamsNotAssignedYet);
			Collections.swap(sortedExmsToSchedule, 0, k++);

			if (k > this.n_exams) { 
				return;
			}
			
		} while (!isFeasible(chromosome) || existYet(chromosome) || ts.isMinLocalYet(chromosome));

		child = ts.run(chromosome).clone();

		if (model.computePenalty(child) < penalty[indWorstParent])
			population[indWorstParent] = child.clone();

	}

	private boolean isFeasible(Integer[] chrom) {
		for (int e = 0; e < this.n_exams; e++) {
			if (chrom[e] == null || model.areConflictual(chrom[e], e, chrom))
				return false;
		}

		return true;
	}

	private void printPopulation() {
		int count = 1;
		System.out.println("Population: ");
		for (Integer[] chrom : population) {
			System.out.println("Chrom " + count + ": " + Arrays.toString(chrom));
			count++;
		}
	}

	private void printPenaltyPop() {

		System.out.println("Penalties: ");
		int i = 0;
		for (Double b : penalty) {
			System.out.println("Penalty" + (i++) + ": " + b);
		}
	}


}