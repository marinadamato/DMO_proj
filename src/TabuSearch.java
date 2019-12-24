import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.TimeUnit;
//import javafx.util.Pair;
import java.util.stream.*;
//import java.util.Comparator.*;


public class TabuSearch {
    private ArrayList<TLelement> tabulist = new ArrayList<>();
    private HashMap<Integer, Integer> solution = new HashMap<>();
    private List<Integer> srtExms = new ArrayList<>();
    private Integer[] prov_sol;
    //private HashMap<Pair<Exam, Exam>, Integer> conflicts = new HashMap<>(); 
    private int timelimit;
    private Model model;
    private boolean find;
    private int returnBack;
    private int nLoop;

    public TabuSearch(int timelimit, Model model) {
        this.timelimit = timelimit;
        this.model = model;
    }

    public void addTL(TLelement tl){
	    if(tabulist.size()>=100) {
	     tabulist.remove(0);
	    }
	    tabulist.add(tl);
   }

    private Integer[] fromMaptoVect(HashMap<Integer, Integer> sol) {
    	Integer[] newsol = new Integer[sol.keySet().size()];
    	for(Map.Entry<Integer,Integer> entry : sol.entrySet()) {
    		newsol[entry.getKey()-1] = entry.getValue();
    	}
    	return newsol;
    }
    
    private HashMap<Integer, Integer> fromVecttoMap(Integer[] sol) {
    	 HashMap<Integer, Integer> newsol = new HashMap<>();
    	 for(int i=0; i < sol.length; i++) {
    		 newsol.put(i+1, sol[i]);
    		 
    	 }
    	
    	
    	return newsol;
    }
    //QUESTO METODO PER ORA E' INUTILE, GUARDARE buildNeEMatrix() IN Model.java
    /*public void generate_conflicts(ArrayList<Student> studs) {
        for(Student s : studs) {
            for(int i=0; i<s.getExams().size()-1; i++) {
                for(int j=i+1; j<s.getExams().size(); j++) {
                    Pair<Exam, Exam> tuple = new Pair<>(s.getExams().get(i), s.getExams().get(j));
                    if(conflicts.containsKey(tuple))
                        conflicts.replace(tuple, conflicts.get(tuple)+1);
                    else
                        conflicts.put(tuple, 1);
                }
            }
        }
        System.out.println(conflicts.entrySet());

    }*/
    
    public List<Integer> mapToList() {
    	//HashMap<Integer, Exam> ex = model.getExms();
    	
    	HashMap<Integer, Double> ex = new HashMap<Integer, Double>();
    	for(int i = 0; i<model.getExms().size();i++)
    		ex.put(i, (double) Arrays.stream(model.getLineFromMatrix(i)).filter( c -> c>0 ).count());//Arrays.stream(nEe[i]).average().getAsDouble());*/
		
    	
    	List<Integer> sortedExms = ex.entrySet().stream()
    			.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    			.map(Map.Entry::getKey)
    			.collect(Collectors.toList());
    	/*for(Integer i : sortedExms) {
    		System.out.println(i + ", " + exms.get(i).getNumber_st_enr());
    	}*/
    	sortedExms.add(-1);
    	return sortedExms;
    }
    
