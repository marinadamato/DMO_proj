package dmo;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws Exception, IOException {
		long start=System.nanoTime();
		long end=180;
		String instance="test";
		
		Model model = new Model(start, end);
		
		model.loadIstance("Test/"+instance);
		
	}
}
