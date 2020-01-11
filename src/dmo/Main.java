package dmo;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws Exception, IOException {
		long start=System.nanoTime();
		long end=180;
		String instance="instance01";
		
		Model model = new Model(start, end);
		
		model.loadIstance("Instances/"+instance);
		model.findSolution();
	}
}
