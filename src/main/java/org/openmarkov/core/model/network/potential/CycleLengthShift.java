/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
import java.util.Collection;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.RelationType;

/** Potential identical to another but moved to another temporal slice.
 * @author marias
 * @version 1.0 */
@RelationType(name = "CycleLengthShift", family = "")
public class CycleLengthShift extends Potential {

	// Constructor
	/** @param potential
	 * @param slice */
	public CycleLengthShift(ArrayList<Variable> variables) {
		super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
		type = PotentialType.CYCLE_LENGTH_SHIFT;
	}
	
    public CycleLengthShift(Potential potential) {
        this(potential.getVariables ());
    }
	
    /**
     * Returns if an instance of a certain Potential type makes sense given the variables and the potential role 
     * @param variables
     * @param role
     */
    public static boolean validate (ProbNode probNode, ArrayList<Variable> variables, PotentialRole role)
    {
        return role == PotentialRole.CONDITIONAL_PROBABILITY && variables.size () == 2
                // child = variables.get (0)
                // parent = variables.get (1)
               && variables.get (0).isTemporal () && variables.get (1).isTemporal ()
               && variables.get (0).getBaseName ().equals (variables.get (1).getBaseName ())
               && variables.get (0).getTimeSlice () == variables.get (1).getTimeSlice () + 1;
    }       

	// Methods
	@Override
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase, 
			InferenceOptions inferenceOptions)
			throws NonProjectablePotentialException, NotEnoughMemoryException {
		// TODO 
		for (Variable variable : variables) {
			if (!evidenceCase.contains(variable)) {
				throw new Error("Variable " + variable.getName() + 
				" is not included in EvidenceCase.");
			}
		}
		return new ArrayList<TablePotential>();
	}
	
	// TODO
	//@Override
	// public void extendEvidence(EvidenceCase)

	@Override
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
		Variable conditionedVariable = variables.get(0);
		Variable conditioningVariable = variables.get(1);
		ArrayList<Finding> inducedFindings = new ArrayList<Finding>();
		if (evidenceCase.contains(conditioningVariable) && 
				!evidenceCase.contains(conditionedVariable)) {
			// TODO Tener en cuenta que la duración de un ciclo puede
			// ser distinta de 1. Por ejemplo, si un ciclo dura 3 meses.
			double numericalValue = evidenceCase.getFinding(
						conditioningVariable).getNumericalValue() + 1;
			inducedFindings.add(new Finding(conditionedVariable, 
						numericalValue));
			
		}
		return inducedFindings;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeDifference) {
		return new CycleLengthShift(getShiftedVariables(probNet, timeDifference));
	}
	
    @Override
    public Potential copy ()
    {
        return new CycleLengthShift(new ArrayList<Variable> (variables));
    }	

}
