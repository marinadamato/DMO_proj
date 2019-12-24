import java.io.*;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("instances/instance02.slo");
        model.loadExm("instances/instance02.exm");
        model.loadStu("instances/instance02.stu");
        model.buildNeEMatrix();
        TabuSearch ts = new TabuSearch(1, model);
        ts.run();
        //ts.run();
    }
}
