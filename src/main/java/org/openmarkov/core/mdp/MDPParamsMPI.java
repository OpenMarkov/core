package org.openmarkov.core.mdp;

/**
 * @author Jorge
 *
 */
public class MDPParamsMPI extends MDPParams {
	/**
	 * 
	 */
	boolean adaptative;

	/**
	 * 
	 */
	int maxIterations;
	
	/**
	 * 
	 */
	int iterationsIncrement;
	
	/**
	 * 
	 */
	double partialEvaluationEpsilon;

	/**
	 * 
	 */
	public MDPParamsMPI () {
		super (0.99, 10e-4);
		adaptative= true;
		partialEvaluationEpsilon= 10e-1;
	}

	/**
	 * 
	 */
	MDPParamsMPI (double discountRate, double epsilon) {
		super (discountRate, epsilon);
		adaptative= true;
		partialEvaluationEpsilon= 10e-1;
	}
	
	/**
	 * @param maxIterations
	 * @param increment
	 */
	public void setOrderSequence (int maxIterations, int increment) {
		adaptative= false;
		this.maxIterations= maxIterations;
		iterationsIncrement= increment;
	}

	/**
	 * @param partialEvaluationEpsilon
	 */
	public void setAdaptativeOrderSequence (double partialEvaluationEpsilon) {
		adaptative= true;
		this.partialEvaluationEpsilon= partialEvaluationEpsilon;
	}
	
	/**
	 * @return
	 */
	public boolean isAdaptative() {
		return adaptative;
	}

	/**
	 * @return
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @param maxIterations
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @return
	 */
	public int getIterationsIncrement() {
		return iterationsIncrement;
	}

	/**
	 * @param iterationsIncrement
	 */
	public void setIterationsIncrement(int iterationsIncrement) {
		this.iterationsIncrement = iterationsIncrement;
	}

	/**
	 * @return
	 */
	public double getPartialEvaluationEpsilon() {
		return partialEvaluationEpsilon;
	}
}
