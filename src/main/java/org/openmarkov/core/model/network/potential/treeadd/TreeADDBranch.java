
package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * TreeADDBranch represents branch of a treeADD. If the top variable of the
 * treeADD is numeric a branch is defined by two thresholds: a minimum and a
 * maximum limit If top variable is finite states, then each branch is defined
 * by its states In both cases branches have a potential assigned,. If the
 * branch is a leaf, it potential could be any kind of potential except a
 * TreeADDPotential
 * @author myebra
 */
public class TreeADDBranch
{
    /**
     * Each treeADDBranch has a potential associated
     */
    private Potential      potential;
    /**
     * If the topVariable of the tree is a finite states or a discretized
     * variable each branch has an associated state.
     */
    private List<State>    states;
    private List<Variable> parentVariables;
    /**
     * If the topVariable of the tree is a continuous variable it is defined in
     * a continuous interval which has two thresholds.
     */
    private Threshold      thresholdMin;
    private Threshold      thresholdMax;
    /**
     * A branch can be labeled, labels are used to reference potential from
     * other branches when that potential has more than one parents
     */
    private Variable       topVariable;

    /* private String label; */
    /**
     * A branch can reference a potential from other branch that has been
     * labeled
     */
    /* private String reference; */
    /**
     * Constructor for discretized and finite estates variables
     * @param branchStates
     * @param potential
     * @param topVariable
     * @param parentVariables
     */
    public TreeADDBranch (List<State> branchStates,
                          Potential potential,
                          Variable topVariable,
                          List<Variable> parentVariables)
    {
        this.states = branchStates;
        this.potential = potential;
        this.topVariable = topVariable;
        this.parentVariables = parentVariables;
        // this.treeADD = treeADD;
    }

    /**
     * Constructor for the parser
     * @param thresholds
     * @param potential
     */
    public TreeADDBranch (Threshold[] thresholds,
                          Potential potential,
                          Variable topVariable,
                          List<Variable> parentVariables)
    {
        this.thresholdMax = thresholds[1];
        this.thresholdMin = thresholds[0];
        this.topVariable = topVariable;
        this.parentVariables = parentVariables;
        this.potential = potential;
    }

    /*
     * public TreeADDBranch(ArrayList<State> branchStates, Potential potential,
     * String label) { this.states = branchStates; this.potential = potential;
     * this.label = label; //this.treeADD = treeADD; }
     */
    /*
     * @argCondition reference must be one of the labels in this ADD and the
     * reference must not create a cycle in the ADD. If a branch has assigned a
     * reference cannot has assigned a potential
     */
    /*
     * public void setReference(String reference) { this.reference = reference;
     * }
     */
    /*
     * @argCondition label is incompatible with reference
     */
    /*
     * public void setLabel(String label) { this.label = label; }
     */
    /*
     * public TreeADDBranch(ArrayList<State> branchStates, String reference) {
     * this.states = branchStates; this.reference = reference; }
     */
    /**
     * Constructor for numeric variables
     * @param thresholdMin
     * @param thresholdMax
     * @param potential
     * @param topVariable
     * @param parentVariables
     */
    public TreeADDBranch (Threshold thresholdMin,
                          Threshold thresholdMax,
                          Potential potential,
                          Variable topVariable,
                          List<Variable> parentVariables)
    {
        this.thresholdMin = thresholdMin;
        this.thresholdMax = thresholdMax;
        this.potential = potential;
        this.topVariable = topVariable;
        this.parentVariables = parentVariables;
    }

    /*
     * public TreeADDBranch(ArrayList<State> branchStates, Potential potential,
     * String label) { this.states = branchStates; this.label = label;
     * this.potentialsLabeled = new HashMap<String, Potential>(); this.potential
     * = potentialsLabeled.put(label, potential); } public
     * TreeADDBranch(ArrayList<State> branchStates, String reference) {
     * this.states = branchStates; }
     */
    public void setThresholdMin (Threshold min)
    {
        this.thresholdMin = min;
    }

    public void setThresholdMax (Threshold max)
    {
        this.thresholdMax = max;
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

    public Variable getTopVariable ()
    {
        return this.topVariable;
    }

    /*
     * public String getLabel(){ return this.label; } public String
     * getReference(){ return this.reference; }
     */
    public Potential getPotential ()
    {
        return this.potential;
    }

    public void setPotential (Potential potential)
    {
        this.potential = potential;
    }

    public void setTopVariable (Variable topVariable)
    {
        this.topVariable = topVariable;
    }

    public void setStates (List<State> states)
    {
        this.states = states;
    }

    public Threshold getMinThreshold ()
    {
        return thresholdMin;
    }

    public Threshold getMaxThreshold ()
    {
        return thresholdMax;
    }

    public TreeADDBranch copy ()
    {
        TreeADDBranch branch = null;
        if (this.topVariable.getVariableType () == VariableType.FINITE_STATES
            || this.topVariable.getVariableType () == VariableType.DISCRETIZED)
        {

			List<State> states = new ArrayList<>();
			for (int i = 0; i < this.getBranchStates().size(); i++) {
				states.add(getBranchStates().get(i));
			}
			branch = new TreeADDBranch(states, this.getPotential().copy(),
					this.getTopVariable(), this.getParentVariables());
            
        }
        else if (this.topVariable.getVariableType () == VariableType.NUMERIC)
        {
			branch = new TreeADDBranch(this.getMinThreshold().copy(), this
					.getMaxThreshold().copy(), this.getPotential().copy(),
					this.getTopVariable(), this.getParentVariables());
        }
        return branch;
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder ();
        builder.append ("TreeADDBranch [potential=");
        builder.append (potential);
        builder.append (", states=");
        builder.append (states);
        builder.append (", parentVariables=");
        builder.append (parentVariables);
        builder.append (", thresholdMin=");
        builder.append (thresholdMin);
        builder.append (", thresholdMax=");
        builder.append (thresholdMax);
        builder.append (", topVariable=");
        builder.append (topVariable);
        builder.append ("]");
        return builder.toString ();
    }
    
    
    
}
