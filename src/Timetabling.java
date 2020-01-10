import java.io.*;

public class Timetabling {

	public static void main(String[] args) throws Exception, IOException {
		//ricordarsi di implementare la scirttura da terminale 
		//java -jar ETPsolverDMOgroupXX.jar instancename -t tlim
		int tlim=600;
		String instance="instance01";
		
		Model model = new Model();
		model.loadIstance("Instances/"+instance);
		model.runGA(tlim);
	}
}