import java.io.*;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("Files/instance01.slo");
        model.loadExm("Files/instance01.exm");
        model.loadStu("Files/instance01.stu");
        model.buildNeEMatrix();
        
        GeneticAlgorithm ga = new GeneticAlgorithm(model, 4);
        ga.fit_predict();
    }
}