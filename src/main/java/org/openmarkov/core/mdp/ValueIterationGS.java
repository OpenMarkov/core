package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

/**
 * @author Jorge
 *
 */
public class ValueIterationGS extends ValueIteration {

	/**
	 * @param id
	 * @throws Exception
	 */
	public ValueIterationGS(ProbNet id) throws Exception {
		super(id);
	}

	/** Backup del estado MDP
	 *  Qn+1(s,a)= C(a) + gamma*sum_s'{ P(s'|s,a)*Jn(s') }
	 * @throws Exception 
	 */
	public double backup (MDPVariableConfig configPriori,
			MDPVariableConfig configActions,
			MDPParams params,
			MDPTableValueFunction valueFunction) throws Exception {
		stats.incrementNumBackups(1);
		double reward= rewardFunction.eval (configPriori, configActions);

		MDPVariableConfig configPosteriori= mdpVariablePosteriori.config_iterator();
		double dSumatorio= 0.0;

		while (configPosteriori.hasNext()) {
			double vs;
			
			if (configPosteriori.iPos>=configPriori.iPos) {
				vs= valueFunction.getValue (configPosteriori);
			}
			else {
				vs= newValueFunction.getValue (configPosteriori);
			}

			dSumatorio += posteriorProb (configPriori, configActions, configPosteriori) * vs;
			configPosteriori.next();
		}

		return reward + params.getDiscountRate()*dSumatorio;
	}			
}
