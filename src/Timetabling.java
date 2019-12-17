import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("Files/instance01.slo");
        model.loadExm("Files/instance01.exm");
        model.loadStu("Files/instance01.stu");
        model.buildNeEMatrix();
        
        GeneticAlgorithm ga = new GeneticAlgorithm(model, 4);
        System.out.println("Number of exams: " + 
        						ga.getN_exams() +
        						" Number of students: " + ga.getN_students());
        ga.fit_predict();
    }
}
