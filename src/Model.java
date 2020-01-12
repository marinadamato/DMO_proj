import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Model {

	private static final int CHROM_NUMBER = 8;
	private static final int THREADS_NUMBER = 3;
	
	private int n_timeslots;
	private int[][] nEe;
	private HashMap<Integer, Exam> exms;
	private HashSet<String> studs;
	private long timeStart;
	private String path;
	private double optPenalty;
	public boolean old_flag;
	private List<Integer[]> minLoc;
	private Integer[] optSolution;

	public Model() {
		super();
		this.timeStart = System.currentTimeMillis();
		exms = new HashMap<Integer, Exam>();
		studs = new HashSet<String>();
		this.optPenalty = Double.MAX_VALUE;
		this.old_flag=false;
		this.minLoc = new ArrayList<Integer[]>();
	}
	
	public void runGA() {
		int count = 0;
		
		for (int i = 0; i < this.exms.size(); i++) 
			for (int j = i + 1; j < this.exms.size(); j++) 
				if(nEe[i][j] > 0)
					count += 1;
		
		double difficultInstance = (double) count/(this.exms.size()*this.n_timeslots);
		int dimPopulation = (int) ((int) CHROM_NUMBER/difficultInstance); 
		
		if(dimPopulation<4)
			dimPopulation = 4;
		
		/*
		 * Threads creation
		 */
		boolean[] threadTaken = new boolean[THREADS_NUMBER];
		GeneticAlgorithm[] generators = new GeneticAlgorithm[THREADS_NUMBER];
		Thread t[] = new Thread[THREADS_NUMBER];

		for(int i = 0; i < THREADS_NUMBER; ++i) {
			generators[i] = new GeneticAlgorithm(this, dimPopulation+i);
			t[i] = new Thread(generators[i]);
			t[i].start();
			threadTaken[i] = false;
		}	
	
	}
	
	public synchronized boolean isNewOpt(Integer[] solution) {
		double penalty = computePenalty(solution);
		optSolution = solution.clone();
		
		if(penalty<optPenalty) {
			optPenalty = penalty;
			this.writeFdile2(solution);
			System.out.println( "\nTime: " + (System.currentTimeMillis() - this.timeStart) / 1000
					+ "s - Optimal: "+optPenalty);
			return true;
		}
		
		return false;
	}
	
	public synchronized double computePenalty(Integer[] solution) {
		int dist;
		double penalty = 0;

		for (int i = 0; i < this.exms.size(); i++) {
			for (int j = i + 1; j < this.exms.size(); j++) {
				dist = Math.abs(solution[i] - solution[j]);
				if (dist <= 5)
					penalty += (Math.pow(2, 5 - dist) * nEe[i][j]);
			}
		}

		penalty = penalty / studs.size();

		return penalty;
	}
	
	// uno dei dei principali problemi del tabusearch iniziale era che risultava
	// lentissimo. Per ogni iterazione si andava
	// a calcolare la penalità dell'intera soluzione (n-esami*timeslot volte). Per
	// ridurre il carico, ora mi
	// vado a calcolare solo la penalità generata dal un singolo esame
	public synchronized double computePenaltyByExam(Integer[] chrom, int exam) {
		int dist;
		double penalty = 0;

		for (int i = 0; i < this.exms.size(); i++) {
			if (exam != i) {
				dist = Math.abs(chrom[exam] - chrom[i]);
				if (dist <= 5)
					penalty += (Math.pow(2, 5 - dist) * this.nEe[exam][i]);
			}
		}

		return penalty / getStuds();
	}
	
	public synchronized boolean areConflictual(int time_slot, int exam_id, Integer[] chrom) {
		for (int e = 0; e < this.exms.size(); e++) {
			if (e != exam_id && chrom[e] != null) {
				if (chrom[e] == time_slot && this.nEe[e][exam_id] != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public synchronized void addMinLoc(Integer[] clone) {
		minLoc.add(clone);
	}
	
	public synchronized double getOptPenalty() {
		return this.optPenalty;
	}
	
	public synchronized Integer[] getOptSolution() {
		return this.optSolution;
	}

	public synchronized int getN_timeslots() {
		return this.n_timeslots;
	}

	public synchronized int[][] getnEe() {
		return this.nEe;
	}

	public synchronized HashMap<Integer, Exam> getExms() {
		return this.exms;
	}

	public synchronized int getStuds() {
		return this.studs.size();
	}
	
	public synchronized List<Integer[]> getMinLoc() {
		return minLoc;
	}

	public void loadIstance(String path) {
		this.path=path;
		this.loadSlo(path+".slo");
		this.loadExm(path+".exm");
		this.loadStu(path+".stu");
		this.buildNeEMatrix();
	}
	
	private boolean loadSlo(String file) {
		String st;
		try {
			File file_time = new File(file);
			BufferedReader br_t = new BufferedReader(new FileReader(file_time));

			while ((st = br_t.readLine()) != null)
				n_timeslots = Integer.parseInt(st);
			br_t.close();

			return true;

		} catch (IOException e) {
			System.err.println("Errore nella lettura del file slo");
			return false;
		}
	}

	private boolean loadExm(String file) {
		String st;
		int id = 0;
		try {
			File file_exams = new File(file);
			BufferedReader br_exm = new BufferedReader(new FileReader(file_exams));
			while ((st = br_exm.readLine()) != null) {
				if (st.length() > 0) {
					String[] parts = st.split(" ");
					int nStudents = Integer.parseInt(parts[1]);
					Exam e = new Exam(parts[0], nStudents);

					ArrayList<String> students = new ArrayList<String>();
					e.setStudents(students);

					exms.put(id++, e);
				}
			}
			br_exm.close();

			return true;
		} catch (IOException e) {
			System.err.println("Errore nella lettura del file exm");
			return false;
		}
	}

	private boolean loadStu(String file) {
		String st;
		try {

			File file_stud = new File(file);
			BufferedReader br_stu = new BufferedReader(new FileReader(file_stud));
			while ((st = br_stu.readLine()) != null) {
				if (st.length() > 0) {
					String[] parts = st.split(" ");
					String idS = parts[0];
					int idE = Integer.parseInt(parts[1]) - 1;

					if (!exms.get(idE).getStudents().contains(idS) && exms.containsKey(idE)) {
						studs.add(idS);
						exms.get(idE).addStudent(idS);
					} else throw new IOException();
				}
			}
			br_stu.close();
			
			return true;
		} catch (IOException e) {
			System.err.println("Errore nella lettura del file stu");
			return false;
		}
	}

	private int[][] buildNeEMatrix() {

		this.nEe = new int[exms.size()][exms.size()];
		ArrayList<String> eList, EList;

		for (Entry<Integer, Exam> entryExam1 : exms.entrySet()) {
			for (Entry<Integer, Exam> entryExam2 : exms.entrySet()) {
				if (!entryExam1.getKey().equals(entryExam2.getKey())) {
					eList = new ArrayList<>(entryExam1.getValue().getStudents());
					EList = new ArrayList<>(entryExam2.getValue().getStudents());
					eList.retainAll(EList);

					nEe[entryExam1.getKey()][entryExam2.getKey()] = eList.size();
				} else
					nEe[entryExam1.getKey()][entryExam2.getKey()] = Integer.MAX_VALUE;

			}
		}

		return nEe;
	}

	public void writeFdile(Integer[] sol) {
		try {
			File file = new File(this.path+"_DMOgroup16.sol");
			file.createNewFile();
			FileWriter myWriter = new FileWriter(file, false);
			for (int i = 0; i < sol.length; i++) {
				myWriter.write(exms.get(i).getID() + " " + sol[i] + "\n");
			}
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred");
			e.printStackTrace();
		}
	}
	
	// to eliminate before sending the code to Manerba
	public synchronized void writeFdile2(Integer[] sol) {
		File file = new File(this.path+"_DMOgroup16.sol");
		
		double old_pen=200;
		try {
			BufferedReader old_file = new BufferedReader(new FileReader(file));
			old_pen = Double.parseDouble(old_file.readLine());
			old_file.close();
			//System.out.println(old_pen);
		}
		catch (IOException e){};
		if (old_pen>this.optPenalty) {
			this.old_flag=true;
			try {
				file.createNewFile();
				FileWriter myWriter = new FileWriter(file, false);
				myWriter.write(this.optPenalty+ "\n");
				for (int i = 0; i < sol.length; i++) {
					myWriter.write(exms.get(i).getID() + " " + sol[i] + "\n");
				}
				myWriter.close();
			} catch (IOException e) {
				System.out.println("An error occurred");
				e.printStackTrace();
			}
		}
	}

}
