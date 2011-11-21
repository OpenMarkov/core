package org.openmarkov.core.mdp;

/**
 *
 */
public class RandomMatrix {
	double [][]matrix;
	int numTransitionsPerState;
	
	/**
	 * @param n
	 * @param numTransitionsPerState
	 */
	public RandomMatrix (int n, int numTransitionsPerState) {
		matrix= new double[n][n];
		
		this.numTransitionsPerState= numTransitionsPerState;
		fillRandom();
	}
	
	
	public void fillRandom () {
		int n= matrix.length;
		
		for (int i=0; i<n; i++) {
			double prob= 1.0;
			boolean []used= new boolean[n];
			int pending= numTransitionsPerState;
			
			while (pending>0) {
				int state= (int) (n*Math.random());
				while (used[state]) {
					state= (state+1)%n;
				}
				
				used[state]= true;
				matrix[i][state]= (pending==1) ? prob : prob*Math.random();
				prob -= matrix[i][state];
				pending--;
			}				
		}
	}	
	
}
