
package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Intervention;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * <code>TreeADDBranch</code> represents branch of a treeADD. If the top variable of the
 * treeADD is numeric a branch is defined by two thresholds: a minimum and a
 * maximum. If top variable is finite states, then each branch is defined
 * by its states. In both cases each branch has a potential assigned. If the
 * branch is a leaf, its potential is any kind of potential except a
 * <code>TreeADDPotential</code>
 * @author myebra
 */
public class TreeADDBranch {
    /**
     * Each TreeADDBranch has an associated potential 
     */
    private Potential      potential = null;
    
    private Variable       rootVariable;

    /**
     * If the topVariable of the tree is a finite states or a discretized
     * variable each branch has an associated state.
     */
    private List<State>    states;
    
    /**
     * If the topVariable of the tree is a continuous variable it is defined in
     * a continuous interval which has two thresholds.
     */
    private Threshold      lowerBound;
    private Threshold      upperBound;
    
    /** If the top variable is continuous, there is an interval and this variable is true. */
    private boolean intervalBranch;

    /** If the top variable is discrete, the branch is associated to one or more states and this variable is true. */
    private boolean statesBranch;

    /** If this treeADD is embedded in a larger treeADD and role is not INTERVENTION, then parentVariables 
     * is the set of variables of that treeADD.
     * This attribute is used when building a TreeADD at the GUI.
     * This attribute is not necessary for interventions. */
    private List<Variable> parentVariables;

    /**
     * A branch can be labeled, labels are used to reference potential from
     * other branches when that potential has more than one parents
     */
    private String label;
    /**
     * A branch can reference a potential from other branch that has been
     * labeled
     */
    private String reference = null;
    private TreeADDBranch referencedBranch = null;
    
    private String indent = "    ";
    
    public void setIndent(String indent) {
		this.indent = indent;
	}

	/**
     * Constructor for discretized and finite states variables
     * @param branchStates
     * @param topVariable
     * @param parentVariables
     */
    public TreeADDBranch(List<State> branchStates, Variable topVariable, 
            List<Variable> parentVariables) {
        this.states = branchStates;
        this.rootVariable = topVariable;
        this.parentVariables = parentVariables;
        this.potential = null;
        this.statesBranch = true;
        this.intervalBranch = false;
    }

    /**
     * Constructor for discretized and finite states variables
     * @param branchStates
     * @param potential
     * @param topVariable
     * @param parentVariables
     */
    public TreeADDBranch(List<State> branchStates, Variable topVariable, Potential potential,
            List<Variable> parentVariables) {
    	this(branchStates, topVariable, parentVariables);
        this.potential = potential;
        this.statesBranch = true;
        this.intervalBranch = false;
    }

