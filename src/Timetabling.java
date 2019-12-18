import java.io.*;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("test.slo");
        model.loadExm("test.exm");
        model.loadStu("test.stu");
        model.buildNeEMatrix();
        
        GeneticAlgorithm ga = new GeneticAlgorithm(model, 4);
        ga.fit_predict();
    }
}