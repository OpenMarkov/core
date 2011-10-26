package org.openmarkov.core.action;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;


@SuppressWarnings("serial")
/** Removes a chance node in an Influence Diagram performing this steps:<ol>
 * <li>Gets all the potentials that depends on <code>variableToDelete</code>.
 * <li>Gets all the utility potentials that depends on 
 *   <code>variableToDelete</code>. 
 * <li>Computes the new probability potential
 * <li>Computes the new utility potential
 * <li>Adds new potentials to the network
 * </ol> */
public class CRemoveChanceNodeIDEdit extends CompoundPNEdit 
        implements UsesVariable {

	private Variable variableToDelete;
	
	private VarEliminationID varEliminationID;

	private double maxExpectedUtility;
	
	private Logger logger;
	
	public CRemoveChanceNodeIDEdit(ProbNet probNet, 
			Variable variableToDelete, VarEliminationID varEliminationID) {
		super(probNet);
		this.variableToDelete = variableToDelete;
		this.varEliminationID = varEliminationID;
		this.logger = Logger.getLogger(CRemoveChanceNodeIDEdit.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void generateEdits() throws NotEnoughMemoryException {
		// Gets all the potentials that depend on variableToDelete. 
		ArrayList<Potential> probPotentials = (ArrayList<Potential>)
			probNet.getProbPotentials(variableToDelete);
		ArrayList<Potential> utilityPotentials = (ArrayList<Potential>)
			probNet.getUtilityPotentials(variableToDelete);
		Potential utilityPotential = 
			calculateUtilityPotential(utilityPotentials);

		// Remove the variable from the graph:
		// 1. Remove chance potentials
		ProbNode nodeToDelete = probNet.getProbNode(variableToDelete);
		RemoveSeveralPotentialsEdit removeSeveralPotentials = 
			new RemoveSeveralPotentialsEdit(probNet, probPotentials);
		// 2. Remove utility potentials
		removeSeveralPotentials.addPotentials(utilityPotentials);
		edits.add(removeSeveralPotentials);
		// 3. Remove variable
		edits.add(new RemoveNodeEdit(probNet, nodeToDelete.getVariable()));

		try {
			// computes the new probability potentials
			TablePotential joinProb = 
				DiscretePotentialOperations.multiply(probPotentials);

			// marginalize
			ArrayList<Variable> variablesToKeep = 
				(ArrayList<Variable>)joinProb.getVariables().clone();
			variablesToKeep.remove(variableToDelete);
			ArrayList<Variable> variablesToEliminate =new ArrayList<Variable>();
			variablesToEliminate.add(variableToDelete);
			ArrayList<Potential> joinProbPotentials =new ArrayList<Potential>();
			joinProbPotentials.add(joinProb);

			TablePotential marginalizedPotential = 
				(TablePotential) DiscretePotentialOperations
				.multiplyAndMarginalize(
					joinProbPotentials, variablesToKeep, variablesToEliminate);

			TablePotential normalizedProb =
				(TablePotential) DiscretePotentialOperations
				.divide(joinProb, marginalizedPotential);
			
			// computes the new utility
			if (utilityPotential != null) {
				ArrayList<Potential> probAndUtility =new ArrayList<Potential>();
				//probAndUtility.add(marginalizedPotential); // normalizedProb?
				probAndUtility.add(normalizedProb); 
				probAndUtility.add(utilityPotential);
				TablePotential newUtility =
					(TablePotential) DiscretePotentialOperations
					.multiplyAndMarginalize(probAndUtility, variableToDelete);

				// adds the new potentials to the network
				if (newUtility.getTableSize() > 1) {
					newUtility.setUtilityVariable(newUtility.getVariable(0));
					if (marginalizedPotential.getTableSize() > 1){
						edits.add(new AddPotentialEdit(
								probNet, marginalizedPotential));
					}
					edits.add(new AddPotentialEdit(probNet, newUtility));
				} else { // the end
					maxExpectedUtility = 
						newUtility.values[newUtility.getInitialPosition()];
					varEliminationID.setMaxExpectedUtility(maxExpectedUtility);
				}
			} else {
				edits.add(new AddPotentialEdit(probNet, marginalizedPotential));
				if (marginalizedPotential.getTableSize() == 1) {
					maxExpectedUtility = marginalizedPotential.values[0];
					varEliminationID.setMaxExpectedUtility(maxExpectedUtility);
				}
			}

		} catch (NotEnoughMemoryException e) {
			logger.fatal(e);
		}
	}

	/** Adds <code>utilityPotentials</code> received.
	 * @param utilityPotentials <code>ArrayList</code> of 
	 *   <code>Potential</code>s.
	 * @return utilityPotential <code>Potential</code>. 
	 * @throws NotEnoughMemoryException */
	private Potential calculateUtilityPotential(
			ArrayList<Potential> utilityPotentials) throws NotEnoughMemoryException {
		Potential utilityPotential = null;
		int numUtilityPotentials = utilityPotentials.size();
		if (numUtilityPotentials > 0) {
			if (numUtilityPotentials == 1) {
				utilityPotential = utilityPotentials.get(0);
			} else {
				utilityPotential = 
					DiscretePotentialOperations.sum(utilityPotentials);
			}
		}
		return utilityPotential;
	}

	public Variable getVariable() {
		return variableToDelete;
	}
	
	/** @return <code>String</code> */
	public String toString() {
		return new String("CRemoveNodeChanceNodeIDEdit: " +	variableToDelete);
	}


}
