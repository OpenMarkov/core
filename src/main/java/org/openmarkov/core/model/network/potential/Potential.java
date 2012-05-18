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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/** @author marias
  * @author fjdiez 
  * @version 1.0
  * @since OpenMarkov 1.0 */
public abstract class Potential{

	// Constants
	/** Maximum size of a String used in toString() */
	protected final int maxLengthString = 150; 

	// Attributes
	/** <code>ArrayList</code> of <code>extends Variable</code>.
	 * @frozen */
    protected ArrayList<Variable> variables;
    
    /** @frozen */
    protected int numVariables;

    /** Utility variable associated to the <code>ProbNode</code> that contains
     *  this potential. */
    protected Variable utilityVariable;
       
    /** @frozen */
    protected PotentialType type;
    
    /** @frozen */
    protected PotentialRole role;
    
	/** This object contains all the information that the parser reads from 
	 *  disk that does not have a direct connection with the attributes stored
	 *  in the <code>Potential</code> object. */
	public HashMap<String, Object> properties;
	
	private String comment =  "";
	
    // Constructor
    /** @param variables. <code>ArrayList</code> of <code>Variable</code>.
     * @param role. <code>PotentialRole</code> */
    public Potential(ArrayList<Variable> variables, PotentialRole role) {
        if (variables != null) {
            numVariables = variables.size();        	
            this.variables = new ArrayList<Variable>(variables);
        } else {
        	numVariables = 0;
        	this.variables = new ArrayList<Variable>();
        }
        utilityVariable = null;
        properties = new HashMap<String, Object>();
    	this.role = role;
    }
    
    /** @param variables <code>ArrayList</code> of <code>Variable</code>.
     * @param role. <code>PotentialRole</code> 
     * @param utility. <code>Variable</code> */
    public Potential(ArrayList<Variable> variables, PotentialRole role, Variable utility) {
        if (variables != null) {
            numVariables = variables.size();        	
            this.variables = new ArrayList<Variable>(variables);
        } else {
        	numVariables = 0;
        	this.variables = new ArrayList<Variable>();
        }
        utilityVariable = utility;
        properties = new HashMap<String, Object>();
    	this.role = role;
    }

    // Methods
    /** Returns if an instance of a certain Potential type makes sense given 
     * the variables and the potential role.
     * @param probNode. <code>ProbNode</code> 
     * @param variables. <code>ArrayList</code> of <code>Variable</code>.
     * @param role. <code>PotentialRole</code>. */
	public static boolean validate(ProbNode probNode, ArrayList<Variable> variables, 
			PotentialRole role) {
        // Default implementation: always return true
        return true;
    }
	
    /** @param evidenceCase. <code>EvidenceCase</code> 
     * @return The conditional probability table of this potential given the evidence
	 * @throws NotEnoughMemoryException
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
    public TablePotential getCPT (EvidenceCase evidenceCase)
    		throws NotEnoughMemoryException, NonProjectablePotentialException, 
    		WrongCriterionException {
	    ArrayList<TablePotential> potentials = tableProject (evidenceCase, null);
	    HashSet<Variable> variablesToEliminate = new HashSet<Variable>();
	    
	    //Fill it with variables appearing in all potentials except this
	    for(TablePotential tablePotential: potentials) {
	        variablesToEliminate.addAll (tablePotential.getVariables ());
	    }
	    variablesToEliminate.removeAll (variables);
	    
	    return DiscretePotentialOperations.multiplyAndMarginalize (potentials, variables,
	                                                               new ArrayList<Variable>(variablesToEliminate));
	}
    
    /** The conditional probability table given by this potential
     * @return <code>TablePotential</code>
     * @throws NotEnoughMemoryException
     * @throws NonProjectablePotentialException
     * @throws WrongCriterionException */
    public TablePotential getCPT ()
    		throws NotEnoughMemoryException, NonProjectablePotentialException, WrongCriterionException {
        return getCPT (new EvidenceCase ());
    }
	
    /** Modifies the frozen variable role. This method exists to avoid some
     * problems with legacy code in DiscretePotentialOperations class and it
     * does not be used except in very special cases.
     * @param role. <code>PotentialRole</code> */
    public void setPotentialRole(PotentialRole role) {
    	this.role = role;
    }
    
