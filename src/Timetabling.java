import java.io.*;

public class Timetabling {

	public static void main(String[] args) throws Exception, IOException {
		//ricordarsi di implementare la scirttura da terminale 
		//java -jar ETPsolverDMOgroupXX.jar instancename -t tlim
		int tlim=180;
		String instance="instance07";
		
		Model model = new Model();
		
		model.loadIstance("Instances/"+instance);

		GeneticAlgorithm ga = new GeneticAlgorithm(model, 12,tlim); // quanti cromosomi sarebbe meglio utilizzare??
		ga.fit_predict();
	}
}