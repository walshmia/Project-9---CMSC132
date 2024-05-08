package spss;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

/*  Mia Walsh, CMSC132 - 0303 
 * This class represents a submission server and uses a HashMap to keep
 * track of students and their submissions. A submission server has 
 * integers representing the number of tests, students, and submissions.
 * This class also extends the Thread class in order to read submissions
 * concurrently.
 * I pledge on my honor that I have not given nor received any unauthorized 
 * assistance on this assignment. */

public class SPSS extends Thread {
	
	private HashMap<String, LinkedHashMap<Integer, List<Integer>>> 
	studentScores;
	private int numTests, numStudents, numSubmissions;

    public SPSS(int numTests) {
    	
    	this.numTests = numTests > 0 ? numTests : 1;
    	studentScores = new HashMap<>();
    }

    /* This method adds a student to the SPSS if the student has not already 
     * been added. 
     * @param newStudent The name of the student being added
     * @return true if the student was successfully added, false otherwise */
    
    public boolean addStudent(String newStudent) {
        
    	if (newStudent != null && !newStudent.isEmpty()) {
    		
    		// Add student if it is not already present in the map
    		if (studentScores.get(newStudent) == null) {
    		
    			// Instantiate the scores LinkedHashMap when adding the student
    			studentScores.put(newStudent, new LinkedHashMap<>());
    			numStudents++;
    			
    			return true;
    		}
    	}
    	
    	return false;
    }

    /* Returns the number of students in the SPSS.
     * @return the integer representing the number of students */
    
    public int numStudents() {
        return numStudents;
    }

    /* This method adds a list of test scores to a student's submission if
     * the student exists in the SPSS.
     * @param name The name of the student whose submission is being added
     * @param testResults The list of test scores being added to the submission
     * @return true if the submission is successfully added, false otherwise */
    
    public boolean addSubmission(String name, List<Integer> testResults) {
    	
    	if (testResults != null && name != null && 
    			testResults.size() == numTests) {
        	
    		// Student must already exist in the SPSS
    		if (studentScores.containsKey(name)) {
    			
    			// Find map of submissions for specified student
    			LinkedHashMap<Integer, List<Integer>> subs = 
    					studentScores.get(name);
    			
    			// Number the submissions in order
    			subs.put(subs.size() + 1, testResults);
    			numSubmissions++;
    			
    			return true;
    		}
        }
    	
    	return false;
    }

    /* This method uses threads to add data for project submissions to the
     * current SPSS object concurrently. Each thread will read the content
     * of one file, extract the data from the file,and store the data in 
     * the SPSS.
     * @param fileNames The list of files that will be read concurrently
     * @return true if submissions are read, false otherwise */
    
