import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabuSearch {
	private ArrayList<TLelement> tabulist;
	private Model model;
	private int n_exams;
	private int n_timeslots;
	private int dimTabuList;
	private double optPenaltyLocal = Double.MAX_VALUE;
	private int[][] conflictMatrix;

	public TabuSearch(Model model) {
		this.model = model;
		this.n_exams = model.getExms().size();
		this.n_timeslots = model.getN_timeslots();
		this.conflictMatrix= model.getConflictMatrix();
	}
	
	// metodo che richiamo nel crossover
	public Integer[] run(Integer[] chrom) {
		tabulist = new ArrayList<>();
		double currentPenalty;
		double newPenalty;
		dimTabuList = getDimTabuList(chrom); // valore che mi serve per definire la
																			// dimensione della tabulist
		Integer[] optSolution = chrom;
		Integer[] newSolution;
		Integer[] currentSolution;

		currentSolution = chrom; // soluzione che gli passo dal crossover
		currentPenalty = model.computePenalty(currentSolution);
		optPenaltyLocal = currentPenalty;

		do {
			newSolution = generateNeigh(currentSolution); // mi genero il vicinato
			newPenalty = model.computePenalty(newSolution);

			// se la penalità tra la mia vecchia soluzione e quella nuova è migliorata di
			// almeno un millesimo della penalità della mia soluzione "più ottima", procedo
			// ad esplorarla ancora
			// (valore da testare meglio, magari basta anche un centesimo)

			if ((currentPenalty - newPenalty) > 0.0005) {
				currentPenalty = newPenalty;
				currentSolution = newSolution.clone();

			} else {
				
				if (!isMinLocalYet(newSolution)) {
					model.addMinLoc(newSolution.clone());
				} else break;
			
				if (newPenalty < optPenaltyLocal) {
					optSolution = newSolution.clone();
					optPenaltyLocal = newPenalty;
					
					if(model.isNewOpt(optSolution)) {
						Thread current = Thread.currentThread();
						System.out.println("Found by " + current.getName());
					} 
					
				} else break;
				
				currentSolution = swapTimeslot(newSolution);
				currentPenalty = model.computePenalty(currentSolution);
				
			}
		} while (true);

		return optSolution;
	}

	// genero il vicinato
	public Integer[] generateNeigh(Integer[] chrom) {
		double bestPenalty = 0;
		double newPenalty;
		double actualPenalty;
		TLelement tl = new TLelement(-1, -1); // inizializzo elemento della tabù list;
		Integer[] bestSol = chrom.clone();
		
		for (int e = 0; e < this.n_exams; e++) { // per ogni exam
			Integer[] newSol = chrom.clone();
			actualPenalty = model.computePenaltyByExam(chrom, e); // calcolo peso-penalità dell'esame e

			for (int i = 1; i <= this.n_timeslots; i++) { // per ogni time slot
				if (!model.areConflictual(i, e, newSol) && i != chrom[e]) { // controllo se posso inserire il timeslot in e
					newSol[e] = i;
					newPenalty = model.computePenaltyByExam(newSol, e); // calcolo il peso-penalità con il nuovo timeslot

					// se la differenza tra le due penalità (nuova e vecchia) è maggiore della
					// precende soluzione
					// migliore
					if ((actualPenalty - newPenalty) > bestPenalty ) {
						// controllo se è una mossa tabu o se anche tabu, mi genera una soluzione
						// migliore di tutte
						// quelle trovate in precedenza
						double penalty = model.computePenalty(newSol);
						if (!tabulist.contains(new TLelement(e, i))
								|| (tabulist.contains(new TLelement(e, i)) && penalty < optPenaltyLocal)) {

							tl = new TLelement(e, i);
							bestPenalty = (actualPenalty - newPenalty);
							bestSol = newSol.clone();


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
		if (tabulist.size() > this.dimTabuList)
			tabulist.remove(0);

		return bestSol;
	}

	public boolean isMinLocalYet(Integer[] solution) {
		List<Integer[]> minLoc = new ArrayList<>(model.getMinLoc());
		for (Integer[] ml: minLoc)
			if (Arrays.equals(ml, solution))
				return true;

		return false;
	}
	
	private Integer[] swapTimeslot(Integer[] sol) {
		Integer[] test;
		Integer[] bestSol = sol.clone();
		double bestPenalty = Double.MAX_VALUE;
		
		for(int timeS1 =1; timeS1<=this.n_timeslots; timeS1++) {
			List<String> exm1 = new ArrayList<>();
			
			for(int i =0; i<this.n_exams; i++) {
				if(sol[i]==timeS1)
					exm1.add(String.valueOf(i));
			}
			
			for(int timeS2 = 1; timeS2<=this.n_timeslots; timeS2++) {
	
				test = sol.clone();
				if(timeS2 != timeS1) {
					List<String> exm2 = new ArrayList<>();
					
					for(int i =0; i<this.n_exams; i++) {
						if(sol[i]==timeS2)
							exm2.add(String.valueOf(i));
					}
					
					List<String> exmSwap1 = new ArrayList<>();
					List<String> exmSwap2 = new ArrayList<>();
					
					for(String e : new ArrayList<>(exm1))
						for(String e2 : exm2)
							if(this.conflictMatrix[Integer.valueOf(e)][Integer.valueOf(e2)] > 0) {
								exmSwap1.add(e);
								break;
							}
					
					for(String e : new ArrayList<>(exm2))
						for(String e1 : exm1)
							if(this.conflictMatrix[Integer.valueOf(e)][Integer.valueOf(e1)] > 0 ) {
								exmSwap2.add(e);
								break;
							}
					
					for(String e : exmSwap1)
						test[Integer.valueOf(e)] = timeS2;
					
					for(String e : exmSwap2)
						test[Integer.valueOf(e)] = timeS1;	
					
					if(model.computePenalty(test)<bestPenalty) {
						bestSol = test.clone();
						bestPenalty = model.computePenalty(test);
					}
				}
			}
		}
		
		return bestSol;			
		
	}

	// dalla soluzione passata dal crossover mi vado a calcolare la media dei
	// timeslot ancora possibili
	// per ogni esami, non so se ha senso (va testato) ma mi serve per definire
	// dimensione tabulist
	private int getDimTabuList(Integer[] chrom) {
		int notConflictual = 0;

		for (int e1 = 0; e1 < n_exams; e1++) {
			for (int i = 1; i <= this.n_timeslots; i++)
				if (!model.areConflictual(i, e1, chrom))
					notConflictual++;

		}
		return (notConflictual - this.n_exams);
	}
	

}