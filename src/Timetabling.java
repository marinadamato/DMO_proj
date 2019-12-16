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
        
    }
}
