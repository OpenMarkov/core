package org.openmarkov.core.mdp;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;

/**
 * @author Jorge
 * TODO: pdte comprobar funcionamiento
 */
public class BackwardInduction extends MDPEvaluation {
	MDPFiniteHorizonTablePolicy mdpPolicyFH;
	
	/**
	 * @param id
	 * @throws Exception
	 */
	public BackwardInduction(ProbNet id, int horizonLength) throws Exception {
		super(id);		
	}

	@Override
	public void run(MDPParams obj) throws NotEnoughMemoryException, Exception {
		if (!(obj instanceof MDPFiniteHorizonParams)) {
			throw new IllegalArgumentException();
		}
		MDPFiniteHorizonParams params= (MDPFiniteHorizonParams) obj;
		
		// TODO Auto-generated method stub
		MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
		MDPVariableConfig configActions= mdpActions.config_iterator();

		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		newValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		mdpPolicyFH= new MDPFiniteHorizonTablePolicy (params.getHorizonLength(), mdpVariablePriori, mdpActions);

		for (int i=0; i< params.getHorizonLength(); i++) {
			configPriori.reset();			
			while (configPriori.hasNext() ) {
				double dBestReward= Double.NEGATIVE_INFINITY;
	
				// Recorrer una a una todas las acciones posibles
				configActions.reset();
				while (configActions.hasNext()) {
					double reward= bellmanUpdate (configPriori, configActions, params, valueFunction);
	
					if (reward>dBestReward) {
						mdpPolicyFH.getTimeSlicePolicy(i).setPolicy (configPriori, configActions);
						dBestReward= reward;
					}
	
					configActions.next();
				}
	
				newValueFunction.setValue (configPriori, dBestReward);
				configPriori.next();
			}
			
			valueFunction= newValueFunction;
		}
	}
	
	public MDPPolicy getPolicy() {
		throw new UnsupportedOperationException();
	}
	
	public MDPFiniteHorizonPolicy getFiniteHorizonPolicy() {
		return mdpPolicyFH;
	}
}