    public boolean readSubmissionsConcurrently(List<String> fileNames) {
        
    	if (fileNames != null) {
    		
    		// Create a thread for each file in the parameter list of files
    		Thread[] threads = new Thread[fileNames.size()];
    		
    		// Initialize all threads
    		for (int i = 0; i < threads.length; i++) {
    			threads[i] = new Thread(new MyThread(fileNames.get(i)));
    		}
    		
    		// Start all threads
    		for (Thread t : threads) {
    			t.start();
    		}
    			
    		// Ensure all threads finish before return value returns
    		for (Thread t : threads) {
    			try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    		
    		return true;
    	}
    	
    	return false;
    }

    /* This method calculates the score of the best submission a certain 
     * student made. 
     * @param name A specific student in the SPSS
     * @return the integer representing the score of the best submission a
     * student made */
    
    public int score(String name) {
    	
    	if (name != null && !name.isEmpty()) {
    		
    		if (studentScores.containsKey(name)) {
    		
    			return calcScore(bestSubmission(name));
    		}
    	}
    	
    	return -1;
    }

    /* Returns the number of submissions a specific student has made.
     * @param name The name of the student whose submissions will be found 
     * @return The integer representing the number of submissions a student
     * has made */
    
    public int numSubmissions(String name) {
    	
    	if (name != null && !name.isEmpty()) {
    		
    		if (studentScores.containsKey(name)) {
    		
    			return studentScores.get(name).size();
    		}
    	}
        
    	// Return -1 if student is not present in the SPSS
    	return -1;
    }

    /* Returns the total number of submissions made in the SPSS.
     * @return the integer representing the total number of submissions */
    
    public int numSubmissions() {
    	
        return numSubmissions;
    }

    /* This method takes the best submission of a specific student and figures
     * out if the submission is satisfactory or not, meaning that there is a
     * small number of zeros relative to the number of test scores. 
     * @param name The student whose submission is tested
     * @return true if the submission is satisfactory, false otherwise */
    
    public boolean satisfactory(String name) {
    	
    	boolean retVal = false;
        
    	if (name != null && !name.isEmpty()) {
    		
    		if (studentScores.containsKey(name)) {
    		
    			List<Integer> bestSub = bestSubmission(name);
    			
    			if (bestSub != null) {
    		
    				int zeroCount = zeroCount(bestSub);
    		
    				// Test number is even
    				if (bestSub.size() % 2 == 0) {
    			
    					if (zeroCount <= bestSub.size()/2) {
    				
    						retVal = true;
    					}
    					
    				// Test number is odd
    				} else {
    			
    					if (zeroCount < bestSub.size()/2 + 1) {
    				
    						retVal = true;
    					}
    				}
    			}
    		}
    	}
    	return retVal;
    }

    /* This method determines whether or not a student gets extra credit
     * based on their best submission. They will get extra credit if they have
     * only made one submission and the test scores are not 0.
     * @param name The student whose submission is tested
     * @return true if the student gets extra credit, false otherwise */
    
    public boolean gotExtraCredit(String name) {
    	
    	boolean retVal = false;
        
    	if (name != null && !name.isEmpty()) {
    		
    		if (studentScores.containsKey(name)) {
    		
    			List<Integer> bestSub = bestSubmission(name);
    		
    			if (bestSub!= null && numSubmissions(name) == 1) {
    			
    				if (zeroCount(bestSub) == 0) {
    				
    					retVal = true;
    				}
    			}
    		}
    	}
    	return retVal;
    }
    
    // Private helper method, calculates the score of a submission
    private int calcScore(List<Integer> list) {
    	 
    	int sum = 0;
    	
    	for (Integer i : list) {
    		
    		sum += i;
    	}
    	
    	return sum;
    }
    
    // Private helper method, counts the number of zeros in a submission
    private int zeroCount(List<Integer> i) {
    	
    	int count = 0;
    	
    	for (Integer score : i) {
    		
    		if (score == 0) {
    			
    			count = count + 1;
    		}
    	}
    	
    	return count;
    }
    
    // Private helper method, finds a student's best submission
    private List<Integer> bestSubmission(String name) {
    	
    	List<Integer> temp = studentScores.get(name).get(1);
    	
    	if (temp != null) {
    		
    		for (Integer i : studentScores.get(name).keySet()) {
    			
    			List<Integer> newScore = studentScores.get(name).get(i);
    			
    			if (calcScore(temp) < calcScore(newScore)) {
    				
    				temp = newScore;
    			}
    		}
    	}
    	
    	return temp;
    }
    
    /* Inner thread class containing an overridden run method. This class
     * must be used in the readSubmissionsConcurrently() method because it
     * uses threads. */
    
    private class MyThread extends Thread {
    	
    	private static Object OBJ = new Object();
    	private String fileName;
    	
    	public MyThread(String fileName) {
    		
    		this.fileName = fileName;
    	}
    	
    	/* This method runs when start is called on a thread. It takes one
    	 * file and reads all the data contained in the file. It also 
    	 * will add all the data to the current SPSS. */
    	
    	public void run() {
    		
    		Scanner objScanner;
    		String studentName = "";
    		List<Integer> scores = new ArrayList<>();
    			
    		synchronized (OBJ) {
    				
    			try {
    				
    				FileReader fileReader = new FileReader(fileName);
    				objScanner = new Scanner(fileReader);
    				
    				while (objScanner.hasNextLine()) {
    					studentName = objScanner.next();
        			
    					while (objScanner.hasNextInt()) {
        				
    						scores.add(objScanner.nextInt());
    					}
    					
    					objScanner.nextLine();
    					
    	    			addSubmission(studentName, scores);
    	    			scores = new ArrayList<>();
    				}
        			
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }

}
