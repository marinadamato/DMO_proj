import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Model {

	private int n_timeslots;
	private int[][] nEe;
	private HashMap<Integer, Exam> exms;
	private HashSet<String> studs;
	long timeStart;
	private String path;
	private double optPenalty;
	public boolean old_flag;

	public Model() {
		super();
		this.timeStart = System.currentTimeMillis();
		exms = new HashMap<Integer, Exam>();
		studs = new HashSet<String>();
		this.optPenalty = Double.MAX_VALUE;
		this.old_flag=false;
	}
	
	public boolean isNewOpt(Integer[] solution) {
		double penalty = computePenalty(solution);
		
		if(penalty<optPenalty) {
			optPenalty = penalty;
			this.writeFdile2(solution);
			return true;
		}
		
		return false;
	}
	
	public double getOptPenalty() {
		return this.optPenalty;
	}
	
	
	public void runGA(int tlim) {
		// Per definire numero cromosomi? valuto difficoltà di una istanza in base al rapporto tra media conflitti esame e timeslot,
		// più è alto il rapporto, maggiore è la difficoltà nel posizionare gli esami
		int count = 0;
		
		for (int i = 0; i < this.exms.size(); i++) 
			for (int j = i + 1; j < this.exms.size(); j++) 
				if(nEe[i][j] > 0)
					count += 1;
		//System.out.println((double) count/(this.exms.size()*this.n_timeslots)); 
		double difficultInstance = (double) count/(this.exms.size()*this.n_timeslots);
		int nChroms = (int) ((int) 10/difficultInstance); 
		
		System.out.println("Number Chrom: "+nChroms);
		
		
		GeneticAlgorithm ga = new GeneticAlgorithm(this, nChroms,tlim); // quanti cromosomi sarebbe meglio utilizzare??
		ga.fit_predict();
	}

	public int getN_timeslots() {
		return this.n_timeslots;
	}

	public int[][] getnEe() {
		return this.nEe;
	}

	public HashMap<Integer, Exam> getExms() {
		return this.exms;
	}

	public HashSet<String> getStuds() {
		return this.studs;
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
					} else
						throw new IOException();
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

		/*
		 * for (int[] row : nEe) // converting each row as string // and then printing
		 * in a separate line System.out.println(Arrays.toString(row));
		 */

		return nEe;
	}
	
	public Integer[] swapTimeslot(Integer[] sol) {
		Random rand = new Random();

		int timeS1 = rand.nextInt(this.n_timeslots)+1;
		int timeS2 = rand.nextInt(this.n_timeslots)+1;
		
		List<String> exm1 = new ArrayList<>();
		List<String> exm2 = new ArrayList<>();
		
		// System.out.println(model.computePenalty(child));
		
		for(int i =0; i<this.exms.size(); i++) {
			if(sol[i]==timeS1)
				exm1.add(String.valueOf(i));
		}
		
		for(int i =0; i<this.exms.size(); i++) {
			if(sol[i]==timeS2)
				exm2.add(String.valueOf(i));
		}
		
		List<String> exmSwap1 = new ArrayList<>(exm1);
		List<String> exmSwap2 = new ArrayList<>(exm2);
		
		for(String e : new ArrayList<>(exm1))
			for(String e2 : exm2)
				if(this.nEe[Integer.valueOf(e)][Integer.valueOf(e2)] > 0) {
					exmSwap1.remove(e);
					break;
				}
		
		for(String e2 : new ArrayList<>(exm2))
			for(String e : exm1)
				if(this.nEe[Integer.valueOf(e2)][Integer.valueOf(e)] > 0) {
					exmSwap2.remove(e2);
					break;
				}
		
		for(String e : exm1)
			sol[Integer.valueOf(e)] = timeS2;
		
		for(String e : exm2)
			sol[Integer.valueOf(e)] = timeS1;	
		
		
		return sol;
		
		// System.out.println(model.computePenalty(child));				
		
	}

	public double computePenalty(Integer[] solution) {
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
	
	public boolean areConflictual(int time_slot, int exam_id, Integer[] chrom) {
		for (int e = 0; e < this.exms.size(); e++) {
			if (e != exam_id && chrom[e] != null) {
				if (chrom[e] == time_slot && this.nEe[e][exam_id] != 0) {
					return true;
				}
			}
		}
		return false;
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
	public void writeFdile2(Integer[] sol) {
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
