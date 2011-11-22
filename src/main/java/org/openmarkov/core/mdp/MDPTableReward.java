package org.openmarkov.core.mdp;

import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author Jorge
 *
 */
public class MDPTableReward extends MDPReward {

	/**
	 * 
	 */
	ProbNet id;

	/**
	 * 
	 * @param id
	 * @throws Exception
	 * 
	 * TODO: permitir varios potenciales de utilidad
	 * TODO: crear una excepcion personalizada para los MDPs
	 */
	public MDPTableReward (ProbNet id) throws Exception {
		super();

		this.id= id;
	}

	/**
	 * @see org.openmarkov.core.mdp.MDPReward#eval(org.openmarkov.core.mdp.MDPVariableConfig, org.openmarkov.core.mdp.MDPVariableConfig)
	 */
	public double eval (MDPVariableConfig configPriori, MDPVariableConfig configActions) {
		double reward= 0.0;

		// Calculo de la recompensa inmediate C(a)
		//
		for (Potential p : id.getPotentialsType(NodeType.UTILITY) ) {
			assert (p instanceof TablePotential);
			TablePotential potU= (TablePotential) p;
			
			int []decisionCoords= new int[potU.getNumVariables()];

			for (int r=0; r<potU.getNumVariables(); r++) {
				Variable x= potU.getVariable(r);
				assert (MDPEvaluation.isTemporal(x));
				int tempIndex= MDPEvaluation.getTemporalIndex(x);

				if (id.getProbNode(x).getNodeType() == NodeType.DECISION) {
					if (configPriori.variable.potEstado.contains(x)) {
						assert (tempIndex==0);
						decisionCoords[r]= configPriori.getStateValue(x);
					}
					else {
						assert (tempIndex==1);
						decisionCoords[r]= configActions.getStateValue(x);
					}
				}
				else if (id.getProbNode(x).getNodeType() == NodeType.CHANCE) {
					assert (tempIndex==1);
					String sPrev= MDPEvaluation.getTemporalName(x) + "_0";
					try {
						x= id.getVariable(sPrev);
					} catch (ProbNodeNotFoundException e) {
						e.printStackTrace();
					}
					
					decisionCoords[r]= configPriori.getStateValue(x);
				}
				else {
					throw new IllegalStateException();
				}
			}
			
			// Por ahora, potenciales son aditivos
			//
			int posUtility= potU.getPosition(decisionCoords);
			reward += potU.getValues()[posUtility];
		}

		return reward;		
	}
}
