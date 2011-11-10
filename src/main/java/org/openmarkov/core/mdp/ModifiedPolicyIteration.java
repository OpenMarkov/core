/**
 * 
 */
package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

/**
 * @author Jorge
 *
 */
public class ModifiedPolicyIteration extends MDPEvaluation {

	/**
	 * 
	 */
	boolean bAdaptative;
	
	/**
	 * 
	 */
	protected int partialEvaluationCounter;
	
	/**
	 * 
	 */
	protected int partialEvaluationMaxIterations;
	
	/**
	 * 
	 */
	protected int partialEvaluationIterationIncrement;
	
	/**
	 * 
	 */
	double dMinSigmaPartialEvaluation;

	/**
	 * @param id
	 * @throws Exception
	 */
	public ModifiedPolicyIteration(ProbNet id) throws Exception {
		super(id);

		bAdaptative= false;
		partialEvaluationCounter= 0;
		partialEvaluationMaxIterations= 2*id.getNumNodes()+1;
		partialEvaluationIterationIncrement= 0;
	}
	
	
	/**
	 * 
	 */
	protected void initPartialPolicyEvaluation () {
		partialEvaluationCounter= 0;
	}
	
	/**
	 * @return
	 * @throws Exception 
	 */
	protected boolean isPartialPolicyEvaluationFinished () throws Exception {
		if (bAdaptative) {
			double dPartialSigma= valueFunction.calculateNormMax(newValueFunction);
			return dPartialSigma < dMinSigmaPartialEvaluation;
		}
		else if (partialEvaluationCounter >= partialEvaluationMaxIterations) {
			// Incremento del numero de iteracion por evaluacion parcial
			partialEvaluationMaxIterations += partialEvaluationIterationIncrement;
			return true;
		}

		return false;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected void nextPartialPolicyIteration () throws Exception {
		partialEvaluationCounter++;
	}
	
	/**
	 * @see openmarkov.jorge.MDPAlgorithm#run(openmarkov.jorge.MDPParams)
	 */
	@Override
	public void run(MDPParams obj) throws Exception {
		if (!(obj instanceof MDPParamsMPI)) {
			throw new IllegalArgumentException();
		}
		MDPParamsMPI params= (MDPParamsMPI) obj;

		// Bucle de evaluacion parcial de la politica;
		dMinSigmaPartialEvaluation= 0.0;
		partialEvaluationMaxIterations= 0;
		partialEvaluationIterationIncrement= 0;
		bAdaptative= params.isAdaptative();
		if (bAdaptative) {
			dMinSigmaPartialEvaluation= params.getPartialEvaluationEpsilon()*(1-params.getDiscountRate())/params.getDiscountRate();
		}
		else {
			partialEvaluationMaxIterations= params.getMaxIterations();
			partialEvaluationIterationIncrement= params.getIterationsIncrement();
		}
		
		MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
		MDPVariableConfig configActions= mdpActions.config_iterator();

		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		newValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		myPolicy= new MDPTablePolicy (mdpVariablePriori, mdpActions);

		// Se establece cual va a ser el valor m�nima a utilizar como criterio de parada de las iteraciones
		double dSigma= Double.MAX_VALUE;
		double dMinSigma= params.getEpsilon()*(1-params.getDiscountRate())/params.getDiscountRate();

		MDPTableValueFunction lastValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		
		// Se realizan iteraciones mientras no se cumpla el criterio de parada establecido
		while (dSigma>dMinSigma) {
			stats.incrementNumIterations(1);

			// Policy improvement
			configPriori.reset();			
			while (configPriori.hasNext() ) {
				double dBestReward= Double.NEGATIVE_INFINITY;

				// Recorrer una a una todas las acciones posibles
				configActions.reset();
				while (configActions.hasNext()) {
					double reward= bellmanUpdate (configPriori, configActions, params, valueFunction);

					if (reward>dBestReward) {
						myPolicy.setPolicy (configPriori, configActions);
						dBestReward= reward;
					}
					configActions.next();
				}
				
				assert (dBestReward > Double.NEGATIVE_INFINITY);
				newValueFunction.setValue (configPriori, dBestReward);
				lastValueFunction.setValue (configPriori, dBestReward);			
				
				configPriori.next();
			}		
			
			MDPTableValueFunction temp= valueFunction;
			valueFunction= newValueFunction;
			newValueFunction= temp;
			
			// Policy Evaluation
			initPartialPolicyEvaluation ();
			
			// PDTE: Modificar adaptativo si epsilon < epsilon_partialEval
			while (!isPartialPolicyEvaluationFinished ()) {
				configPriori.reset();			
				while (configPriori.hasNext() ) {
					MDPVariableConfig policy_act= myPolicy.getPolicy (configPriori);
					double reward= bellmanUpdate (configPriori, policy_act, params, valueFunction);
					newValueFunction.setValue (configPriori, reward);
					
					configPriori.next();
				}
				
				// Intercambio para la siguiente iteraci�n
				temp= valueFunction;
				valueFunction= newValueFunction;
				newValueFunction= temp;
								
				nextPartialPolicyIteration();
			}

			// Se recalcula el valor de sigma, utilizado como criterio de convergencia
			dSigma= valueFunction.calculateNormMax(lastValueFunction);
			
			if (dSigma<dMinSigma) {
				break;
			}
		}
	}	
}
