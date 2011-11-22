/**
 * 
 */
package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;


/**
 * @author Jorge
 *
 */
public class ValueIteration extends MDPEvaluation {

	/**
	 * @param id
	 * @throws Exception
	 */
	public ValueIteration(ProbNet id) throws Exception {
		super(id);
	}

	/* (non-Javadoc)
	 * @see openmarkov.jorge.MDPAlgorithm#run(openmarkov.jorge.MDPParams)
	 */
	@Override
	public void run(MDPParams params) throws Exception {
		MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
		MDPVariableConfig configActions= mdpActions.config_iterator();

		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		newValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		myPolicy= new MDPTablePolicy (mdpVariablePriori, mdpActions);

		// Se establece cual va a ser el valor m�nima a utilizar como criterio de parada de las iteraciones
		double dSigma= Double.MAX_VALUE;
		double dMinSigma= params.getEpsilon()*(1-params.getDiscountRate())/params.getDiscountRate();

		// Se realizan iteraciones mientras no se cumpla el criterio de parada establecido
		while( dSigma > dMinSigma ) {
			stats.incrementNumIterations(1);
			
			// Recorrer uno a uno la tabla de estados posibles
			configPriori.reset();			
			while (configPriori.hasNext() ) {
				double dBestReward= Double.NEGATIVE_INFINITY;

				// Recorrer una a una todas las acciones posibles
				configActions.reset();
				while (configActions.hasNext()) {
					dBestReward= Math.max (dBestReward, bellmanUpdate (configPriori, configActions, params, valueFunction));
					configActions.next();
				}

				newValueFunction.setValue (configPriori, dBestReward);
				configPriori.next();
			}

			// Se recalcula el valor de sigma, utilizado como criterio de convergencia
			dSigma= valueFunction.calculateNormMax (newValueFunction);

			// Intercambio para la siguiente iteraci�n
			MDPTableValueFunction temp= valueFunction;
			valueFunction= newValueFunction;
			newValueFunction= temp;
		}

		// Se genera la politica correcta
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
			configPriori.next();
		}
	}	
}
