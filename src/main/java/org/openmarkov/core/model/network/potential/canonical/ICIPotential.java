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
import java.util.Collection;
import java.util.HashMap;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

public abstract class ICIPotential extends Potential {

	/* Model type may be OR, causal MAX, AND, etc. */
	protected ICIModelType modelType;
	
	/* ICI family will be MAX (which includes OR, causal MAX...), MIN, etc. */
	protected ICIFamily family;
	
    /**
     * List of Z variables we are going to use in the canonical model
     */
    private HashMap<Variable, Variable> zVariables;
    
	/**
	 * Noisy parameters for the canonical model
	 */
	private HashMap<Variable, double[]> noisyParameters;
	
    /**
     * Leak parameters for the canonical model
     */
    private double[] leakyParameters;	
    
    private Variable leakyVariable = null;

	// Constructor
	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @param model. <code>ICIModel</code> */
	public ICIPotential(ICIModelType modelType, ArrayList<Variable> variables) {
		// In principle, role will be "conditional probability"
		// and the first variable will be the conditioned variable
		super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
		this.modelType = modelType;
		this.family = modelType.getFamily();
		this.noisyParameters = getDefaultNoisyParameters();
		this.leakyParameters = getDefaultLeakyParameters (variables.get (0).getNumStates ());
		
        zVariables = new HashMap<Variable, Variable> ();
        for(int i=1; i<variables.size (); ++i)
        {
            zVariables.put (variables.get (i), new Variable ("z" + variables.get (i).getName (),
                                                             variables.get (0).getStates ()));
        }
        
        leakyVariable = new Variable (variables.get (0).getName () + "-leaky", variables.get (0).getStates ());
	}

    public HashMap<Variable, double[]> getDefaultNoisyParameters()
    {
        HashMap<Variable, double[]> noisyParameters = new HashMap<Variable, double[]> ();
        Variable conditionedVariable = variables.get (0); 
        
        for (int i = 1; i < variables.size (); ++i)
        {
            Variable parent = variables.get (i); 
            double[] probabilities = new double[conditionedVariable.getNumStates () * parent.getNumStates ()];
            for (int j = 0; j < parent.getNumStates (); ++j)
            {
                for (int k = 0; k < conditionedVariable.getNumStates (); ++k)
                {
                    probabilities[j * conditionedVariable.getNumStates () + k] = (k == j) ? 1.0 : 0.0;
                }
            }
            noisyParameters.put (parent, probabilities);
        }
        return noisyParameters;
    }
	
    /**
     * Returns if an instance of a certain Potential type makes sense given the variables and the potential role 
     * @param variables
     * @param role
     */
    public static boolean validate (ProbNode probNode, ArrayList<Variable> variables, PotentialRole role)
    {
        return variables.size () > 1;
    }   
    
	public abstract double[] getDefaultLeakyParameters(int numStates);
	
	
//	{
//		ArrayList<Variable> leakyVariables = new ArrayList<Variable>();
//		leakyVariables.add(variables.get(0));
//		TablePotential tablePotential = new TablePotential(leakyVariables, PotentialRole.CONDITIONAL_PROBABILITY);
//		double[] leakyParameters = new double[variables.get(0).getNumStates()];
//		leakyParameters[0] = 1.0;
//		for(int i=1; i<leakyParameters.length; ++i)
//		{
//			leakyParameters[0] = 0.0;
//		}
//		tablePotential.values = leakyParameters;
//		return tablePotential;
//	}
	
	// Methods
    /**
     * @return The conditional probability table given by this potential
     */
    public TablePotential getCPT () throws NotEnoughMemoryException
    {
        ArrayList<Variable> variablesToEliminate = new ArrayList<Variable> (zVariables.values ());
        variablesToEliminate.add (leakyVariable);
        // Eliminate zVariables through marginalization
        return DiscretePotentialOperations.multiplyAndMarginalize (buildSubpotentialList(), variables,
                                                                   new ArrayList<Variable>(variablesToEliminate));
    }
    
