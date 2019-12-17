import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("test.slo");
        model.loadExm("test.exm");
        model.loadStu("test.stu");
        model.buildNeEMatrix();
        
        int n_exams = model.getExms().size();
        
        GeneticAlgorithm ga = new GeneticAlgorithm(model, 4, n_exams);
        ga.fit_predict();
    }
}
