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
    private Integer[] solution;
    private List<Integer> srtExms = new ArrayList<>();
    private Integer[] prov_sol;
    private int timelimit;
    private Model model;
    private boolean find;
    private int returnBack;
    private int nLoop;
    private int n_exams;
	private int n_timeslots;
	private HashMap<Integer, Integer[]> minLoc;

    public TabuSearch(int timelimit, Model model) {
        this.timelimit = timelimit;
        this.model = model;
        this.n_exams = model.getExms().size();
        this.n_timeslots = model.getN_timeslots();
        this.minLoc = new HashMap<Integer, Integer[]>();
    }
    
    /**
	 * Sort Exams by the number of students enrolled it, to try to assign exams first with 
	 * the biggest average of student in conflict
	 * 
	 */
	private void getSortedExmToScheduleByNumStudent() {
		HashMap<Integer,Double> exmStuds = new HashMap<Integer, Double>();
		
		for(int i = 0; i<this.n_exams;i++)
			exmStuds.put(i, (double) Arrays.stream(model.getnEe()[i]).filter( c -> c>0 ).count());//Arrays.stream(nEe[i]).average().getAsDouble());
		
		this.srtExms = exmStuds.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .map(Map.Entry::getKey)
			    .collect(Collectors.toList());
		
		//this.srtExms.add(-1);
		
	}
    
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
	 */
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
    
	private void doRecursive(Integer[] sol,int step, int exam_id, int numExamsNotAssignedYet) {
		if(numExamsNotAssignedYet > 0) {
			if(sol[exam_id]!=null) 
				doRecursive(sol, step+1, srtExms.get(step+1), numExamsNotAssignedYet);
			else {
				for(Integer i : getBestPath(sol)) {
					if(!find) {
						if(!are_conflictual(i, exam_id, sol)) {
							sol[exam_id] = i;
							doRecursive(sol, step+1, srtExms.get(step+1), numExamsNotAssignedYet-1);
							sol[exam_id] = null;
							
							if(returnBack>0 ) {
								returnBack--; 
								return;
							} else 
								nLoop++; // every time i fail to assign i time slot to exam_id
			
						}
					} else return;
				}
				
				if(!find)
					nLoop++; // every time i fail a complete for cycle
				
				if(nLoop > model.getExms().size() && !find)  {
					returnBack = (int) (step*Math.random()); // number of time that i have to go back
					//System.out.print("Step "+ step + " returnBack "+returnBack+ " \n");
					nLoop = 0;
				} 
			}
			
		} else {
			find = true;
			prov_sol = sol.clone();
			System.out.print("Found ");
		}
	}


	public Integer[] init_sol(Integer[] chrom){
		find = false;
		/*prov_sol = new Integer[model.getExms().size()];
		Integer[] sol = new Integer[model.getExms().size()];
		getSortedExmToScheduleByNumStudent();
		
		do {
			doRecursive(sol,0,srtExms.get(0), model.getExms().size());
		
		} while(!isFeasible(prov_sol));*/
		
		prov_sol = chrom;
		
		System.out.println("Initial solution: " + Arrays.toString(prov_sol));
		
		return prov_sol;
	}

    public boolean isTabu(int e, int t){

        for(TLelement el :tabulist)
            if(el.equals(new TLelement(e, t)))
                return true;
        
        return false;
    }

    public Integer[] generateNeigh(Integer[] chrom){
        double bestPenalty=Integer.MAX_VALUE;
        double theBestPenalty = model.computePenalty(chrom);;
        double newPenalty;
        TLelement tl = new TLelement(-1, -1);

        Integer[] bestSol = chrom.clone();
        getSortedExmToScheduleByNumStudent();
        
        for(int e : getBadExams(chrom)) {// exam
        	Integer[] newSol = chrom.clone();
            
            for(int i = 0; i<this.n_timeslots;i++) {//: getBestPath(newSol)) { // time slot
                if(i != chrom[e]){
                    if(!are_conflictual(i, e, newSol)) {
                        newSol[e]= i;
                        newPenalty = model.computePenalty(newSol);
                        
                        if (newPenalty < bestPenalty ) {
                            if (!isTabu(e, i) || (isTabu(e, i) 
                            		&& newPenalty < theBestPenalty)) {
                                
                            	tl = new TLelement(e, i);
                                bestPenalty = newPenalty;
                                bestSol = newSol.clone();
                                
                                //if(Double.compare(bestPenalty, theBestPenalty)<0)
                                //	theBestPenalty = bestPenalty;
                                
                            }
                        }
                    }
                }
            }
        }

         //System.out.println("Elemento da inserire nella TL:\nesame: " + tl.getE() + " timeslot: " + tl.getTimeslot());
        
        if(tl.getE() > -1 )
        	tabulist.add(tl);
        
        if(tabulist.size()>this.n_timeslots)
        	tabulist.remove(0);
        
         //System.out.println();
        
        return bestSol;
    }
    
    private List<Integer> getBadExams(Integer[] chrome ) {
		List<Integer> idBadExams ;
		HashMap<Integer,Double> sortExam = new HashMap<Integer, Double>();
		int distance;
		double penalty;
		
		for(int e1 = 0; e1 < n_exams; e1++) { // For each exams
			penalty = 0;
			
			for(int e2 = e1 + 1; e2 < n_exams; e2++) { // For each other exams
				distance = Math.abs(chrome[e1] - chrome[e2]);
				if(distance <= 5) {
					penalty += (Math.pow(2, (5-distance)) * this.model.getnEe()[e1][e2]);
				}
			}

			sortExam.put(e1, penalty);
		}
		
		idBadExams = sortExam.entrySet().stream()
	    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
	    .map(Map.Entry::getKey)
	    .collect(Collectors.toList());
		
		return idBadExams;
	}
    
    private boolean isMinLocalYet(Integer[] solution) {
    	for(Integer[] mL : minLoc.values())
    		if(Arrays.equals(mL, solution))
    			return true;
    		
    	return false;
    }

    public Integer[] run(Integer[] chrom) {
    	// minLoc = new HashMap<Integer, Integer[]>();
    	tabulist = new ArrayList<>();
        double currentPenalty;
        double newPenalty = Integer.MAX_VALUE;
        
        Integer[] bestSol;
        
        	 solution = chrom;
        	 currentPenalty = model.computePenalty(solution);
        	 // System.out.println("Penality:" + penalty);

	        do{
	            bestSol = generateNeigh(solution.clone());
	            newPenalty = model.computePenalty(bestSol);
	            if(!Arrays.equals(solution,bestSol) || !isMinLocalYet(bestSol) ){
	            	if(newPenalty < currentPenalty) {
		            	minLoc.put(minLoc.size(), bestSol.clone());
	            		solution = bestSol.clone();
	            		currentPenalty = newPenalty;
	            	}
	                 //System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + penalty);
	            
	            } else {
	            	//if(!isMinLocalYet(bestSol)) {
	            		//System.out.println("Min inserted!");
	            	//	minLoc.put(minLoc.size(), bestSol.clone());
	            	//}  // else  
	            		// System.out.println("Min already present!");
	                
		            	// System.out.println("Minimo locale: " + minLoc.toString());
		                // System.out.println();
		                break;
	            	}
	        }while(true);//iteration <this.n_exams); 
	        // System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + penalty);
            

	        return  solution.clone();
    }
}
