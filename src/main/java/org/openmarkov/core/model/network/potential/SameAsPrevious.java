package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/** @author marias
 * @version 1.0 */
public class SameAsPrevious extends Potential {

	// Attributes
	protected Potential originalPotential;
	
	protected int timeDifference;
	
	protected ProbNet probNet;

	// Static methods
	/** Looks for a potential with  
	 * @param probNet. <code>ProbNet</code>
	 * @param variable. <code>Variable</code> 
	 * @return Potential The potential referred to by this one
	 * @throws NodeNotFoundException 
	 * @argCondition probNet must be a Markov Net with order = 1 because 
	 * otherwise the potential returned could not be the right one
	 @precondition The previousProbNode must have at least
	 one potential assigned */
	public static Potential getPotential(ProbNet probNet, Variable variable) 
	throws NodeNotFoundException {
		String simpleName = variable.getName();
		Potential previousPotential = null;
		int indexC = simpleName.lastIndexOf(" [");
		if (indexC != -1) {
			// For each variable in probNet...
			ArrayList<Variable> variables = probNet.getVariables();
			simpleName = simpleName.substring(0, indexC);
			String simpleNameExtended = new String(simpleName + " [");
			// ... looks for a variable that starts with variable.getName()+" ["
			for (Variable probNetVariable : variables) {
				if (probNetVariable.getName().startsWith(simpleNameExtended)) {
					// ...then get its potentials 
					ProbNode probNode = probNet.getProbNode(probNetVariable);
					ArrayList<Potential> potentialsNode = 
						probNode.getPotentials();
					// Assumption: all the variables have only one 
					// potential P(C|P1,P2,...,Pn)
					for (Potential potential : potentialsNode) {
						// finally, ensures that the potential is not another
						// SAME_AS_PREVIOUS
						if (potential.getPotentialType() != 
								PotentialType.SAME_AS_PREVIOUS) {
							previousPotential = potential;
							break;
						}
					}
					if (previousPotential != null) {
						break;
					}
				}
			}
			if (previousPotential == null) {// There is no previous variable
				throw new NodeNotFoundException("It does not exists a " +
						"previous variable called: " + 
						variable.getName() + " in this probNet");
			}
		} else {
			throw new NodeNotFoundException("Variable has no a temporal " +
					"type name: varName[number].");
		}
		return previousPotential;
	}

	// Constructors
	/** Creates a potential linked to the original potential
	 * @param originalPotential
	 * @param timeDifference
	 * @param probNet The net from which the variables will be taken
	 * @argCondition The network must contain the shifted variables
	 */
	public SameAsPrevious(Potential originalPotential, int timeDifference,
			ProbNet probNet) {
		super(originalPotential.getShiftedVariables(probNet, timeDifference), 
				originalPotential.getPotentialRole());
		this.originalPotential = originalPotential;
		this.probNet = probNet;
		this.timeDifference = timeDifference;
		if ( isUtility() ) {
			Variable originalUtilityVariable = 
				originalPotential.getUtilityVariable();
			utilityVariable = probNet.getShiftedVariable(
					originalUtilityVariable, timeDifference);	
		}
		type = PotentialType.SAME_AS_PREVIOUS;
	}

	// Methods
	@Override
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions)
	throws NonProjectablePotentialException, NotEnoughMemoryException,
	WrongCriterionException {
		
		// takes the evidence to the past
		EvidenceCase shiftedEvidence = 
			evidenceCase.shiftEvidenceBackwards(timeDifference, 
					inferenceOptions.probNet); 
		
		// projects the original potential according to the shifted evidence
		ArrayList<TablePotential> projectedPotentials = 
			originalPotential.tableProject(shiftedEvidence, inferenceOptions);
		
		// creates a copy of the projected potentials shifted to the future
		ArrayList<TablePotential> shiftedProjectedPotentials =
			new ArrayList<TablePotential>();
		for (TablePotential projectedPotential : projectedPotentials) {
			ArrayList<Variable> shiftedVariables = new ArrayList<Variable>();
			// creates a list of shifted variables
			for (Variable variable : projectedPotential.variables) {
				if (variable.isTemporal()) {
					shiftedVariables.add(probNet.getShiftedVariable(variable, 
							timeDifference));
				} else {
					shiftedVariables.add(variable);
				}
			}
			TablePotential shiftedPotential = 
				new TablePotential(shiftedVariables, role);
			shiftedPotential.utilityVariable = 
				projectedPotential.utilityVariable;
//			shiftedPotential.values = projectedPotential.values;

			// Set discount rate
			double accumulatedDiscount = 1.0;
			for (int i = 0; i < timeDifference; i++) {
				accumulatedDiscount *= inferenceOptions.discountRate;
			}
			int numValues = projectedPotential.values.length;
			double[] shiftedValues = new double[numValues];
			for (int i = 0; i < numValues; i++) {
				shiftedValues[i] = 
						shiftedPotential.values[i] * accumulatedDiscount; 
			}
			shiftedPotential.values = shiftedValues;
					
			shiftedProjectedPotentials.add(shiftedPotential);
		}
		return shiftedProjectedPotentials;
	}
	
	public Potential getOriginalPotential() {
		return originalPotential;
	}

	//TODO implementar
	/* @Override
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase)
			throws IncompatibleEvidenceException {
		return null;
	}
	*/

	@Override
	// TODO Quitar error
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		throw new Error("We have invoked SameAsPrevious.shift()");
	}
	
}
