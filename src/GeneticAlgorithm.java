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
		this.rand = new Random();
		this.ts = new TabuSearch(this.model);
		this.bestBenchmark = Double.MIN_VALUE;
	}

	public boolean existYet(Integer[] chrom) {

		for (Integer[] c : population)
			if (Arrays.equals(c, chrom))
				return true;

		return false;
	}

	public void fit_predict() {
		this.initial_population_RANDOM();
		// this.print_population();
		this.fitness();
		// this.print_banchmark();
		int i = 0;

		// crossover fino a scadenza dei 180/300 secondi
		while (true) {
			// System.out.print("\n"+ i++ +"th Iteration - Time:
			// "+(System.currentTimeMillis()-model.timeStart)/1000+" second\n");

			this.crossover();
			this.fitness();
			// this.print_population();
			// this.print_banchmark();

			if ((System.currentTimeMillis() - model.timeStart) > (180 * 1000)) { // termino il programma dopo 300s
				System.out.print("\nBest Bench: " + 1 / bestBenchmark
						+ /* "\nBest Solution: "+Arrays.toString(bestSolution)+ */"\n");

				this.print_population();
				this.print_banchmark();
				System.exit(1);
			}
		}

	}

	private boolean are_conflictual(int time_slot, int exam_id, Integer[] chrom) {
		for (int e = 0; e < this.n_exams; e++) {
			if (e != exam_id && chrom[e] != null) {
				if (chrom[e] == time_slot && this.nEe[e][exam_id] != 0) {
					return true;
				}
			}
		}
		return false;
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

		this.sortedExmToSchedule = exmStuds.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		this.sortedExmToSchedule.add(-1);

	}

	private void initial_population_RANDOM() {
		this.getSortedExmToScheduleByNumStudent();

		for (int c = 0; c < n_chrom; c++) {
			do {
				found = false;

				chromosome = new Integer[this.n_exams];
				nLoop = 0;

				doRecursive(chromosome, 0, sortedExmToSchedule.get(0), this.n_exams);

				Collections.swap(sortedExmToSchedule, 0, rand.nextInt(n_exams));
				// per generare soluzioni il più possibili diverse dopo la creazione
				// di una soluzine inverto l'ordine di due esami

			} while (!isFeasible(chromosome) || existYet(chromosome));

			population[c] = ts.run(chromosome);
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
				doRecursive(chrom, step + 1, sortedExmToSchedule.get(step + 1), numExamsNotAssignedYet);

				if (returnBack > 0) {
					returnBack--;
					return;

				}
			} else {
				for (int i : getBestPath(chrom, exam_id)) { // timeslot
					if (!found) {
						if (!are_conflictual(i, exam_id, chrom)) {
							chrom[exam_id] = i;
							doRecursive(chrom, step + 1, sortedExmToSchedule.get(step + 1), numExamsNotAssignedYet - 1);
							chrom[exam_id] = null;

							if (returnBack > 0) {
								returnBack--;
								return;
							} else
								nLoop++;

						}
					} else
						return;
				}

				if (!found)
					nLoop++; // every time i fail a complete for cycle

				if (nLoop > n_exams && !found) {
					returnBack = (int) (step * Math.random()); // number of time that i have to go back
					// System.out.print("Step "+ step + " returnBack "+returnBack+ " \n");
					nLoop = 0;
				}
			}

		} else {
			found = true;
			chromosome = chrom.clone();
			// System.out.print("Found ");
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
	public List<Integer> getBestPath(Integer[] chrom, int exam) {
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
	 * This method computes fitness for each chromosomes
	 */
	private void fitness() {
		double penalty;
		int distance;

		for (int c = 0; c < n_chrom; c++) { // For each chroms
			penalty = 0;
			distance = 0;

			for (int e1 = 0; e1 < n_exams; e1++) { // For each exams
				for (int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
					distance = Math.abs(population[c][e1] - population[c][e2]);
					if (distance <= 5) {
						penalty += (Math.pow(2, (5 - distance)) * this.nEe[e1][e2]);
					}
				}

			}
			// penalty = penalty / this.n_students;
			this.fitness[c] = this.n_students / penalty;
		}
	}

	/**
	 * This method computes fitness for a chromosome
	 * 
	 * @param chrom
	 * @return
	 */
	private double getChromFitness(Integer[] chrom) {
		double penalty = 0;
		int distance = 0;

		for (int e1 = 0; e1 < n_exams; e1++) { // For each exams
			for (int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
				distance = Math.abs(chrom[e1] - chrom[e2]);
				if (distance <= 5) {
					penalty += (Math.pow(2, 5 - distance) * this.nEe[e1][e2]);
				}
			}
		}

		penalty = this.n_students / penalty;

		return penalty;
	}

	private void crossover() {
		// System.out.print("Best Bench: "+1/bestBenchmark+/*"\nBest Solution:
		// "+Arrays.toString(bestSolution)+*/"\n");

		int indParent1 = 0, indParent2 = 0;
		double minValueP1 = Double.MAX_VALUE, minValueP2 = Double.MAX_VALUE;
		Integer[][] parents = new Integer[2][n_exams];

		// Search the two worst fitness in my population
		for (int i = 0; i < this.n_chrom; i++) {
			if (fitness[i] < minValueP1) {
				minValueP1 = fitness[i];
				indParent1 = i;
			}
		}

		for (int i = 0; i < this.n_chrom; i++) {
			if (fitness[i] < minValueP2 /* && indParent1!=i */ ) {
				minValueP2 = fitness[i];
				indParent2 = i;
			}
		}
		parents[0] = population[rand.nextInt(n_chrom)].clone();
		parents[1] = population[rand.nextInt(n_chrom)].clone();

		// Calculate a random crossing section
		int crossingSecStart = rand.nextInt(n_exams);
		int crossingSecEnd = (int) (rand.nextInt(n_exams - crossingSecStart) + crossingSecStart);
		Integer[][] childs = new Integer[2][n_exams];

		// System.out.print("Crossing Section: " + crossingSecStart + " - " +
		// crossingSecEnd + "\n");

		// copy crossing section two chromosome
		for (int i = crossingSecStart; i <= crossingSecEnd; i++) {
			childs[0][i] = parents[0][i];
			childs[1][i] = parents[1][i];
		}

		// Order Crossover modified
		for (int i = 0; i < 2; i++) {
			// getSortedExmToScheduleByNumStudent();
			int k = 0; // contatore di ricorsioni fallite

			do {
				int numExamsNotAssignedYet = (this.n_exams - (crossingSecEnd + 1 - crossingSecStart));
				found = false;
				chromosome = new Integer[this.n_exams];
				nLoop = 0;

				// va testato se è meglio la ricorsione del crossover o usare la stessa per
				// generare le soluzioni iniziali
				doRecursive(childs[i], 0, sortedExmToSchedule.get(0), numExamsNotAssignedYet);

				// se la mia ricorsione è fallita ed è uscita dal ciclo, provo a modificare
				// l'ordine di due esami
				Collections.swap(sortedExmToSchedule, 0, k++);

				if (k > this.n_exams) { // se ho fallito più del numero esami, abbandono sezione di taglio e ne provo
										// un'altra
					System.out
							.println("Time: " + (System.currentTimeMillis() - model.timeStart) / 1000 + " s - Failed");
					getSortedExmToScheduleByNumStudent();
					return;
				}
			} while (!isFeasible(chromosome) || existYet(chromosome) || ts.isMinLocalYet(chromosome));

			childs[i] = chromosome.clone();

		}

		childs[0] = ts.run(childs[0]).clone();
		childs[1] = ts.run(childs[1]).clone();

		if (getChromFitness(childs[0]) < getChromFitness(childs[1])) {

			if (getChromFitness(childs[0]) > fitness[indParent1])
				population[indParent1] = childs[0].clone();

			if (getChromFitness(childs[1]) > fitness[indParent2])
				population[indParent2] = childs[1].clone();

		} else {

			if (getChromFitness(childs[1]) > fitness[indParent1])
				population[indParent1] = childs[1].clone();

			if (getChromFitness(childs[0]) > fitness[indParent2])
				population[indParent2] = childs[0].clone();
		}

		this.fitness();

		// rapporto tra la fitness media e la fitness massima
		double ratio = (Arrays.stream(this.fitness).average().getAsDouble() // da teoria libro
				/ Arrays.stream(this.fitness).max().getAsDouble());

		// mi segno il miglior benchmark trovato
		if (Arrays.stream(this.fitness).max().getAsDouble() > bestBenchmark) {
			bestBenchmark = Arrays.stream(this.fitness).max().getAsDouble();
			System.out.println("Time: " + (System.currentTimeMillis() - model.timeStart) / 1000
					+ " s - New best benchmark : " + 1 / bestBenchmark + " - Ratio: " + ratio + "\n");
			for(Integer[] c : population)
				if(getChromFitness(c)>=bestBenchmark) {
					model.writeFdile(c);
					return;
				}
		}

		if (ratio > 0.99995) {// da teoria libro
			for (int c : getIndexBadChroms()) {

				do {
					found = false;
					chromosome = new Integer[this.n_exams];
					nLoop = 0;

					Collections.swap(sortedExmToSchedule, 0, rand.nextInt(n_exams));
					doRecursive(chromosome, 0, sortedExmToSchedule.get(0), this.n_exams);

				} while (!isFeasible(chromosome) || existYet(chromosome));

				population[c] = ts.run(chromosome);
			}

			System.out.println("Time: " + (System.currentTimeMillis() - model.timeStart) / 1000
					+ " s - NEW ENTRY POPULATION - Ratio:" + ratio + "\n");
		}

	}

	private List<Integer> getIndexBadChroms() {
		List<Integer> idx;
		HashMap<Integer, Double> fitness = new HashMap<Integer, Double>();

		for (int k = 0; k < this.n_chrom; k++)
			fitness.put(k, this.fitness[k]);

		idx = fitness.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
				.collect(Collectors.toList());

		for (int k = 0; k < this.n_chrom / 2; k++)
			idx.remove(0);

		return idx;
	}

	private boolean isFeasible(Integer[] chrom) {
		for (int e = 0; e < this.n_exams; e++) {
			if (chrom[e] == null || are_conflictual(chrom[e], e, chrom))
				return false;
		}

		return true;
	}

	private void print_population() {
		int count = 1;
		System.out.println("Population: ");
		for (Integer[] chrom : population) {
			System.out.println("Chrom " + count + ": " + Arrays.toString(chrom));
			count++;
		}
	}

	private void print_fitness() {

		System.out.println("Fitness: ");
		for (int i = 0; i < n_chrom; i++) {
			System.out.println("Fitness" + (i + 1) + ": " + 1 / fitness[i]);
		}
	}

	private void print_banchmark() {

		System.out.println("Banchmark: ");
		for (int i = 0; i < n_chrom; i++) {
			System.out.println("Banchmark" + (i + 1) + ": " + model.computePenalty(population[i]));
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