	protected abstract TablePotential getFFunctionPotential ()  throws NotEnoughMemoryException;

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
        for (TablePotential subPotential : buildSubpotentialList())
        {
            projectedPotentials.add (subPotential.tableProject (evidenceCase, null).get (0));
        }
        return projectedPotentials;
    }
	
    public double[] getNoisyParameters(Variable variable)
    {
        return noisyParameters.get(variable);
    }	
	/**
	 * Sets the noisy parameters, i.e. <i>P(z<sub>i</sub>|x<sub>i</sub>)</i>
	 * @param parent parent variable (<i>X<sub>i</sub></i>) whose noisy parameters we want to set 
	 * @param parameters the noisy parameters. The length of the array must be the multiplication of the parent's and child's state number
	 */
	public void setNoisyParameters(Variable parent, double[] parameters)
	{
	    if(parameters.length != variables.get (0).getNumStates () * parent.getNumStates ())
	    {
            throw new IllegalArgumentException (
                                                "The length of the array must be the multiplication"
                                                        + " of the parent's and child's state number "
                                                        + variables.get (0).getNumStates ()
                                                        * parent.getNumStates () + " and is "
                                                        + parameters.length);
	    }
        if (!getVariables ().contains (parent))
        {
            throw new IllegalArgumentException("There is no variable " + parent + " in this ICI family.");
        }
        
        noisyParameters.put (parent, parameters);
	}
	
	/**
	 * There will be a potential for each link, plus the leak potential 
	 * @return <code>ArrayList</code> of <code>TablePotential</code>. 
	 * @throws NotEnoughMemoryException 
	 * */
	protected ArrayList<TablePotential> buildSubpotentialList() throws NotEnoughMemoryException {
	    ArrayList<TablePotential> subpotentials = new ArrayList<TablePotential> ();

	    // F function
	    subpotentials.add (getFFunctionPotential ());

	    //Noisy parents
	    for(Variable parent: zVariables.keySet ())
	    {
	        ArrayList<Variable> linkVariables = new ArrayList<Variable> ();
	        linkVariables.add(zVariables.get (parent)); // Z variable
	        linkVariables.add(parent);
	        
	        subpotentials.add (new TablePotential(linkVariables, PotentialRole.CONDITIONAL_PROBABILITY, noisyParameters.get(parent)));
	    }

	    // Leak parent
	    if(this.leakyParameters != null)
	    {
            ArrayList<Variable> leakVariables = new ArrayList<Variable> ();
            leakVariables.add(leakyVariable); // conditioned variable
            subpotentials.add (new TablePotential(leakVariables, PotentialRole.CONDITIONAL_PROBABILITY, leakyParameters));
	    }
	        
	     return subpotentials;
	}
	
	/** @return Leak potential. <code>TablePotential</code> */
	public double[] getLeakyParameters() {
		return this.leakyParameters;
	}
	
	/**
	 * Sets Leak parameters
	 * @param leakyParameters
	 */
    public void setLeakyParameters(double[] leakyParameters) {
        if(leakyParameters.length != variables.get (0).getNumStates () )
        {
            throw new IllegalArgumentException (
                                                "The length of the array must be the conditioned variable's state number "
                                                        + variables.get (0).getNumStates ()
                                                        + " and is " + leakyParameters.length);
        }
        
        this.leakyParameters = leakyParameters;
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
		buffer.append("\nNumber of variables: " +  variables.size());
		buffer.append("\nVariables: ");
        buffer.append ("[");
        for (int i = 0; i < variables.size () - 1; i++)
        {
            buffer.append (variables.get (i) + ", ");
        }
        buffer.append (variables.get (variables.size () - 1) + "] ");
		buffer.append("\n");
		return buffer.toString();
	}
	
	@Override
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO implement this function
		throw new Error("function shift is not implemented in ICIPotential");
	}
	
	/**
	 * 
	 * @return collection of Z variables
	 */
	protected Collection<Variable> getAuxiliaryVariables()
	{
	    return zVariables.values ();
	}

}
