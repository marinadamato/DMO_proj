import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;


public class TabuSearch {
    private ArrayList<TLelement> tabulist = new ArrayList<>();
    private HashMap<Integer, ArrayList<Exam>> solution = new HashMap<>();
    private HashMap<Pair<Exam, Exam>, Integer> conflicts = new HashMap<>();
    private int timelimit;

    public TabuSearch(int timelimit) {
        this.timelimit = timelimit;
    }

    public void generate_conflicts(ArrayList<Student> studs) {
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
    }

    public void run() {

    }
}
