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
    private Model model;
    private int n_exams;
	private int n_timeslots;
	private List<Integer[]> minLoc;
	private int avgTimeSlotNotConflictual;

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
	
	private double computePenaltyByExam(Integer[] chrom, int exam) {
		int dist;
        double penalty=0;
        
        for (int i=0; i<this.n_exams; i++){
        	dist = Math.abs(chrom[exam]-chrom[i]);
        		if(dist<=5)
        			penalty += (Math.pow(2, 5-dist)*model.getnEe()[exam][i]);
            
        };

        return penalty/model.getStuds().size();
	}

    public Integer[] generateNeigh(Integer[] chrom){
        double bestPenalty=Integer.MIN_VALUE;
        double theBestPenalty = model.computePenalty(chrom);
        double newPenalty;
        double actualPenalty;
        TLelement tl = new TLelement(-1, -1);

        Integer[] bestSol = chrom.clone();
        
        for(int e =0; e<this.n_exams; e++) { //: getBadExams(chrom)) {// exam
        	Integer[] newSol = chrom.clone();
        	actualPenalty = computePenaltyByExam(chrom,e);
            
            for(int i = 1; i<=this.n_timeslots;i++) {//: getBestPath(newSol)) { // time slot
                if(!are_conflictual(i, e, newSol)) {
                    newSol[e]= i;
                    newPenalty = computePenaltyByExam(newSol,e);
                    
                    if ((actualPenalty - newPenalty) > bestPenalty ) {
                        if (!tabulist.contains(new TLelement(e, i)) || (tabulist.contains(new TLelement(e, i)) 
                        		&& (actualPenalty - newPenalty) > theBestPenalty)) {
                            
                        	tl = new TLelement(e, i);
                            bestPenalty = (actualPenalty - newPenalty);
                            bestSol = newSol.clone();
                            
                            if(Double.compare(bestPenalty, theBestPenalty)>0)
                            	theBestPenalty = bestPenalty;
                            
                        }
                    }
                }
            }
        }

        //System.out.println("Elemento da inserire nella TL:\nesame: " + tl.getE() + " timeslot: " + tl.getTimeslot());
        
        if(tl.getE() > -1 )
        	tabulist.add(tl);
        
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

			sortExam.put(e1, (notConflictual/penalty));
		}
		
		idBadExams = sortExam.entrySet().stream()
	    .sorted(Map.Entry.comparingByValue(/*Comparator.reverseOrder()*/))
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

    public Integer[] run(Integer[] chrom) {
    	// minLoc = new HashMap<Integer, Integer[]>();
    	tabulist = new ArrayList<>();
        double currentPenalty;
        double newPenalty = Integer.MAX_VALUE;
        double optPenalty = newPenalty;
        avgTimeSlotNotConflictual = getAvgTimeSlotNotConflictual(chrom)-1;
        
        Integer[] optSolution = chrom;
        Integer[] bestSol;
        
        	 solution = chrom;
        	 currentPenalty = model.computePenalty(solution);
        	 // System.out.println("Penality:" + penalty);

	        do{
	            bestSol = generateNeigh(solution);
	            newPenalty = model.computePenalty(bestSol);
	            
	            if((currentPenalty-newPenalty) > (optPenalty/1000) ) {//!Arrays.equals(solution,bestSol) && !isMinLocalYet(bestSol)){
	            	currentPenalty = newPenalty;
	            	solution = bestSol.clone();
	                //System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + currentPenalty);
	            	
	                //minLoc.put(minLoc.size(), bestSol.clone());
            		
            		if(newPenalty<optPenalty) {
	                	optSolution = bestSol.clone();
	                	optPenalty = currentPenalty;
	                } 
	                
	            } else {
	            	if(!isMinLocalYet(bestSol)) {
	            		// System.out.println("Min inserted!");
	            		minLoc.add( bestSol.clone());
		            	solution = bestSol.clone();
		            	currentPenalty = newPenalty;
	            		
	            		if(newPenalty<optPenalty) {
		                	optSolution = bestSol.clone();
		                	optPenalty = newPenalty;
		                }
	            	} else {
	            		
	            		if(newPenalty<optPenalty) {
		                	optSolution = bestSol.clone();
		                	optPenalty = newPenalty;
		                }
	            		
	            		break;
	            	}
	            		// System.out.println("Min already present!");
	                
		            	// System.out.println("Minimo locale: " + minLoc.toString());
		                // System.out.println();
	            }
	        }while(true);
	        // System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + penalty);
            

	        return  optSolution;
    }

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
