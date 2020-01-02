import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.*;


public class TabuSearch {
    private ArrayList<TLelement> tabulist;
    private List<Integer> srtExms = new ArrayList<>();
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
    }
    
    
    /**
	 * Sort Exams by the number of students enrolled it, to try to assign exams first with 
	 * the biggest average of student in conflict
	 * 
	 */
	/*private void getSortedExmToScheduleByNumStudent() {
		HashMap<Integer,Double> exmStuds = new HashMap<Integer, Double>();
		
		for(int i = 0; i<this.n_exams;i++)
			exmStuds.put(i, (double) Arrays.stream(model.getnEe()[i]).filter( c -> c>0 ).count());//Arrays.stream(nEe[i]).average().getAsDouble());
		
		this.srtExms = exmStuds.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .map(Map.Entry::getKey)
			    .collect(Collectors.toList());
		
		//this.srtExms.add(-1);
		
	}*/
    
    private boolean are_conflictual(int time_slot, int exam_id, Integer[] chrom) {		
    	for(int e = 0; e < this.n_exams; e++) {
			if(e != exam_id && chrom[e]!=null) {
				if(chrom[e] == time_slot && this.model.getnEe()[e][exam_id] > 0) {
					return true;
				}
			}
		}
		return false;
	}
    
    /**
	 * Find the best order path to schedule timeslot in base al numero totale di studenti che 
	 * sostengono esami già schedulati in un timeslot. L'idea è di cercare prima di schedulare, se possibile, 
	 * un esame nei timeslot più affollati in modo da riservare i restanti timeslot agli esami più conflittuali
	 * @param chrom
	 * @return list of sorted timeslot by the number of students enrolled in the exam assigned yet
	 */ /*
	public List<Integer> getBestPath(Integer[] chrom) {
		List<Integer> path;
		HashMap<Integer,Integer> numStudentTimeSlot = new HashMap<Integer, Integer>();
		
		for(int k=0; k<model.getN_timeslots();k++)
			numStudentTimeSlot.put(k, 0);
		
		for(int i=0; i<model.getExms().size(); i++) {
			if( chrom[i] != null ) {
				int numStud = (int) (numStudentTimeSlot.get(chrom[i]) + model.getExms().get(i).getNumber_st_enr());
				numStudentTimeSlot.replace(chrom[i], numStud);
			}
		} 
		
		path =  numStudentTimeSlot.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .map(Map.Entry::getKey)
			    .collect(Collectors.toList());
		
		// if i just started and my chromosome is empty, i generate a random path
		if(numStudentTimeSlot.values().stream().mapToInt(Integer::intValue).sum() == 0)
			Collections.shuffle(path);
		
		return path;
	}
	*/
	// uno dei dei principali problemi del tabusearch iniziale era che risultava lentissimo. Per ogni iterazione si andava
	// a calcolare la penalità dell'intera soluzione (n-esami*timeslot volte). Per ridurre il carico, ora mi 
	// vado a calcolare solo la penalità generata dal un singolo esame
	private double computePenaltyByExam(Integer[] chrom, int exam) {
		int dist;
        double penalty=0;
        
        for (int i=0; i<this.n_exams; i++){
        	if( exam != i) {
	        	dist = Math.abs(chrom[exam]-chrom[i]);
	        		if(dist<=5)
	        			penalty += (Math.pow(2, 5-dist)*model.getnEe()[exam][i]);
        	}
        }

        return penalty/model.getStuds().size();
	}

	// genero il vicinato
    public Integer[] generateNeigh(Integer[] chrom){
        double bestPenalty=0;
        double newPenalty;
        double actualPenalty;
        TLelement tl = new TLelement(-1, -1); // inizializzo elemento della tabù list;

        Integer[] bestSol = chrom.clone();
        double chromPenalty = model.computePenalty(chrom);
        Integer[] subSol = chrom;
        
        for(int e = 0; e<this.n_exams; e++ ) {//: getBadExams(chrom)) {// per ogni exam
        	Integer[] newSol = chrom.clone();
        	actualPenalty = computePenaltyByExam(chrom,e); // calcolo peso-penalità dell'esame e
            
            for(int i = 1; i<=this.n_timeslots;i++) {//: getBestPath(newSol)) { // per ogni time slot
                if(!are_conflictual(i, e, newSol)) { // controllo se posso inserire il timeslot in e
                    newSol[e]= i;
                    newPenalty = computePenaltyByExam(newSol,e); // calcolo il peso-penalità con il nuovo timeslot
                    
                    // se la differenza tra le due penalità (nuova e vecchia) è maggiore della precende soluzione
                    // migliore
                    if ((actualPenalty - newPenalty) > bestPenalty ) { 
                    	// controllo se è una mossa tabu o se anche tabu, mi genera una soluzione migliore di tutte
                    	// quelle trovate in precedenza
                    	double penalty = model.computePenalty(newSol);
                        if (!tabulist.contains(new TLelement(e, i)) 
                        		|| (tabulist.contains(new TLelement(e, i)) && penalty <= theBestPenalty)) { 
                            
                        	tl = new TLelement(e, i);
                            bestPenalty = (actualPenalty - newPenalty);
                            bestSol = newSol.clone();
                            
                            if(penalty < theBestPenalty) {
                            	theBestPenalty = penalty;
                            	 // System.out.print("\n"+theBestPenalty);
                            }
                            
                        }
                        
                        if(tabulist.contains(new TLelement(e, i)) && penalty < chromPenalty) {
                        	chromPenalty = penalty;
                        	subSol = newSol.clone();
                        }
                    }
                }
            }
        }

        //System.out.println("Elemento da inserire nella TL:\nesame: " + tl.getE() + " timeslot: " + tl.getTimeslot());
        
        // una volta visitato tutti gli esami, provati tutti i timeslot e trovato la mossa che mi restituisce una
        // variazione di penalità migliore, salvo la mossa
        if(tl.getE() > -1 )
        	tabulist.add(tl);
        else
        	bestSol = subSol;
        
        // se la tabulist ha una dimensione superiore al numero di esami per la media di timeslot per esami, 
        // elimino la mossa più vecchia. Se ho capito bene, la tabulist mi serve per andare ad esplorare soluzioni
        // inizialmente meno buone. Quindi devo obbligare il mio metodo ad posizionare tutti i possibili timeslot.
        // Io so che per ogni esame ho una media di n timeslot possibili, quindi ho all'incirca nesami*mediatime possibili
        // mosse. Siccome potrebbe essere troppo grande, nel calcolo della media, ho diminuito di 1 la media trovata.
        // Comunque anche questo va testato
        if(tabulist.size()>this.n_exams*this.avgTimeSlotNotConflictual)
        	tabulist.remove(0);
        
        //System.out.println();
        
        return bestSol;
    }
    
    
    private List<Integer> getBadExams(Integer[] chrome ) {
		List<Integer> idBadExams ;
		HashMap<Integer,Double> sortExam = new HashMap<Integer, Double>();
		int distance;
		int notConflictual;
		double penalty;
		
		for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
			penalty = 0;
			notConflictual =0;
			
			for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
				distance = Math.abs(chrome[e1] - chrome[e2]);
				if(distance <= 5) {
					penalty += (Math.pow(2, (5-distance)) * this.model.getnEe()[e1][e2]);
				}
			}
			
			for(int i =1; i<=this.n_timeslots; i++)
				if(!are_conflictual(i, e1, chrome))
					notConflictual++;

			sortExam.put(e1, (notConflictual*penalty));
		}
		
		idBadExams = sortExam.entrySet().stream()
	    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
	    .map(Map.Entry::getKey)
	    .collect(Collectors.toList());
		
		return idBadExams;
	}
    
    public boolean isMinLocalYet(Integer[] solution) {
    	for(Integer[] mL : minLoc)
    		if(Arrays.equals(mL, solution))
    			return true;
    		
    	return false;
    }
    
    
    // metodo che richiamo nel crossover
    public Integer[] run(Integer[] chrom) {
    	tabulist = new ArrayList<>();
        double currentPenalty;
        double newPenalty;
        double optPenalty;
        avgTimeSlotNotConflictual = getAvgTimeSlotNotConflictual(chrom); // valore che mi serve per definire la dimensione della tabulist
        
        Integer[] optSolution = chrom;
        Integer[] newSolution;
        Integer[] currentSolution;
        
    	currentSolution = chrom; // soluzione che gli passo dal crossover
    	currentPenalty = model.computePenalty(currentSolution); 
    	optPenalty = currentPenalty;
    	// System.out.println("Penality:" + penalty);

        do{
            newSolution = generateNeigh(currentSolution); // mi genero il vicinato
            newPenalty = model.computePenalty(newSolution);
            
            //	se la penalità tra la mia vecchia soluzione e quella nuova è migliorata di
            // almeno un millesimo della penalità della mia soluzione "più ottima", procedo ad esplorarla ancora
            // (valore da testare meglio, magari basta anche un centesimo)
            
            if((currentPenalty-newPenalty) > 0 ) {//!Arrays.equals(solution,bestSol) && !isMinLocalYet(bestSol)){
            	currentPenalty = newPenalty;
            	currentSolution = newSolution.clone();
                //System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + currentPenalty);
            	
        		
        		if(newPenalty<optPenalty) {
                	optSolution = newSolution.clone();
                	optPenalty = currentPenalty;
                } 
                
            } else if(!isMinLocalYet(newSolution)) { //se invece la mia nuova soluzione non è migliorata, la considero un minimo locale, se non l'ho già inserito nella lista dei minimi
        		// System.out.println("Min inserted!");
            	 currentSolution = newSolution.clone(); // non esco comunque dal tabusearch perchè voglio provare ancora una volta se mi porta ad una soluzione migliore
            	//currentPenalty = newPenalty;
        		
        		if(newPenalty<=optPenalty) {
        			minLoc.add( newSolution.clone());
                	optSolution = newSolution.clone();
                	optPenalty = newPenalty;
                } 
        		
        	} else {
        		
        		if(newPenalty<=optPenalty) 
                	optSolution = newSolution.clone();
        		
        		// se invece è già presente, esco dal tabusearch e restituisco la soluzione migliore che ho trovato
	            break;
        	
        		// System.out.println("Min already present!");
            
            	// System.out.println("Minimo locale: " + minLoc.toString());
                // System.out.println();
            }
        }while(true);
        // System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + penalty);
        

        return  optSolution;
    }
    
    
    // dalla soluzione passata dal crossover mi vado a calcolare la media dei timeslot ancora possibili 
    // per ogni esami, non so se ha senso (va testato) ma mi serve per definire dimensione tabulist
	private int getAvgTimeSlotNotConflictual(Integer[] chrom) {
		int notConflictual = 0;
		
		for(int e1 = 0; e1 < n_exams; e1++) { 
			for(int i =1; i<=this.n_timeslots; i++)
				if(!are_conflictual(i, e1, chrom))
					notConflictual++;

		}
		return notConflictual/this.n_exams;
	}
}
