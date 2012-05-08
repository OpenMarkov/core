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

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.RelationType;

/** Potential associated to supervalue node to indicate that the utility is a
 * product of the utilities of its parents.
 * @author marias
 * @author mkpalacio
 * @version 1.0 */
@RelationType(name="Product", family="Utility")
public class ProductPotential extends Potential {

	// Constructor
	/**
	 * @param variables
	 * @param parentsProbNodes
	 * @param role
	 */
	public ProductPotential(ArrayList<Variable> variables, PotentialRole role) {
		super(variables, role);
		type = PotentialType.PRODUCT;
	}
	
    public ProductPotential(Potential potential) {
        this(potential.getVariables (), potential.getPotentialRole ());
    }	
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the variables and the potential role 
     * @param variables
     * @param role
     */
    public static boolean validate (ProbNode probNode, ArrayList<Variable> variables, PotentialRole role)
    {
        return probNode.getNodeType () == NodeType.UTILITY && isProductNode(probNode);
    }        
    

	// Methods
	@Override
	/** @return If none of the potential variables are included in the 
	 * <code>evidenceCase</code> variables returns itself, in other case, 
	 * returns a uniform potential with the potential variables minus the 
	 * <code>evidenceCase</code> variables.
	 * @param evidenceCase. <code>evidenceCase</code> */
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions)
	throws NonProjectablePotentialException, NotEnoughMemoryException, 
	WrongCriterionException {
	    /*
		// TODO se puede simplificar proyectando cada potencial padre
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

	    throw new NonProjectablePotentialException("Cannot project into tables a ProductPotential");
	}

	/**
	 * @param parentPotentials
	 * @param evidenceCase
	 * @param inferenceOptions
	 * @return
	 * @throws NotEnoughMemoryException
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	private ArrayList<TablePotential> getTableProjectedParentPotentials(
			ArrayList<Potential> parentPotentials, EvidenceCase evidenceCase,
			InferenceOptions inferenceOptions) 
	throws NotEnoughMemoryException, NonProjectablePotentialException, 
	WrongCriterionException {
		ArrayList<TablePotential> tableProjectedParentPotentials =
			new ArrayList<TablePotential>(parentPotentials.size());
		for (Potential potential : parentPotentials) {
			tableProjectedParentPotentials.addAll(
					potential.tableProject(evidenceCase, inferenceOptions));
		}
		return tableProjectedParentPotentials;
	}

	@Override
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase)
			throws IncompatibleEvidenceException, NotEnoughMemoryException {
		return null;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO Auto-generated method stub
		return null;
	}
	
    private static boolean isProductNode (ProbNode probNode)
    {
        // if some one of the parents are not utility node
        ArrayList<Node> parents = probNode.getNode ().getParents ();
        if (parents.size () > 0)
        {
            for (Node node : parents)
            {
                if (((ProbNode) node.getObject ()).getNodeType () != NodeType.UTILITY)
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    @Override
    public Potential copy ()
    {
        return new ProductPotential(new ArrayList<Variable> (variables), role);
    }	

}

