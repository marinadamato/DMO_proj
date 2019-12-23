import java.io.*;

public class Timetabling {

	public static void main(String[] args) throws Exception, IOException {
		Model model = new Model();
		// Upstream, based on origin/GeneticAlgorithm
		model.loadSlo("Instances/instance04.slo");
		model.loadExm("Instances/instance04.exm");
		model.loadStu("Instances/instance04.stu");

		model.buildNeEMatrix();

		GeneticAlgorithm ga = new GeneticAlgorithm(model, 4);
		ga.fit_predict();
	}
}