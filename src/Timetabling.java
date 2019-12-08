import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timetabling {

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static void main(String[] args) throws Exception, IOException {

        String st;
        int n_timeslots;
        //HashMap<Integer, ArrayList<Student>> ex_st = new HashMap<Integer, ArrayList<Student>>();
        HashMap<Integer, Exam> exms = new HashMap<Integer, Exam>();
        ArrayList<Student> studs = new ArrayList<>();

        File file_time = new File("test.slo");
        BufferedReader br_t = new BufferedReader(new FileReader(file_time));
        while ((st = br_t.readLine()) != null)
            n_timeslots = Integer.parseInt(st);
        br_t.close();

        File file_exams = new File("test.exm");
        BufferedReader br_exm = new BufferedReader(new FileReader(file_exams));
        while ((st = br_exm.readLine()) != null) {

            String[] parts = st.split(" ");
            if(parts.length != 2) throw new Exception();
            for(int i=0; i<parts.length; i++) {
                if(!isNumeric(parts[i])) throw new Exception();
                else System.out.println(parts[i]);
            }
            int id = Integer.parseInt(parts[0]);
            int nStudents = Integer.parseInt(parts[1]);
            Exam e = new Exam(id, nStudents);
            ArrayList<Student> students = new ArrayList<Student>();
            e.setStudents(students);
            exms.put(id, e);
            //ex_st.put(id, students);

        }
        br_exm.close();

		/*for(Exam e : exms){
			System.out.println(e.toString());
		}*/

        File file_stud = new File("test.stu");
        BufferedReader br_stu = new BufferedReader(new FileReader(file_stud));
        while ((st = br_stu.readLine()) != null) {
            //System.out.println(st);
            String[] parts = st.split(" ");
            if(parts.length != 2) throw new Exception();
            if(!isNumeric(parts[1]) || isNumeric(parts[0])) throw new Exception();
            else {
                //System.out.println(parts[0]);
                //System.out.println(parts[1]);
            }
            String idS = parts[0];
            int idE = Integer.parseInt(parts[1]);
            Student s = new Student(idS);
            if(studs.contains(s)) {
                if(!exms.get(idE).getStudents().contains(s)) {
                    s.addExams(exms.get(idE));
                }
            }
            else {
                s.addExams(exms.get(idE));
                studs.add(s);
                exms.get(idE).addStudent(s);
            }
        }
        br_stu.close();

		/*System.out.println();

		for (Integer e : ex_st.keySet() ){
			System.out.println(e);
			for (Student s : ex_st.get(e)){
				System.out.println(s.toString());
			}
		}*/
        //if(args.length == 0 || !isNumeric(args[0])) throw new Exception();
        TabuSearch ts = new TabuSearch(1);
        ts.generate_conflicts(studs);

    }
}

