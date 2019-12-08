import java.util.ArrayList;

public class Exam {

    private int id;
    private ArrayList<Student> students = new ArrayList<Student>();
    private int number_st_enr;

    public Exam(int id, int number_st_enr) {
        this.id=id;
        this.number_st_enr=number_st_enr;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID(){
        return this.id;
    }

    public void setStudents(ArrayList<Student> students) {

        this.students = students;

    }
    public ArrayList<Student> getStudents() {

        return this.students;
    }

    public void addStudent(Student s) throws Exception {
        students.add(s);
        if(this.students.size() > this.number_st_enr) throw new Exception();
    }

    public void setNumber_st_enr(int number_st_enr){
        this.number_st_enr=number_st_enr;
    }

    public int getNumber_st_enr(){
        return this.number_st_enr;
    }
    public String toString(){
        return this.id + ", " + this.number_st_enr;
    }
}

