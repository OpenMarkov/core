package org.openmarkov.core.model.decisiontree;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.decisiontree.DecisionTreeElement;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * @author Manuel Arias
 */
public class DecisionTreeNode extends DecisionTreeElement {

	// Attributes
    private NodeType nodeType;
    private Potential potential = null;
    private List<DecisionTreeElement> children;
    private DecisionTreeElement parent;
    private double scenarioProbability;

    // Constructor
	/**
	 * @param node
	 */
	public DecisionTreeNode(Node node) {
		super(node.getVariable());
		nodeType = node.getNodeType();
        List<Potential> potentials = node.getPotentials();
        potential = !potentials.isEmpty() ? potentials.get(0) : null;
        children = new ArrayList<>();
        parent = null;
        utility = NO_UTILITY;
        scenarioProbability = NO_PROBABILITY;
	}

	@Override
    public double getUtility() {
    	return utility == NO_UTILITY ? calculateUtilityFromChildren() : utility;
    }
    
	/**
	 * Calculates the utility depending on <code>NodeType</code> (CHANCE, DECISION and UTILITY).
	 * In UTILITY nodes, the method takes into account the potential type.
	 * Store the utility in the internal variable <code>utility</code>.
	 * @return utility. <code>double</code>
	 */
	private double calculateUtilityFromChildren() {
		if (children.isEmpty()) {
			utility = 0.0;
		} else {
			switch(nodeType) {
			case UTILITY:
				if (potential instanceof SumPotential) {
					utility = 0;
					for (DecisionTreeElement child : children) {
						utility += child.getUtility();
					}
				} else if(potential instanceof ProductPotential) {
					utility = 1;
					for (DecisionTreeElement child : children) {
						utility *= child.getUtility();
					}
				} else if(potential instanceof TablePotential) {
					utility = ((TablePotential)potential).getValue(getBranchStates());
				}
				break;
			case DECISION:
				utility = Double.NEGATIVE_INFINITY;
				for (DecisionTreeElement branch : children) {
					double branchUtility = branch.getUtility();
					// In a decision node, the utility is the maximum utility of its branches.
					utility = branchUtility > utility ? branchUtility : utility;
				}
				break;
			case CHANCE:
				utility = 0;
				for (DecisionTreeElement child : children) {
					utility += child.getUtility ();
				}
				break;
			default:
				// Do nothing in case of SV_SUM, SV_PRODUCT. 
				break;
			}
		}
		return utility;
	}

	@Override
	public EvidenceCase getBranchStates() {
        return (parent != null) ? parent.getBranchStates () : new EvidenceCase();
	}

	@Override
	public double getScenarioProbability() {
		return scenarioProbability == NO_PROBABILITY ? calculateScenarioProbability() : scenarioProbability;
	}

    /**
     * The scenario probability is applied for CHANCE and DECISION nodes.<p>
     * The scenario probability is:
     * <ul>
     * <li>CHANCE nodes: the summation of the scenario probabilities of the children.
     * <li>DECISION nodes: the scenario probability of the (unique) child.
     * </ul>  
     * @return scenarioProbability. <code>double</code>
     */
    private double calculateScenarioProbability() {
    	if (!children.isEmpty()) {
        	if (nodeType == NodeType.CHANCE) {
            	scenarioProbability = 0.0;
        		for (DecisionTreeElement child : children) {
        			scenarioProbability += child.getScenarioProbability();
        		}
        	} else if (nodeType == NodeType.DECISION) {
        		scenarioProbability = children.get(0).getScenarioProbability();
        	}
    	} else {
        	scenarioProbability = 0.0;
    	}
    	return scenarioProbability;
	}
    
    /**
     * Compares the children utilities with the given branch utility. 
     * @param branch
     * @return TRUE when the branch utility is better or equal to the other branches utilities.
     */
    public boolean isBestDecision(DecisionTreeElement branch) {
        boolean isBestDecision;
        if (nodeType == NodeType.DECISION) {
            isBestDecision = true;
            double thisUtility = branch.getUtility();
            int numChildren = children.size();
            for (int i = 0; i < numChildren && isBestDecision; i++) {
            	isBestDecision &= thisUtility >= children.get(i).getUtility();
            }
        } else {
        	isBestDecision = true;  // CHANCE nodes
        }
        return isBestDecision;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder ();
        builder.append ("DecisionTreeNode [variable=");
        builder.append (elementVariable.getName());
        builder.append (", children=").append (children);
        builder.append ("]");
        return builder.toString ();
    }

	public NodeType getNodeType() {
    	return nodeType;
    }

}
