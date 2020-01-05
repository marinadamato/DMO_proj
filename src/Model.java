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

	public Model() {
		super();
		this.timeStart = System.currentTimeMillis();
		exms = new HashMap<Integer, Exam>();
		studs = new HashSet<String>();
		this.optPenalty = Double.MAX_VALUE;
	}
	
	public boolean isNewOpt(Integer[] solution) {
		double penalty = computePenalty(solution);
		
		if(penalty<optPenalty) {
			optPenalty = penalty;
			this.writeFdile(solution);
			return true;
		}
		
		return false;
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
	
	public boolean are_conflictual(int time_slot, int exam_id, Integer[] chrom) {
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

}
