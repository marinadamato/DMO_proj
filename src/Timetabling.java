import java.io.*;

public class Timetabling {


    public static void main(String[] args) throws Exception, IOException {
        Model model = new Model();
        model.loadSlo("instances/instance01.slo");
        model.loadExm("instances/instance01.exm");
        model.loadStu("instances/instance01.stu");
        model.buildNeEMatrix();
        TabuSearch ts = new TabuSearch(1, model);
        ts.run();
        //ts.run();
    }
}