	/** Checks if all the variables belongs to the type received. 
	 * The utility variable is not considered.
	 * @param type. <code>VariableType</code> 
	 * @return <code>boolean</code> */
	protected boolean allVariablesBelongToType(VariableType type) {
		if (variables != null) {
			for (Variable variable : variables) {
				if (variable.getVariableType() != type) {
					return false;
				}
			}
		}
		return true;
	}

	/** @consultation
	 * @return An <code>ArrayList</code> of <code>Variable</code>s */
	@SuppressWarnings("unchecked")
	public ArrayList<Variable> getVariables() {
		return (ArrayList<Variable>)variables.clone();
	}
	
	/** @consultation
	 * @return The variable in the place <code>position</code> */
	public Variable getVariable(int position) {
		return variables.get(position);
	}

    public void replaceVariable(Variable variableToReplace, Variable variable) {
    	replaceVariable(variables.indexOf(variableToReplace), variable);
    }
    
    public void replaceVariable(int position, Variable variable) {
        variables.remove(position);
        variables.add (position, variable);
    }	

	/** @return <code>true</code> if contains the received 
	 * <code>Variable</code>.
	 * @param variable <code>Variable</code> */
	public boolean contains(Variable variable) {
		return variables.contains(variable);
	}
	
	// TODO documentar
	/**
	 * @param evidenceCase <code>EvidenceCase</code> 
	 * @param inferenceOptions TODO
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NoFindingException */
	public abstract ArrayList<TablePotential> tableProject(
			EvidenceCase evidenceCase, InferenceOptions inferenceOptions) 
		throws NonProjectablePotentialException, NotEnoughMemoryException, 
		WrongCriterionException;
	
    /** @return isUtility <code>boolean</code> */
    public boolean isUtility() {
        return role == PotentialRole.UTILITY; 
    }

	/** @return number of variables: <code>int</code> */
	public int getNumVariables() {
		return numVariables;
	}  

	/** @return utilityVariable. <code>Variable</code> */
	public Variable getUtilityVariable() {
		return utilityVariable;
	}
	
	/** @return <code>PotentialType</code> */
	public PotentialType getPotentialType() {
		return type;
	}

	/** @param utilityVariable. <code>Variable</code> */
	public void setUtilityVariable(Variable utilityVariable) {
		this.utilityVariable = utilityVariable;
		if (utilityVariable != null) {
			role = PotentialRole.UTILITY;
		}
	}
    
