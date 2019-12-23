import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.*;

import javax.swing.event.ListSelectionEvent;


public class TabuSearch {
    private ArrayList<TLelement> tabulist = new ArrayList<>();
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

    public TabuSearch(int timelimit, Model model) {
        this.timelimit = timelimit;
        this.model = model;
        this.n_exams = model.getExms().size();
        this.n_timeslots = model.getN_timeslots();
    }

    public void addTL(TLelement tl){
        tabulist.add(tl);
    }

    private Integer[] fromMaptoVect(HashMap<Integer, Integer> sol) {
    	Integer[] newsol = new Integer[sol.keySet().size()];
    	for(Map.Entry<Integer,Integer> entry : sol.entrySet()) {
    		newsol[entry.getKey()] = entry.getValue();
    	}
    	return newsol;
    }
    
    private HashMap<Integer, Integer> fromVecttoMap(Integer[] sol) {
    	 HashMap<Integer, Integer> newsol = new HashMap<>();
    	 for(int i=0; i < sol.length; i++) {
    		 newsol.put(i, sol[i]);
    		 
    	 }
    	
    	
    	return newsol;
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
		
		this.srtExms.add(-1);
		
	}
    
    private boolean are_conflictual(int time_slot, int exam_id, Integer[] sol) {		
		for(int e = 0; e < model.getExms().size(); e++) {
			if(e != exam_id && sol[e]!=null) {
				if(sol[e] == time_slot && model.areConflictual(e, exam_id)) {
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

	private boolean isFeasible(Integer[] sol) {
		 for(int e = 0; e<model.getExms().size(); e++) {
			  if(sol[e] == null || are_conflictual(sol[e], e, sol))
				  return false;
		  }
		 
		return true;
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

    public int isTabu(int e, int t){

        int flag=0;
        for(TLelement el:tabulist){
            if(el.getE() == e && el.getTimeslot() == t){
                flag=1;
                return 1;
            }
        }
        if(flag==0)
            return 0;
        return 0;
    }

    public Integer[] generateNeigh(Integer[] solution, double penalty){
        double bestP=Integer.MAX_VALUE;
        double newP;
        int tabumove;
        TLelement tl = new TLelement(-1, -1);

        Integer[] bestSol = solution.clone();
        
        for(int e = 0; e<this.n_exams; e++){
        	Integer[] newSol = solution.clone();
            
            for(int i=0; i<n_timeslots; i++){
                if(i!=e){
                    
                    if(model.checkVal(newSol, i, e)) {
                        newSol[e]= i;
                        newP = model.computePenalty(newSol);
                        tabumove = isTabu(e, i);
                        
                        if (newP <= bestP) {
                            if (tabumove == 0 || (tabumove == 1 && newP < penalty)) {
                                tl.setE(e);
                                tl.setTimeslot(i);
                                
                                bestP = newP;
                                bestSol = newSol;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Elemento da inserire nella TL:\nesame: " + tl.getE() + " timeslot: " + tl.getTimeslot());
        if(tl.getE()!=0 && tl.getTimeslot()!=0)
            this.addTL(tl);
        System.out.println();
        
        return bestSol;
    }

    public Integer[] run(Integer[] chrom) {
    	HashMap<Integer, Integer[]> minLoc = new HashMap<Integer, Integer[]>();
    	long diff;
        long endTime;
        long startTime = System.nanoTime();
        double penalty;
        
        Integer[] toPut;
        Integer[] bestSol;
        int i = 0;
        //for(int i=0; i<10; i++) {
        	 solution = chrom;
        	 penalty = model.computePenalty(solution);
        	 System.out.println("Penality:" + penalty);

	        do{
	            bestSol = generateNeigh(solution, penalty);
	            endTime = System.nanoTime();
	            diff=(endTime-startTime)/1000000;
	            if(!solution.equals(bestSol)){
	                solution = bestSol;
	                penalty = model.computePenalty(bestSol);
	                System.out.println("Actual solution: " + Arrays.toString(solution) + "\nWith penalty: " + penalty);
	            }
	            else{
	            	if(!minLoc.containsValue(bestSol)) {
	            		System.out.println("Min inserted!");
	            		minLoc.put(i, bestSol.clone());
	            	}
	            	else
	            		System.out.println("Min already present!");
	                System.out.println("Minimo locale: " + minLoc.toString());
	                System.out.println();
	                break;
	            }
	        }while(true);
        //}
	        return  bestSol.clone();
    }
}
