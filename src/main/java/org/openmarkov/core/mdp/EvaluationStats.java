package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public class EvaluationStats {
	/**
	 * 
	 */
	private int numBackups;
	
	/**
	 * 
	 */
	private int numIterations;
	
	/**
	 * 
	 */
	public EvaluationStats () {
		numBackups= 0;
		numIterations= 0;
	}
	
	/**
	 * @param n
	 */
	public void incrementNumBackups (int n) {
		numBackups += n;
	}

	/**
	 * @param n
	 */
	public void incrementNumIterations (int n) {
		numIterations += n;
	}

	/**
	 * @return
	 */
	public int getNumBackups() {
		return numBackups;
	}

	/**
	 * @return
	 */
	public int getNumIterations() {
		return numIterations;
	}
}
