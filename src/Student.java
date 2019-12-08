import java.util.ArrayList;

public class Student {
    private String id;
    private ArrayList<Exam> exams = new ArrayList<Exam>();

    public Student(String id){
        this.id=id;
    }

    public String getId(){
        return this.getId();
    }

    public void setId(String id){
        this.id=id;
    }

    public ArrayList<Exam> getExams(){
        return this.exams;
    }

    public void setExams(ArrayList<Exam> exams){
        this.exams=exams;
    }

    public void addExams(Exam e){
        exams.add(e);
    }

    public String toString(){
        String st = "";
        for(Exam e : this.exams)
            st += e.toString() + " ";
        return this.id + ", " + st;
    }
}

