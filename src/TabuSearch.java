import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabuSearch {
	private ArrayList<TLelement> tabulist;
	private Model model;
	private int n_exams;
	private int n_timeslots;
	private List<Integer[]> minLoc;
	private int avgTimeSlotNotConflictual;
	private double theBestPenalty = Double.MAX_VALUE;

	public TabuSearch(Model model) {
		this.model = model;
		this.n_exams = model.getExms().size();
		this.n_timeslots = model.getN_timeslots();
		this.minLoc = new ArrayList<Integer[]>();
		tabulist = new ArrayList<>();
	}

	private boolean are_conflictual(int time_slot, int exam_id, Integer[] chrom) {
		for (int e = 0; e < this.n_exams; e++) {
			if (e != exam_id && chrom[e] != null) {
				if (chrom[e] == time_slot && this.model.getnEe()[e][exam_id] > 0) {
					return true;
				}
			}
		}
		return false;
	}

	// uno dei dei principali problemi del tabusearch iniziale era che risultava
	// lentissimo. Per ogni iterazione si andava
	// a calcolare la penalità dell'intera soluzione (n-esami*timeslot volte). Per
	// ridurre il carico, ora mi
	// vado a calcolare solo la penalità generata dal un singolo esame
	private double computePenaltyByExam(Integer[] chrom, int exam) {
		int dist;
		double penalty = 0;

		for (int i = 0; i < this.n_exams; i++) {
			if (exam != i) {
				dist = Math.abs(chrom[exam] - chrom[i]);
				if (dist <= 5)
					penalty += (Math.pow(2, 5 - dist) * model.getnEe()[exam][i]);
			}
		}

		return penalty / model.getStuds().size();
	}

	// genero il vicinato
	public Integer[] generateNeigh(Integer[] chrom) {
		double bestPenalty = 0;
		double newPenalty;
		double actualPenalty;
		TLelement tl = new TLelement(-1, -1); // inizializzo elemento della tabù list;

		Integer[] bestSol = chrom.clone();
		double chromPenalty = model.computePenalty(chrom);
		Integer[] subSol = chrom;

		for (int e = 0; e < this.n_exams; e++) {// : getBadExams(chrom)) {// per ogni exam
			Integer[] newSol = chrom.clone();
			actualPenalty = computePenaltyByExam(chrom, e); // calcolo peso-penalità dell'esame e

			for (int i = 1; i <= this.n_timeslots; i++) {// : getBestPath(newSol)) { // per ogni time slot
				if (!are_conflictual(i, e, newSol)) { // controllo se posso inserire il timeslot in e
					newSol[e] = i;
					newPenalty = computePenaltyByExam(newSol, e); // calcolo il peso-penalità con il nuovo timeslot

					// se la differenza tra le due penalità (nuova e vecchia) è maggiore della
					// precende soluzione
					// migliore
					if ((actualPenalty - newPenalty) > bestPenalty && !isMinLocalYet(newSol)) {
						// controllo se è una mossa tabu o se anche tabu, mi genera una soluzione
						// migliore di tutte
						// quelle trovate in precedenza
						double penalty = model.computePenalty(newSol);
						if (!tabulist.contains(new TLelement(e, i))
								|| (tabulist.contains(new TLelement(e, i)) && penalty <= theBestPenalty)) {

							tl = new TLelement(e, i);
							bestPenalty = (actualPenalty - newPenalty);
							bestSol = newSol.clone();

							if (penalty < theBestPenalty) {
								theBestPenalty = penalty;
								// System.out.print("\n"+theBestPenalty);
							}

						}

						if (tabulist.contains(new TLelement(e, i)) && penalty < chromPenalty) {
							chromPenalty = penalty;
							subSol = newSol.clone();
						}
					}
				}
			}
		}

		// una volta visitato tutti gli esami, provati tutti i timeslot e trovato la
		// mossa che mi restituisce una
		// variazione di penalità migliore, salvo la mossa
		if (tl.getE() > -1)
			tabulist.add(tl);
		else
			bestSol = subSol;

		// se la tabulist ha una dimensione superiore al numero di esami per la media di
		// timeslot per esami,
		// elimino la mossa più vecchia. Se ho capito bene, la tabulist mi serve per
		// andare ad esplorare soluzioni
		// inizialmente meno buone. Quindi devo obbligare il mio metodo ad posizionare
		// tutti i possibili timeslot.
		// Io so che per ogni esame ho una media di n timeslot possibili, quindi ho
		// all'incirca nesami*mediatime possibili
		// mosse. Siccome potrebbe essere troppo grande, nel calcolo della media, ho
		// diminuito di 1 la media trovata.
		// Comunque anche questo va testato
		if (tabulist.size() > this.n_exams * this.avgTimeSlotNotConflictual)
			tabulist.remove(0);

		return bestSol;
	}

	public boolean isMinLocalYet(Integer[] solution) {
		for (Integer[] mL : minLoc)
			if (Arrays.equals(mL, solution))
				return true;

		return false;
	}

	// metodo che richiamo nel crossover
	public Integer[] run(Integer[] chrom) {
		// tabulist = new ArrayList<>();
		double currentPenalty;
		double newPenalty;
		double optPenalty;
		avgTimeSlotNotConflictual = getAvgTimeSlotNotConflictual(chrom)-1; // valore che mi serve per definire la
																			// dimensione della tabulist

		Integer[] optSolution = chrom;
		Integer[] newSolution;
		Integer[] currentSolution;

		currentSolution = chrom; // soluzione che gli passo dal crossover
		currentPenalty = model.computePenalty(currentSolution);
		optPenalty = currentPenalty;

		do {
			newSolution = generateNeigh(currentSolution); // mi genero il vicinato
			newPenalty = model.computePenalty(newSolution);

			// se la penalità tra la mia vecchia soluzione e quella nuova è migliorata di
			// almeno un millesimo della penalità della mia soluzione "più ottima", procedo
			// ad esplorarla ancora
			// (valore da testare meglio, magari basta anche un centesimo)

			if ((currentPenalty - newPenalty) > 0) {// !Arrays.equals(solution,bestSol) && !isMinLocalYet(bestSol)){
				currentPenalty = newPenalty;
				currentSolution = newSolution.clone();
				// System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith
				// penalty: " + currentPenalty);

				if (newPenalty < optPenalty) {
					optSolution = newSolution.clone();
					optPenalty = currentPenalty;
				}

			} else {
				if (!isMinLocalYet(newSolution))
					minLoc.add(newSolution.clone());

				if (newPenalty < optPenalty)
					optSolution = newSolution.clone();

				// esco dal tabusearch e restituisco la soluzione
				// migliore che ho trovato
				break;
			}
		} while (true);
		// System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith
		// penalty: " + penalty);

		return optSolution;
	}

	// dalla soluzione passata dal crossover mi vado a calcolare la media dei
	// timeslot ancora possibili
	// per ogni esami, non so se ha senso (va testato) ma mi serve per definire
	// dimensione tabulist
	private int getAvgTimeSlotNotConflictual(Integer[] chrom) {
		int notConflictual = 0;

		for (int e1 = 0; e1 < n_exams; e1++) {
			for (int i = 1; i <= this.n_timeslots; i++)
				if (!are_conflictual(i, e1, chrom))
					notConflictual++;

		}
		return notConflictual / this.n_exams;
	}
}