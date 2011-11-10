package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

/**
 * @author Jorge
 *
 */
public class AsynchronousValueIteration extends ValueIteration {

	/**
	 * @param id
	 * @throws Exception
	 */
	public AsynchronousValueIteration(ProbNet id) throws Exception {
		super(id);
	}

	/** 
	 * @see org.openmarkov.core.mdp.ValueIteration#run(org.openmarkov.core.mdp.MDPParams)
	 */
	@Override
	public void run(MDPParams params) throws Exception {
		MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
		MDPVariableConfig configActions= mdpActions.config_iterator();

		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		myPolicy= new MDPTablePolicy (mdpVariablePriori, mdpActions);

		MDPTableValueFunction lastValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		
		// Se establece cual va a ser el valor m�nima a utilizar como criterio de parada de las iteraciones
		double dSigma= Double.MAX_VALUE;
		double dMinSigma= params.getEpsilon()*(1 - params.getDiscountRate())/params.getDiscountRate();

		// Se realizan iteraciones mientras no se cumpla el criterio de parada establecido
		for(;;) {
			stats.incrementNumIterations(1);
			
			// Recorrer uno a uno la tabla de estados posibles
			if (!configPriori.hasNext()) {
				// Se recalcula el valor de sigma, utilizado como criterio de convergencia
				dSigma= valueFunction.calculateNormMax(lastValueFunction);
				
				if ( dSigma <= dMinSigma ) {
					break;
				}
				
				// Copia de la etapa anterior para test de parada
				lastValueFunction.copyFrom(valueFunction);
				
				configPriori.reset();							
			}
			
			double dBestReward= Double.NEGATIVE_INFINITY;

			// Recorrer una a una todas las acciones posibles
			configActions.reset();
			while (configActions.hasNext()) {
				dBestReward= Math.max (dBestReward, bellmanUpdate (configPriori, configActions, params, valueFunction));
				configActions.next();
			}

			// Se actualiza sobre la propia ValueFunction
			valueFunction.setValue (configPriori, dBestReward);
			configPriori.next();
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
