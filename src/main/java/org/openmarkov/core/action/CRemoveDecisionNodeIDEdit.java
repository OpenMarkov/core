package org.openmarkov.core.action;

import java.awt.Choice;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;


@SuppressWarnings("serial")
/** Removes a utility node in an Influence Diagram performing this steps:<ol>
 * <li>Gets all the potentials that depends on <code>variableToDelete</code>.
 * <li>Gets all the utility potentials that depends on 
 *   <code>variableToDelete</code>. 
 * <li>Computes the new probability potential
 * <li>Computes the new utility potential
 * <li>Adds new potentials to the network
 * </ol> */
public class CRemoveDecisionNodeIDEdit extends CompoundPNEdit 
        implements UsesVariable {

	private Variable variableToDelete;
	
	private ArrayList<Potential> utilitiesHistory;
	
//TODO	private VarEliminationID varEliminationID;

	private HashMap<Variable, GTablePotential<Choice>> optimalStrategy;
	
	private Logger logger;
	
	public CRemoveDecisionNodeIDEdit(ProbNet probNet, 
			Variable variableToDelete, ArrayList<Potential> utilitiesHistory,
//TODO:			VarEliminationID varEliminationID,
			HashMap<Variable, GTablePotential<Choice>> optimalStrategy) {
		super(probNet);
		this.variableToDelete = variableToDelete;
		this.utilitiesHistory = utilitiesHistory;
//TODO:		this.varEliminationID = varEliminationID;
		this.optimalStrategy = optimalStrategy;
		this.logger= Logger.getLogger(CRemoveDecisionNodeIDEdit.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void generateEdits() {
		// Gets the utility potential that depends on variableToDelete. 
		ArrayList<Potential> probPotentials = 
			(ArrayList<Potential>)probNet.getProbPotentials(variableToDelete);

		ArrayList<Potential> utilityPotentials = (ArrayList<Potential>)
			probNet.getUtilityPotentials(variableToDelete);
		// keep utilities for explanation
		utilitiesHistory.addAll(utilityPotentials);
		// Remove the node from the graph
		ProbNode nodeToDelete = probNet.getProbNode(variableToDelete);
		probNet.removePotentials(probPotentials);
		probNet.removePotentials(utilityPotentials);
		probNet.removeProbNode(nodeToDelete);
		Potential utilityPotential = getUtilityPotential(utilityPotentials);
		
		try {
			Object[] chancePotential = DiscretePotentialOperations
			    .multiplyAndMaximize(probPotentials, variableToDelete);
			TablePotential newChancePotential = 
				(TablePotential)chancePotential[0];

			// computes the new utility
			if (utilityPotential != null) {
				Object[] utilityAndPolicyPotentials =DiscretePotentialOperations
					.maximize(utilityPotential, variableToDelete);
	
				TablePotential newUtility = 
					(TablePotential)utilityAndPolicyPotentials[0];
				optimalStrategy.put(variableToDelete, 
					(GTablePotential<Choice>)utilityAndPolicyPotentials[1]); 
				// adds the new potentials to the probNet
				if (newUtility.getTableSize() > 1) { 
					probNet.addPotential(newUtility);
					if (newChancePotential.getNumVariables() > 0) {
						probNet.addPotential(newChancePotential);
					}
				} else { // the end
//TODO:					varEliminationID.setMaxExpectedUtility(
//						newUtility.values[newUtility.getInitialPosition()]);
				}
			} else {
				if (newChancePotential.getNumVariables() > 0) {
					probNet.addPotential(newChancePotential);
				}				
			}
			
		} catch (NotEnoughMemoryException e) {
			logger.fatal(e);
		}
	}

	/** Adds <code>utilityPotentials</code> received.
	 * @param utilityPotentials <code>ArrayList</code> of 
	 *   <code>Potential</code>s.
	 * @return utilityPotential <code>Potential</code>. */
	private Potential getUtilityPotential(
			ArrayList<Potential> utilityPotentials) {
		Potential utilityPotential = null;
		int numUtilityPotentials = utilityPotentials.size();
		if (numUtilityPotentials == 0) {
			return null;
		}
		if (numUtilityPotentials == 1) {
			utilityPotential = utilityPotentials.get(0);
		} else {
			try {
				utilityPotential = 
					DiscretePotentialOperations.sum(utilityPotentials);
			} catch (NotEnoughMemoryException e) {
				logger.fatal(e);
			}
		}
		return utilityPotential;
	}

	/** @return variableToDelete <code>Variable</code> */
	public Variable getVariable() {
		return variableToDelete;
	}

	/** @return <code>String</code> */
	public String toString() {
		return new String("CRemoveDecisionNodeIDEdit: " + variableToDelete);
	}


}
