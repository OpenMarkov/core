/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

public abstract class ICIPotential extends Potential {

	/* Model type may be OR, causal MAX, AND, etc. */
	protected ICIModelType modelType;
	
	/* ICI family will be MAX (which includes OR, causal MAX...), MIN, etc. */
	protected ICIFamily family;
	
	/* There will be a potential for each link, plus the leak potential */
	protected ArrayList<TablePotential> subPotentials;

	// Constructor
	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @param model. <code>ICIModel</code> */
	public ICIPotential(ICIModelType modelType, ArrayList<Variable> variables, 
			PotentialRole role) {
		// In principle, role will be "conditional probability"
		// and the first variable will be the conditioned variable
		super(variables, role);
		this.modelType = modelType;
		this.family = modelType.getFamily();
		subPotentials = new ArrayList<TablePotential>();
		
		for(int i = 1; i < variables.size (); ++i)
		{
            double [] probabilities = new double[variables.get(0).getNumStates () * variables.get(i).getNumStates ()];
            for(int j = 0; j < variables.get(i).getNumStates (); ++j)
            {
                for(int k = 0; k < variables.get(0).getNumStates (); ++k)
                {
                    probabilities[j* variables.get(0).getNumStates() + k] = (k == j)? 1.0: 0.0; 
                }
            }
            
            ArrayList<Variable> linkVariables = new ArrayList<Variable>();
            linkVariables.add(variables.get(0));
            linkVariables.add(variables.get(i));
            try
            {
                addSubPotential (new TablePotential(linkVariables, role));
            }
            catch (NotEnoughMemoryException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
		
	}

	// Methods
	public abstract TablePotential getCPT() throws NotEnoughMemoryException;

	@Override
    /** @param evidenceCase. <code>EvidenceCase</code>
     * @return <code>ArrayList</code> of <code>Potential</code>*/
    public ArrayList<TablePotential> tableProject (EvidenceCase evidenceCase,
                                                   InferenceOptions inferenceOptions)
        throws NonProjectablePotentialException,
        NotEnoughMemoryException,
        WrongCriterionException
    {
        ArrayList<TablePotential> projectedPotentials = new ArrayList<TablePotential> ();
        for (TablePotential subPotential : subPotentials)
        {
            projectedPotentials.add (subPotential.tableProject (evidenceCase, inferenceOptions).get (0));
        }
        return projectedPotentials;
    }
	
	
	/** @param potential. <code>Potential</code> */
	public void addSubPotential(TablePotential subPotential) {
		subPotentials.add(subPotential);
	}
	
	/** @return <code>ArrayList</code> of <code>TablePotential</code>. */
	public ArrayList<TablePotential> getSubPotentials() {
		return subPotentials;
	}
	
	/** @param variable. <code>Variable</code>
	 * @return The first subPotential that contains <code>variable</code>.
	 *  <code>TablePotential</code> or <code>null</code> */
	public TablePotential getSubPotential(Variable variable) {
		for (TablePotential potential : subPotentials) {
			if (potential.getVariables().contains(variable)) {
				return potential;
			}
		}
		return null;
	}
	
	/** @return Leak potential. <code>TablePotential</code> */
	public TablePotential getLeakPotential() {
		// The leak potential depends on only one variable
		int i = 0;
		while (i < subPotentials.size()
				&& subPotentials.get(i).getNumVariables() > 1)
			++i;
		return subPotentials.get(i);
	}

	/** @return model. <code>ICIModel</code> */
	public ICIModelType getModelType() {
		return modelType;
	}

	/** @return model. <code>ICIModel</code> */
	public ICIFamily getFamily() {
		return modelType.getFamily();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append("\nFamily: " + family + ". Model: " + modelType);
		buffer.append("\nNumber of subPotentials: " +  subPotentials.size());
		buffer.append("\nSubpotentials: ");
		for (Potential potential : subPotentials) {
			ArrayList<Variable> potentialVariables = potential.getVariables();
			buffer.append("[");
			int i;
			for (i = 0; i < potentialVariables.size() - 1; i++) {
				buffer.append(potentialVariables.get(i) + ", ");
			}
			buffer.append(potentialVariables.get(i) + "] ");
		}
		buffer.append("\n");
		return buffer.toString();
	}
	
	@Override
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO implement this function
		throw new Error("function shift is not implemented in ICIPotential");
	}

}
