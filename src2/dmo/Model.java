package dmo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class Model {
	
	private final long start;
	private final long end;
	private int n_timeslots;
	private HashMap<Integer, Exam> exms = new HashMap<Integer, Exam>();
	private HashSet<String> studs = new HashSet<String>();
	private String path;
	private int[][] conflicts;

	public Model(long start, long end) {
		this.start=start;
		this.end=end;
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
					Exam e = new Exam(Integer.parseInt(parts[0]), nStudents);
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

		this.conflicts = new int[exms.size()][exms.size()];
		ArrayList<String> eList, EList;

		for (Entry<Integer, Exam> entryExam1 : exms.entrySet()) {
			for (Entry<Integer, Exam> entryExam2 : exms.entrySet()) {
				if (!entryExam1.getKey().equals(entryExam2.getKey())) {
					eList = new ArrayList<>(entryExam1.getValue().getStudents());
					EList = new ArrayList<>(entryExam2.getValue().getStudents());
					eList.retainAll(EList);
					if(eList.size()>0) {
						if(!entryExam1.getValue().isInConflict(entryExam2.getValue())) {
							entryExam1.getValue().addConflict(entryExam2.getValue());
						}
						if(!entryExam2.getValue().isInConflict(entryExam1.getValue())) {
							entryExam2.getValue().addConflict(entryExam1.getValue());
						}
					}
					conflicts[entryExam1.getKey()][entryExam2.getKey()] = eList.size();
				} else
					conflicts[entryExam1.getKey()][entryExam2.getKey()] = Integer.MAX_VALUE;
			}
		}	
		
		for (int[] row : conflicts) 
			System.out.println(Arrays.toString(row));
		 
		return conflicts;
	}
	
	public void findSolution() {
		boolean[] threadTaken = new boolean[3];
		Parallel[] p = new Parallel[3];
		Thread t[] = new Thread[3];
		
		for(int i = 0; i < 3; ++i) {
			p[i] = new Parallel(n_timeslots, exms, conflicts, start, end);
			t[i] = new Thread(p[i]);
			t[i].start();
			threadTaken[i] = false;
		}
		
		// CODICE THREAD COPIATO BRUTALMENTE: DA VEDERE INSIEME

		for(int i = 0; i < 3; ++i) {
			/*
			 * For each thread, wait for a solution, then check feasibility
			 */
			int k = 0;
			do {
				k++;
				if(k >= 3) {
					k = 0;
				}
			}
			while(t[k].getState() != Thread.State.TERMINATED || threadTaken[k]);
			threadTaken[k] = true;

			try {
				t[k].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