    private boolean are_conflictual(int time_slot, int exam_id, Integer[] sol) {		
		for(int e = 0; e < model.getExms().size(); e++) {
			if(e != exam_id && sol[e]!=null) {
				if(sol[e] == time_slot && model.areConflictual(e+1, exam_id+1)) {
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

	public Integer[] init_sol(){
		find = false;
		prov_sol = new Integer[model.getExms().size()];
		Integer[] sol = new Integer[model.getExms().size()];
		srtExms = mapToList();
		
		do {
			doRecursive(sol,0,srtExms.get(0), model.getExms().size());
		
		} while(!isFeasible(prov_sol));
		
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

    public HashMap<Integer, Integer> generateNeigh(HashMap<Integer, Integer> solution, double penalty, int n_timeslots){
        double bestP=0;
        double newP=0;
        int conflict;
        int tabumove;
        TLelement tl = new TLelement(0, 0);

        HashMap<Integer, Integer> bestSol = (HashMap<Integer, Integer>) solution.clone();
        for(Map.Entry<Integer,Integer> entry : solution.entrySet()){
            HashMap<Integer, Integer> newSol = (HashMap<Integer, Integer>) solution.clone();
            for(int i=1; i<n_timeslots+1; i++){
                if(i!=entry.getValue()){
                    conflict = model.checkVal(fromMaptoVect(newSol), i, entry.getKey()-1);
                    if(conflict == 0) {
                        newSol.replace(entry.getKey(), i);
                        newP = model.computePenalty(newSol);
                        tabumove = isTabu(entry.getKey(), i);
                        if (newP <= bestP || bestP==0) {
                            if (tabumove == 0 || (tabumove == 1 && newP < penalty)) {
                                tl.setE(entry.getKey());
                                tl.setTimeslot(i);
                                bestP = newP;
                                bestSol = (HashMap<Integer, Integer>) newSol.clone();
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Elemento da inserire nella tl:\nesame: " + tl.getE() + " timeslot: " + tl.getTimeslot());
        if(tl.getE()!=0 && tl.getTimeslot()!=0)
            this.addTL(tl);
        System.out.println();
        return bestSol;
    }
    
    private Integer[] toIntArray(List<Integer> list){
    	Integer[] ret = new Integer[list.size()];
	  for(int i = 0;i < ret.length;i++)
	    ret[i] = list.get(i);
	  return ret;
	}
    
    private void print_minLoc(HashMap<Integer, List<Integer>> minLoc) {
    	for(Map.Entry<Integer, List<Integer>> e : minLoc.entrySet()) {
    		System.out.println("Num iterazione: " + e.getKey() + " - Minimo: " + e.getValue());
    		System.out.println("Con penalit�: " + model.computePenalty(fromVecttoMap(toIntArray(e.getValue()))));
    	}
    }

    public void run() throws InterruptedException {
    	HashMap<Integer, List<Integer>> minLoc = new HashMap<Integer, List<Integer>>();
    	long diff;
        long endTime;
        long startTime = System.nanoTime();
        double penalty, newpenalty;
        int rip;
        HashMap<Integer, Integer> bestSol;
        
        for(int i=0; i<10; i++) {
        	 solution = fromVecttoMap(init_sol());
        	 penalty = model.computePenalty(solution);
        	 System.out.println("Penality:" + penalty);
        	 rip = 0;
	        do{
	            bestSol = generateNeigh(solution, penalty, model.getN_timeslots());
	            endTime = System.nanoTime();
	            diff=(endTime-startTime)/1000000;
	            newpenalty = model.computePenalty(bestSol);
	            if(penalty != newpenalty){
	            	if(newpenalty>=(penalty-0.1) && newpenalty<=(penalty+0.1)) {
	            		rip++;
	            		if(rip>200) {
	            			solution = bestSol;
	            			List<Integer> toPut = new ArrayList<Integer>(bestSol.values());
	    	            	if(!minLoc.containsValue(toPut)) {
	    	            		System.out.println("Min inserted!"); 
	    	            		minLoc.put(i, toPut);
	    	            		Thread.sleep(2000);
	    	            	}
	    	            	else
	    	            		System.out.println("Min already present!");
	    	                System.out.println();
	    	                break;
	            		}
	            			
	            	}
	            	else
	            		rip = 0;
	                solution = bestSol;
	                penalty = newpenalty;
	                System.out.println("Actual solution: " + solution.toString() + "\nWith penalty: " + penalty);
	            }
	            else{
	            	List<Integer> toPut = new ArrayList<Integer>(bestSol.values());
	            	if(!minLoc.containsValue(toPut)) {
	            		System.out.println("Min inserted!"); 
	            		minLoc.put(i, toPut);
	            		Thread.sleep(2000);
	            	}
	            	else
	            		System.out.println("Min already present!");
	                //System.out.println("Minimo locale: " + minLoc.toString());
	                System.out.println();
	                break;
	            }
	        }while(true);
        }
       print_minLoc(minLoc);
    }
}
