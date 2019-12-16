
public class GeneticAlgorithm {
	
	private int[][] population;
	private Model model;
	private int n_chrom;
	private int n_exams;
	private Integer[][] nEe;
	 
	public GeneticAlgorithm(Model model, int n_chrom, int n_exams) {
		super();
		this.model = model;
		this.n_chrom = n_chrom;
		this.n_exams = n_exams;
		this.nEe = model.getnEe();
		
		population = new int[n_chrom][n_exams];
		
		
	}
	
	private boolean areConflictual(int time_slot, int exam_id, int[] chrom) {		
		for(int e = 0; e < this.n_exams; e++) {
			if(chrom[e] == time_slot && this.nEe[e][exam_id] != 0 && e != exam_id) {
				return true;
			}
		}
		return false;
	}
	
	public void computeStartPopulation() {		
		int time_slot = 0;
		
		for(int c=0; c<n_chrom; c++) {
			time_slot = 0;
			for(int e=0; e < n_exams; e++) {
				if( !areConflictual(time_slot, e, population[c])) {
					population[c][e] = time_slot;
				}				
				time_slot++;
			}
		}
	}
	
	
	
	 
}
