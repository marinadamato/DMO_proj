import java.io.IOException;
import java.util.ArrayList;

public class Exam implements Comparable<Exam> {
	
	private int id;
    private ArrayList<String> students = new ArrayList<String>();
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

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }
    
    public ArrayList<String> getStudents() {
        return this.students;
    }

    public void addStudent(String s) throws IOException {
        students.add(s);
        if(this.students.size() > this.number_st_enr) throw new IOException();
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
    
    public int compareTo(Exam e1) {
    	if(this.getNumber_st_enr()>e1.getNumber_st_enr())
    		return 1;
    	else if(this.getNumber_st_enr()<e1.getNumber_st_enr())
    		return -1;
    	else
    		return 0;
    }
    
    /*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Exam other = (Exam) obj;
		if (id != other.id)
			return false;
		return true;
	}*/
}

