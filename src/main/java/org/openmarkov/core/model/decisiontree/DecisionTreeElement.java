package org.openmarkov.core.model.decisiontree;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;

/** 
 * Common class for decision trees, that can be either a node or a branch.
 * @author Manuel Arias
 */
public abstract class DecisionTreeElement {

	// Constants
	protected final static double NO_UTILITY = Double.NaN;
	
	protected final static double NO_PROBABILITY = Double.NaN;
	
	// Attributes
	protected Variable elementVariable;
	
	protected DecisionTreeElement parent;
	
	protected List<DecisionTreeElement> children;
	
    protected double utility;
	
	// Constructor
	/**
	 * @param variable
	 */
	public DecisionTreeElement(Variable variable) {
		this.elementVariable = variable;
		this.parent = null;
		this.children = new ArrayList<>();
	}
	
	// Methods
	/**
	 * @return The elements below <code>this</code>.
	 */
	public List<DecisionTreeElement> getChildren() {
		List<DecisionTreeElement> childrenClone = new ArrayList<>(children.size());
		childrenClone.addAll(children);
		return childrenClone;
	}
	
	/**
	 * @return The (supposedly) unique element below <code>this</code>.
	 */
	public DecisionTreeElement getChild() {
		return children.size() > 0 ? children.get(0) : null;
	}

    /**
     * @param child. <code>DecisionTreeElement</code> 
     */
    public void addChild(DecisionTreeElement child) {
    	children.add(child);
    }
    
    /**
     * Sets the received <code>DecisionTreeElement</code> as the only child.
     * @param child
     */
    public void setChild(DecisionTreeElement child) {
    	children.clear();
    	children.add(child);
    }
    
    /**
     * @param parent. <code>DecisionTreeElement</code>
     */
    public void setParent(DecisionTreeElement parent) {
        this.parent = parent;
    }
    
    /**
     * @return parent. <code>DecisionTreeElement</code>
     */
    public DecisionTreeElement getParent() {
        return parent;
    }
    
    /**
     * @return variable
     */
    public Variable getVariable() {
    	return elementVariable;
    }

    // Abstract methods to be implemented
	public abstract double getUtility();

	public abstract EvidenceCase getBranchStates();

	public abstract double getScenarioProbability();

}
