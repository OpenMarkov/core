package org.openmarkov.core.mdp;

import org.openmarkov.core.model.network.ProbNet;

public class ModifiedPolicyIterationWithJumps extends ModifiedPolicyIteration {
	
	int itersSinJump;
	int iEstadoJump;
	boolean bInitJumpVbles;

	double []dif_01;
	MDPTableValueFunction jump_aprox;
	MDPTableValueFunction jump_next;
	MDPParamsMPI paramsMPI;
	boolean asynthoticJumpPossible;
	
	public ModifiedPolicyIterationWithJumps(ProbNet id) throws Exception {
		super(id);
		bInitJumpVbles= false;
	}

	/**
	 * @see openmarkov.jorge.MDPAlgorithm#run(openmarkov.jorge.MDPParams)
	 */
	@Override
	public void run(MDPParams obj) throws Exception {
		// Necesario para los backups dentro de isPartialEvaluationFinished
		paramsMPI= (MDPParamsMPI) obj;
		
		super.run(obj);
	}
	
	/**
	 * 
	 */
	protected void initPartialPolicyEvaluation () {
		super.initPartialPolicyEvaluation();
		
		itersSinJump= 0;
		iEstadoJump= 0;
		asynthoticJumpPossible= false;
		
		if (!bInitJumpVbles) {
			bInitJumpVbles= true;
			
			dif_01= new double[valueFunction.values.length];
			jump_aprox= new MDPTableValueFunction(mdpVariablePriori);
			jump_next= new MDPTableValueFunction(mdpVariablePriori);			
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	protected boolean isPartialPolicyEvaluationFinished () throws Exception {
		if (asynthoticJumpPossible) {
			return true;
		}
		
		return super.isPartialPolicyEvaluationFinished();
	}

	/**
	 * @throws Exception 
	 * 
	 */
	protected void nextPartialPolicyIteration () throws Exception {
		asynthoticJumpPossible= isAsynthoticJumpPossible();
		
		super.nextPartialPolicyIteration();
	}
	
	protected boolean isAsynthoticJumpPossible() throws Exception {
		if (iEstadoJump==0) {
			if (itersSinJump>=15) {
				itersSinJump= 0;
				iEstadoJump= 1;
				
				for (int i=0; i< dif_01.length; i++) {
					dif_01[i]= valueFunction.values[i]-newValueFunction.values[i];
				}
			}
			else {
				itersSinJump++;
			}
		}
		else if (iEstadoJump==1) {
			iEstadoJump= 0;
			
			for (int i=0; i< dif_01.length; i++) {
				double k= valueFunction.values[i]-newValueFunction.values[i];
				jump_aprox.values[i]= k*k/(dif_01[i]-k)+valueFunction.values[i];
			}

			MDPVariableConfig configPriori= mdpVariablePriori.config_iterator();
			
			while (configPriori.hasNext() ) {
				MDPVariableConfig policy_act= myPolicy.getPolicy (configPriori);
				double reward= bellmanUpdate (configPriori, policy_act, paramsMPI, jump_aprox);
				jump_next.setValue (configPriori, reward);
				
				configPriori.next();
			}

			// Se recalcula el valor de sigma, utilizado como criterio de convergencia
			double dSigmaAnt= valueFunction.calculateNormMax(newValueFunction);
			double dSigmaJump= jump_aprox.calculateNormMax(jump_next);
			
			if (dSigmaJump<dSigmaAnt) {
				MDPTableValueFunction temp= newValueFunction;
				newValueFunction= jump_aprox;
				jump_aprox= temp;
				
				temp= valueFunction;
				valueFunction= jump_next;
				jump_next= temp;
				
				// Salimos de la eval.parcial (el salto nos lanza varias iter adelante, es muy probable que sea superfluo seguir evaluando esta politica
				return true;
			}
		}
		
		return false;
	}
}
