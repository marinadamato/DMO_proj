import java.io.*;

public class Timetabling {

	public static void main(String[] args) throws Exception, IOException {
		Model model = new Model();
		// Upstream, based on origin/GeneticAlgorithm
		model.loadSlo("Instances/instance02.slo");
		model.loadExm("Instances/instance02.exm");
		model.loadStu("Instances/instance02.stu");
		model.buildNeEMatrix();

		GeneticAlgorithm ga = new GeneticAlgorithm(model, 10); // quanti cromosomi sarebbe meglio utilizzare?? 
		ga.fit_predict();
	}
}