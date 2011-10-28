/**
 * 
 */
package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

/**
 * @author Jorge
 *
 */
public class PolicyIteration extends MDPEvaluation {

	/**
	 * @param id
	 * @throws Exception
	 */
	public PolicyIteration(ProbNet id) throws Exception {
		super(id);
	}
	
	/* (non-Javadoc)
	 * @see openmarkov.jorge.MDPAlgorithm#run(openmarkov.jorge.MDPParams)
	 * TODO: revisar con cambios priori/posterior
	 */
	@Override
	public void run(MDPParams params) throws Exception {
		MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
		MDPVariableConfig configPosteriori= mdpVariablePosteriori.config_iterator();
		MDPVariableConfig configActions= mdpActions.config_iterator();

		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		myPolicy= new MDPTablePolicy (mdpVariablePriori, mdpActions);
		MDPTablePolicy lastPolicy= null;
		
		// Resolucion gauss-jordan
		double[] vec_b = new double [mdpVariablePriori.potEstado.getTableSize()];
		double[][] mat_a= new double [mdpVariablePriori.potEstado.getTableSize()][mdpVariablePriori.potEstado.getTableSize()];
		
		// Se realizan iteraciones mientras no se cumpla el criterio de parada establecido
		for(;;) {
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
				configPriori.next();
			}		
			
			if (lastPolicy==null) {
				lastPolicy= new MDPTablePolicy(mdpVariablePriori, mdpActions);
			}
			else if (myPolicy.equals(lastPolicy)) {
				break;
			}				
			
			configPriori.reset();			
			while (configPriori.hasNext() ) {
				MDPVariableConfig policy= myPolicy.getPolicy (configPriori);
				lastPolicy.setPolicy (configPriori, policy);
				
				configPriori.next();
			}
			
			// Policy Evaluation
			int i=0;
			configPriori.reset();
			while (configPriori.hasNext() ) {
				MDPVariableConfig policy= myPolicy.getPolicy (configPriori);
				vec_b[i]= rewardFunction.eval(configPriori, policy);
				
				int j=0;
				configPosteriori.reset();
				while (configPosteriori.hasNext() ) {
					// Diagonal
					mat_a[i][j] = (i==j) ? 1.0 : 0.0;					
					mat_a[i][j] -= params.getDiscountRate() * posteriorProb (configPriori,policy,configPosteriori);
					
					configPosteriori.next();
					j++;
				}
				
				configPriori.next();
				i++;
			}

			int[] index = new int [mdpVariablePriori.potEstado.getTableSize()];
			double[] x= LinearEq.solve (mat_a, vec_b, index);
			
			i= 0;
			configPriori.reset();
			while (configPriori.hasNext() ) {
				valueFunction.setValue (configPriori, x[i]);
				
				configPriori.next();
				i++;
			}
		}
	}	
}
