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
	
	// Constructors
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
		List<DecisionTreeElement> childrenClone;
		if (children.size() > 0) {
			childrenClone = new ArrayList<>(children.size());
			childrenClone.addAll(children);
		} else {
			childrenClone = new ArrayList<>();
		}
		return childrenClone;
	}
	
	/**
	 * @return The (supposedly) unique element below <code>this</code>.
	 */
	public DecisionTreeElement getChild() {
		DecisionTreeElement child = null;
		if (children.size() > 0) {
			child = children.get(0);
		}
		return child;
	}

    public void addChild(DecisionTreeElement child) {
    	children.add(child);
    }
    
    /**
     * Sets the received DecisionTreeElement as the only child.
     * @param child
     */
    public void setChild(DecisionTreeElement child) {
    	children.clear();
    	addChild(child);
    }
    
    public void setParent(DecisionTreeElement parent) {
        this.parent = parent;
    }
    
    public DecisionTreeElement getParent() {
        return parent;
    }
    
    public Variable getVariable() {
    	return elementVariable;
    }

	public abstract double getUtility();

	public abstract EvidenceCase getBranchStates();

	public abstract double getScenarioProbability();

}
