/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;

/** Potential associated to supervalue node to indicate that the utility is a
 * sum of the utilities of its parents.
 * @author mkpalacio
 * @version 1.0 */
@RelationPotentialType(name="Sum", family="Utility")
public class SumPotential extends Potential {

	// Constructor
	/**
	 * 
	 * @param variables
	 * @param role
	 * @param utilityVariable
	 */
	public SumPotential(List<Variable> variables, PotentialRole role, Variable utilityVariable) {
		super(variables, role, utilityVariable);
		type = PotentialType.SUM;
	}	
	/**
	 * @param variables
	 * @param parentsProbNodes
	 * @param role
	 */
	public SumPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
		type = PotentialType.SUM;
	}
	
    public SumPotential(SumPotential potential) {
        super(potential);
        type = PotentialType.SUM;
    }

	// Methods
    /** Returns if an instance of a certain Potential type makes sense given 
     * the variables and the potential role.
     * @param probNode. <code>ProbNode</code> 
     * @param variables. <code>ArrayList</code> of <code>Variable</code>.
     * @param role. <code>PotentialRole</code>. */
	public static boolean validate(ProbNode probNode, List<Variable> variables, PotentialRole role) {
		boolean suitable = (role == PotentialRole.CONDITIONAL_PROBABILITY
				|| role == PotentialRole.POLICY) && variables.get(0).getVariableType() == VariableType.NUMERIC;
				
        return suitable || role == PotentialRole.UTILITY;
    }
    
	@Override
	/** @return If none of the potential variables are included in the 
	 * <code>evidenceCase</code> variables returns itself, in other case, 
	 * returns a uniform potential with the potential variables minus the 
	 * <code>evidenceCase</code> variables.
	 * @param evidenceCase. <code>evidenceCase</code> */
    public List<TablePotential> tableProject (EvidenceCase evidenceCase,
                                              InferenceOptions inferenceOptions,
                                              List<TablePotential> projectedPotentials)
        throws NonProjectablePotentialException,
        WrongCriterionException
    {
/*		// TODO se puede simplificar proyectando cada potencial padre
		// dentro del bucle for. Asi se elimina el metodo getTableProjectedParentPotentials
		// Get potentials to be multiplied
		ArrayList<Potential> factorPotentials = 
				new ArrayList<Potential>(parentProbNodes.size());
		for (ProbNode parentProbNode : parentProbNodes) {
			factorPotentials.add(parentProbNode.getPotentials().get(0));
		}
		ArrayList<TablePotential> projectedFactorPotentials =
				getTableProjectedParentPotentials(
						factorPotentials, evidenceCase, inferenceOptions);
		TablePotential multiplication = 
				DiscretePotentialOperations.multiply(projectedFactorPotentials);
		if (multiplication.isUtility() && 
				multiplication.getUtilityVariable() == null) {
			multiplication.setUtilityVariable(utilityVariable);
		}
		return multiplication.tableProject(evidenceCase, inferenceOptions);
		*/
	       throw new NonProjectablePotentialException("Cannot project into tables a SumPotential");
	}

//	/**
//	 * @param parentPotentials
//	 * @param evidenceCase
//	 * @param inferenceOptions
//	 * @return
//	 * @throws NonProjectablePotentialException
//	 * @throws WrongCriterionException
//	 */
//    private List<TablePotential> getTableProjectedParentPotentials (List<Potential> parentPotentials,
//                                                                    EvidenceCase evidenceCase,
//                                                                    InferenceOptions inferenceOptions)
//        throws NonProjectablePotentialException,
//        WrongCriterionException
//    {
//        List<TablePotential> tableProjectedParentPotentials = new ArrayList<TablePotential> (
//                                                                                             parentPotentials.size ());
//        for (Potential potential : parentPotentials)
//        {
//            tableProjectedParentPotentials.addAll (potential.tableProject (evidenceCase,
//                                                                           inferenceOptions));
//        }
//        return tableProjectedParentPotentials;
//    }

	@Override
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase, double cycleLength)
			throws IncompatibleEvidenceException {
		return null;
	}

    @Override
    public Potential copy ()
    {
        return new SumPotential(this);
    }	

    public double getUtility (HashMap<Variable, Integer> sampledStateIndexes, HashMap<Variable, Double> utilities)
    {
    	double sum = 0.0;
    	for(Variable variable: getVariables())
    	{
    		sum+= utilities.get(variable);
    	}
        return sum;
    }	    
    
    public  Potential addVariable(Variable variable) {
    	variables.add(variable);
    	return this;
    }
    /**
     * Removes variable to a potential implemented in each child class
     * 
     */
    public  Potential removeVariable(Variable variable) {
    	variables.remove(variable);
    	return this;
    }
	@Override
	public boolean isUncertain() {
		return false;
	}    
}