    /**
     * Constructor for numeric variables
     * @param lowerThreshold
     * @param upperThreshold
     * @param topVariable
     * @param potential
     * @param parentVariables
     */
    public TreeADDBranch (Threshold lowerBound,
                          Threshold upperBound,
                          Variable topVariable,
                          Potential potential,
                          List<Variable> parentVariables)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.potential = potential;
        this.rootVariable = topVariable;
        this.parentVariables = parentVariables;
        this.statesBranch = false;
        this.intervalBranch = true;
    }

	/**
     * Constructor for discretized and finite states variables with reference
     * @param branchStates
     * @param topVariable
     * @param reference
     * @param parentVariables
     */
    public TreeADDBranch (List<State> branchStates,
                          Variable topVariable,
                          String reference,
                          List<Variable> parentVariables)
    {
        this.states = branchStates;
        this.reference = reference;
        this.rootVariable = topVariable;
        this.parentVariables = parentVariables;
        this.statesBranch = true;
        this.intervalBranch = false;
    }

    /**
     * Constructor for numeric variables with reference
     * @param thresholdMin
     * @param thresholdMax
     * @param potential
     * @param topVariable
     * @param parentVariables
     */
    public TreeADDBranch (Threshold lowerBound,
                          Threshold upperBound,
                          Variable topVariable,
                          String reference,
                          List<Variable> parentVariables)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.reference = reference;
        this.rootVariable = topVariable;
        this.parentVariables = parentVariables;
        this.statesBranch = false;
        this.intervalBranch = true;
    }
    
    public TreeADDBranch copy ()
    {
        TreeADDBranch branch = null;
        if(potential != null)
        {
            if (this.rootVariable.getVariableType() == VariableType.FINITE_STATES
                    || this.rootVariable.getVariableType() == VariableType.DISCRETIZED) {
                branch = new TreeADDBranch(new ArrayList<>(getBranchStates()),
                        this.getRootVariable(),
                        this.getPotential().copy(),
                        this.getParentVariables());

            } else if (this.rootVariable.getVariableType() == VariableType.NUMERIC) {
                branch = new TreeADDBranch(new Threshold(this.getLowerBound()),
                		new Threshold(this.getUpperBound()),
                        this.getRootVariable(),
                        this.getPotential().copy(),
                        this.getParentVariables());
            }
            if(label != null)
            {
                branch.setLabel(label);
            }
        }else if(reference != null)
        {
            if (this.rootVariable.getVariableType () == VariableType.FINITE_STATES
                    || this.rootVariable.getVariableType () == VariableType.DISCRETIZED)
           {            
                branch = new TreeADDBranch(new ArrayList<>(getBranchStates()),
                        this.getRootVariable(),
                        this.reference,
                        this.getParentVariables());
            }
            else if (this.rootVariable.getVariableType () == VariableType.NUMERIC)
            {
                branch = new TreeADDBranch(new Threshold(this.getLowerBound()),
                		new Threshold(this.getUpperBound()),
                        this.getRootVariable(),
                        this.reference,
                        this.getParentVariables());
           }
            if(referencedBranch != null)
            {
                branch.setReferencedBranch(referencedBranch);
            }
        }else // HACK for interventions 
        {
        	if (this.rootVariable.getVariableType() == VariableType.FINITE_STATES
                    || this.rootVariable.getVariableType() == VariableType.DISCRETIZED) {
                branch = new TreeADDBranch(new ArrayList<>(getBranchStates()),
                        this.getRootVariable(),
                        (Potential)null,
                        this.getParentVariables());

            } else if (this.rootVariable.getVariableType() == VariableType.NUMERIC) {
                branch = new TreeADDBranch(new Threshold(this.getLowerBound()),
                		new Threshold(this.getUpperBound()),
                        this.getRootVariable(),
                        (Potential)null,
                        this.getParentVariables());
            }
        }
        return branch;
    }    
    
    public List<State> getStates() {
		return states;
	}

    public List<Variable> getAddableVariables() {
        List<Variable> addableVariables = new ArrayList<>();
        List<Variable> potentialVariables = potential.getVariables();
        for (Variable variable : parentVariables) {
            if (!variable.equals(rootVariable)
                    && !potentialVariables.contains(variable)
                    && !variable.equals(potential.getConditionedVariable())) {
                addableVariables.add(variable);
            }
        }
        return addableVariables;
    }
    
    public void setLowerBound (Threshold lowerBound)
    {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound (Threshold upperBound)
    {
        this.upperBound = upperBound;
    }

    public List<State> getBranchStates ()
    {
        return this.states;
    }

    public List<Variable> getParentVariables ()
    {
        return this.parentVariables;
    }

    public void setParentVariables (List<Variable> parentVariables)
    {
        this.parentVariables = parentVariables;
    }

    public Variable getRootVariable ()
    {
        return this.rootVariable;
    }

    public Potential getPotential ()
    {
        return (potential != null || referencedBranch == null) ? potential
                : referencedBranch.getPotential();
    }

    public void setPotential (Potential potential)
    {
        this.potential = potential;
    }

    public void setRootVariable (Variable topVariable)
    {
        this.rootVariable = topVariable;
    }

    public void setStates (List<State> states)
    {
        this.states = states;
    }

    public Threshold getLowerBound ()
    {
        return lowerBound;
    }

    public Threshold getUpperBound ()
    {
        return upperBound;
    }
    
    public boolean isInsideInterval(double numericValue)
    {
        return (lowerBound == null || lowerBound.isBelow(numericValue))
                && (upperBound == null || upperBound.isAbove(numericValue));
    }

    public String getLabel() {
        return this.label;
    }    

    public void setLabel(String label) {
        this.label = label;
    }    
    
    public boolean isLabeled()
    {
        return this.label != null;
    }    

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return this.reference;
    }
    
    public boolean isReference()
    {
        return this.reference != null;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder ();
        builder.append(indent);
        builder.append("branch (");
        builder.append(rootVariable);
        builder.append(")");
        builder.append(". States = ");
        builder.append(states);
        builder.append(". ");
        if (potential != null) {
            List<Variable> potentialVariables = potential.getVariables();
            if (potentialVariables != null && potentialVariables.size() > 0) {
            	builder.append(" ");
            	builder.append(potentialVariables.size());
            	builder.append(" variables(");
            	for (int i = 0; i < potentialVariables.size(); i++) {
            		builder.append(potentialVariables.get(i));
            		if (i < potentialVariables.size() - 1) {
            			builder.append(", ");
            		} else {
            			builder.append(")");
            		}
            	}
            }
        }
        if (parentVariables != null && parentVariables.size() > 0 && !(potential instanceof Intervention)) {
			builder.append("\n");
			builder.append(indent);
        	builder.append("ParentVariables = ");
        	builder.append(parentVariables);
        }
        if (lowerBound != null && upperBound != null) {
			builder.append("\n");
			builder.append(indent);
        	builder.append("Interval: (");
        	builder.append(lowerBound);
        	builder.append(", ");
        	builder.append(upperBound);
        	builder.append(")");
        }
		builder.append("\n");
		if (potential != null && potential.getClass() == Intervention.class) {
			List<TreeADDBranch> branches = ((Intervention)potential).getBranches();
			for (TreeADDBranch branch : branches) {
				branch.setIndent(indent + "    ");
				builder.append(branch.toString());
			}
		}
        return builder.toString ();
    }

    public void setReferencedBranch(TreeADDBranch treeADDBranch) {
        this.reference = treeADDBranch.getLabel();
        this.referencedBranch = treeADDBranch;
        this.potential = null;
    }
    
    public boolean isIntervalBranch() {
		return intervalBranch;
	}

	public boolean isStatesBranch() {
		return statesBranch;
	}

}
