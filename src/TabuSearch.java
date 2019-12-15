import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;


public class TabuSearch {
    private ArrayList<TLelement> tabulist = new ArrayList<>();
    private HashMap<Integer, Integer> solution = new HashMap<>();
    private HashMap<Pair<Exam, Exam>, Integer> conflicts = new HashMap<>();
    private int timelimit;
    private Model model;

    public TabuSearch(int timelimit, Model model) {
        this.timelimit = timelimit;
        this.model = model;
    }

    public void addTL(TLelement tl){
        tabulist.add(tl);
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
                    conflict = model.checkVal(newSol, i, entry.getKey());
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

        System.out.println(tl.getE());
        System.out.println(tl.getTimeslot());
        if(tl.getE()!=0 && tl.getTimeslot()!=0)
            this.addTL(tl);
        return bestSol;
    }

    public void run() {
        double penalty;
        solution = model.initialSol();
        penalty = model.computePenalty(solution);
        System.out.println(penalty);
        HashMap<Integer, Integer> bestSol;
        long diff;
        long endTime;
        long startTime = System.nanoTime();

        do{
            bestSol = generateNeigh(solution, penalty, model.getN_timeslots());
            endTime = System.nanoTime();
            diff=(endTime-startTime)/1000000;
            if(!solution.equals(bestSol)){
                solution = bestSol;
                penalty = model.computePenalty(bestSol);
                System.out.println(penalty);
                System.out.println(solution);
            }
            else{
                System.out.println("Minimo locale");
                break;
            }
        }while(diff<300000);

    }
}
