import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Model {

    private int n_timeslots;
    private int[][] nEe;
    private HashMap<Integer, Exam> exms;
    private HashSet<String> studs;



    public Model() {

        this.n_timeslots = 0;
        exms = new HashMap<Integer, Exam>();
        studs = new HashSet<String>();
    }

    public int getN_timeslots(){
        return this.n_timeslots;
    }

    public int[][] getnEe(){
        return this.nEe;
    }

    public HashMap<Integer, Exam> getExms(){
        return this.exms;
    }

    public HashSet<String> getStuds(){
        return this.studs;
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public boolean loadSlo(String file) {
        String st;
        try {
            File file_time = new File(file);
            BufferedReader br_t = new BufferedReader(new FileReader(file_time));
            while ((st = br_t.readLine()) != null)
                n_timeslots = Integer.parseInt(st);
            br_t.close();

            return true;

        } catch (IOException e) {
            System.err.println("Errore nella lettura del file");
            return false;
        }
    }

    public boolean loadExm(String file) {
        String st;
        try {
            File file_exams = new File(file);
            BufferedReader br_exm = new BufferedReader(new FileReader(file_exams));
            while ((st = br_exm.readLine()) != null) {
            	if(st.length()>0){
	                String[] parts = st.split(" ");
	                if(parts.length != 2) throw new IOException();
	                for(int i=0; i<parts.length; i++) {
	                    if(!isNumeric(parts[i])) throw new IOException();
	                    //else System.out.println(parts[i]);
	                }
	                int id = Integer.parseInt(parts[0]);
	                int nStudents = Integer.parseInt(parts[1]);
	                Exam e = new Exam(id, nStudents);
	                ArrayList<String> students = new ArrayList<String>();
	                e.setStudents(students);
	                exms.put(id, e);
	                //ex_st.put(id, students);
            	}
            }
            br_exm.close();
            
            return true;
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file");
            return false;
        }
    }

    public boolean loadStu(String file) {
        String st;
        try {

            File file_stud = new File(file);
            BufferedReader br_stu = new BufferedReader(new FileReader(file_stud));
            while ((st = br_stu.readLine()) != null) {
                //System.out.println(st);
            	if(st.length()>0){
	                String[] parts = st.split(" ");
	                if(parts.length != 2) throw new IOException();
	                if(!isNumeric(parts[1]) || isNumeric(parts[0])) throw new IOException();
	                //else {
	                //System.out.println(parts[0]);
	                //System.out.println(parts[1]);
	                //}
	                String idS = parts[0];
	                int idE = Integer.parseInt(parts[1]);
	                if(!exms.get(idE).getStudents().contains(idS) && exms.containsKey(idE)) {
	                    studs.add(idS);
	                    exms.get(idE).addStudent(idS);
	                }
	                else throw new IOException();
            	}
            }
            br_stu.close();
            return true;

        } catch (IOException e) {
            System.err.println("Errore nella lettura del file");
            return false;
        }
    }

    public int[][] buildNeEMatrix() {

        this.nEe = new int[exms.size()][exms.size()];
        ArrayList<String> eList, EList;

        for(Entry<Integer, Exam> entryExam1 : exms.entrySet()){
            for(Entry<Integer, Exam> entryExam2 : exms.entrySet()) {
                if(!entryExam1.getKey().equals(entryExam2.getKey())) {
                    eList = new ArrayList<>(entryExam1.getValue().getStudents());
                    EList = new ArrayList<>(entryExam2.getValue().getStudents());
                    eList.retainAll(EList);

                    nEe[entryExam1.getKey()-1][entryExam2.getKey()-1] = eList.size();
                }
            }
        }
        
       /*for (int[] row : nEe)
            // converting each row as string
            // and then printing in a separate line
            System.out.println(Arrays.toString(row)); 
*/
        return nEe;
    }

    public int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public Boolean areConflictual(int e1, int e2){
        if (nEe[e1-1][e2-1] != 0 && e1!=e2)
            return true;
        else
            return false;
    }

    public int checkVal(HashMap<Integer, Integer> solution, int nSlots, int e){
        boolean isConflict;

        if(!solution.isEmpty()){
            for(Map.Entry<Integer,Integer> entry : solution.entrySet())
            {
                if(entry.getValue().equals(nSlots))
                {
                    isConflict = areConflictual(e, entry.getKey());
                    if (isConflict){
                        return 1;
                    }
                }
            }
        }
        else
            return 0;
        return 0;
    }

    public HashMap<Integer, Integer> initialSol(){
        HashMap<Integer, Integer> solution = new HashMap<>();
        int nExam = exms.size();
        int nSlots;
        int flag=0;

        for (Integer e : exms.keySet()){
            flag = 1;
            nSlots = getRandomNumberUsingNextInt(1, n_timeslots+1);
            while(flag!=0){
                flag = checkVal(solution, nSlots, e);
                solution.put(e, nSlots);
                nSlots = getRandomNumberUsingNextInt(1, n_timeslots+1);
            }
        }

        System.out.println("Initial solution: " + solution.toString());
        return solution;
    }

    public double computePenalty(HashMap<Integer, Integer> solution){
        int dist;
        double penalty=0;
        double res=0;
        for (int i=0; i<nEe.length-1; i++){
            for (int j=i+1; j<nEe.length; j++){
                if(i!=j){
                    if (nEe[i][j]!=0){
                        dist = Math.abs(solution.get(i+1)-solution.get(j+1));
                        if(dist<=5){
                            res = Math.pow(2, 5-dist);
                            res = res*nEe[i][j]/studs.size();
                            penalty += res;
                        }
                        else
                            penalty += 0;
                    }
                }
            }
        }

        return penalty;
    }

}
