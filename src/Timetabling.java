import java.io.*;

public class Timetabling {

	public static void main(String[] args) throws Exception, IOException {
		Model model = new Model();
		// Upstream, based on origin/GeneticAlgorithm
		model.loadSlo("Instances/instance05.slo");
		model.loadExm("Instances/instance05.exm");
		model.loadStu("Instances/instance05.stu");
		model.buildNeEMatrix();

		GeneticAlgorithm ga = new GeneticAlgorithm(model, 5);
		ga.fit_predict();
	}
}