	/** Generates new <code>Finding</code>s generated by an  
	 * <code>EvidenceCase</code>. In principle this method does not generate
	 * any new finding, but it is overridden in some of its subclasses.
	 * @param evidenceCase. <code>EvidenceCase</code> 
	 * @return <code>Collection</code> of <code>Finding</code>s
	 * @throws IncompatibleEvidenceException 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException */
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) 
	throws IncompatibleEvidenceException, NotEnoughMemoryException, 
	WrongCriterionException {
		return new ArrayList<Finding>();
	}

    /** @return role. <code>PotentialRole</code> */
    public PotentialRole getPotentialRole() {
    	return role;
    }

    /** @param comment. <code>String</code> */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/** @return comment. <code>String</code> */
	public String getComment() {
		return comment;
	}

	/** By default, it returns "this". This is OK for potentials
	* that do not depend on temporal variables.
	* Subclasses of Potential must override this method.
	* @return <code>Potential</code>
	* @param timeDifference. <code>int</code>
	* @param probNet This parameter is necessary because the shifted variables
	* are taken from the network. <code>ProbNet</code> 
	 * @throws ProbNodeNotFoundException 
	 * @throws NotEnoughMemoryException */
	public abstract Potential shift(ProbNet probNet, int timeDifference) 
	throws ProbNodeNotFoundException, NotEnoughMemoryException;

	/**
	 * Creates links between the variables of a potential
	 * @argCondition The role of the potential must be utility of conditional
	 * probability
	 * @argCondition The network must contain all the variables of the potential
	 */
	public void createDirectedLinks(ProbNet probNet) {
		Variable childVariable;
		int firstParentIndex;
		if ( isUtility() ) {
			childVariable = utilityVariable;
			firstParentIndex = 0;
		} else {
			childVariable = variables.get(0);
			firstParentIndex = 1;
		}
		for (int parentIndex = firstParentIndex; parentIndex < variables.size();
				parentIndex++) {
			try {
				probNet.addLink(variables.get(parentIndex), childVariable, true);
			} catch (NodeNotFoundException e) {
				// Unreachable code
			}
		}
		
	}

	/** 
	 * Returns a list with the same variables as this potential, including the
	 * utility variable but shifted in
	 * time as indicated by timeDifference
	 * @argCondition The network must contain the shifted variables.
	 */
	public ArrayList<Variable> getShiftedVariables(ProbNet probNet, 
			int timeDifference) {
		ArrayList<Variable> shiftedVariables = new ArrayList<Variable>();
		for (Variable variable : getVariables()) {
			if ( variable.isTemporal() ){
				shiftedVariables.add(
					probNet.getShiftedVariable(variable, timeDifference));
			} else {
				shiftedVariables.add(variable);
			} 
		}
		return shiftedVariables;
	}

	/** Overrides <code>toString</code> method. Mainly for test purposes */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (numVariables == 0) { // Constant potential
			switch (role) {
			case UTILITY:
				buffer.append(utilityVariable.getName());
				break;
			case CONDITIONAL_PROBABILITY:
				break;
			case JOINT_PROBABILITY:
				break;
			}
		} else {
			switch (role) {
			case CONDITIONAL_PROBABILITY:
				buffer.append("P(" + variables.get(0));
				if (numVariables > 1) {
					buffer.append(" | ");
					printVariables(buffer, 1);
				}
				buffer.append(")");
				break;
			case UTILITY:
				buffer.append("U(" + utilityVariable);
				if (numVariables > 0) {
					buffer.append(" | ");
				}
				printVariables(buffer, 0);
				buffer.append(")");
				break;
			case JOINT_PROBABILITY:
				buffer.append("P(");
				printVariables(buffer, 0);
				buffer.append(")");
				break;
			default:
				buffer.append(numVariables + " Variables: "); 
				if (numVariables > 0) {
					buffer.append(variables.get(0).getName());
					for (int i = 1; i < numVariables - 1; i++) {
						buffer.append(", " + variables.get(i).getName());
					}
					if (numVariables > 1) {
						buffer.append(", " + 
								variables.get(numVariables - 1).getName());
					}
				}
			}
		}
		return buffer.toString();
	}
		
	/** Prints in buffer the variables and in case of TablePotential the
	 * configurations */
	private StringBuffer printVariables(StringBuffer buffer, 
			int firstVariable) {
		// Print variables
		for (int i = firstVariable; i < numVariables - 1; i++) {
			buffer.append(variables.get(i) + ", ");
		}
		buffer.append(variables.get(numVariables - 1));
		return buffer;
	}

	public String treeADDString() {
		return toString();
	}

	/** @throws NotEnoughMemoryException 
	 * @returns a sampled potential. By default, itself, i.e., not sampled. */
	public Potential sample(Variable simulationIndexVariable) 
			throws NotEnoughMemoryException {
		return this; // By default
	}


    @Override
    public boolean equals (Object arg0)
    {
        if(arg0.getClass ().equals (this.getClass ()))
        {
            Potential potential = (Potential) arg0;
            return variables.equals (potential.getVariables ())
                   && type == potential.getPotentialType ()
                   && role == potential.getPotentialRole ();
        }else
        {
            return false;
        }
    }
    public Integer sample (Random randomGenerator, HashMap<Variable, Integer> sampledStateIndexes)
    {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * Return a copy instance of the potential
     * @return potential copy
     */
    public abstract Potential copy()  throws NotEnoughMemoryException;	
    
    /**
     * Adds variable to a potential implemented in each child class
     * @throws NotEnoughMemoryException 
     * 
     */
    public  Potential addVariable(Variable variable) throws NotEnoughMemoryException {
    	return null;
    }
    /**
     * Removes variable to a potential implemented in each child class
     * @throws NotEnoughMemoryException 
     * 
     */
    public  Potential removeVariable(Variable variable) throws NotEnoughMemoryException {
    	return null;
    }

    public double getProbability (HashMap<Variable, Integer> sampledStateIndexes)
    {
        // TODO Auto-generated method stub
        return 0;
    }	
    
    public double getUtility (HashMap<Variable, Integer> sampledStateIndexes, HashMap<Variable, Double> utilities)
    {
        // TODO Auto-generated method stub
        return 0;
    }	    
    
    protected static ArrayList<Variable> toArrayList (Variable[] variables)
    {
        ArrayList<Variable> variablesArrayList = new ArrayList<Variable> ();
        for(Variable variable: variables)
        {
            variablesArrayList.add (variable);
        }
        return variablesArrayList;
    }    
	
}
