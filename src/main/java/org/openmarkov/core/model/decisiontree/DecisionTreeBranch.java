package org.openmarkov.core.model.decisiontree;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

public class DecisionTreeBranch extends DecisionTreeElement {

	// Attributes
    private State branchState;
    private ProbNet probNet;
    private EvidenceCase scenarioEvidence;
    private double scenarioProbability; 

    // Constructors
	/**
	 * @param probNet
	 * @param branchVariable
	 * @param branchState
	 */
	public DecisionTreeBranch(ProbNet probNet, Variable branchVariable, State branchState) {
		super(branchVariable);
        this.probNet = probNet;
        this.branchState = branchState;
        this.scenarioEvidence = null;
        this.scenarioProbability = NO_PROBABILITY;
	}
	
    /**
     * @param probNet
     */
    public DecisionTreeBranch (ProbNet probNet) {
        this (probNet, null, null);
    }

	// Methods
	@Override
    public double getUtility() {
        if (utility == NO_PROBABILITY) {
        	DecisionTreeElement child = getChild();
            utility = (child != null) ? child.getUtility () : 0.0;
            if (parent != null && ((DecisionTreeNode)parent).getNodeType() == NodeType.CHANCE) {
                utility *= getBranchProbability();
            }
        }
        return utility;
    }
	
    public double getBranchProbability() {
    	double parentScenarioProb = parent.getScenarioProbability();
    	return (parentScenarioProb != 0) ? getScenarioProbability() / parentScenarioProb : 0;
    }   
    
	@Override
    public EvidenceCase getBranchStates() {
    	if (scenarioEvidence == null) {
	        scenarioEvidence = (parent!=null)? new EvidenceCase(parent.getBranchStates()) : new EvidenceCase();
	        if (elementVariable != null) {
	            try {
	            	scenarioEvidence.addFinding (new Finding(elementVariable, branchState));
	            }
	            catch (InvalidStateException | IncompatibleEvidenceException e) {
	                e.printStackTrace();
	            }
	        }
    	}
        return scenarioEvidence;
    }
    
    public double getScenarioProbability() {
        if (scenarioProbability == NO_PROBABILITY) {
        	scenarioProbability = 1.0;    	
        	if (((DecisionTreeNode)getChild()).getNodeType() == NodeType.UTILITY)	{
            	EvidenceCase evidenceCase = getBranchStates();
    	    	for (Finding finding : evidenceCase.getFindings())	 {
    	    		Node node = probNet.getNode(finding.getVariable());
    	    		if (node != null && node.getNodeType() == NodeType.CHANCE) {
    	    			Potential potential = node.getPotentials().get(0);
    	    			scenarioProbability *= potential.getProbability(evidenceCase);
    	    		}
    	    	}
        	} else {
        		scenarioProbability = getChild().getScenarioProbability();
        	}
        }
        return scenarioProbability;
    }       

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder ();
        builder.append("DecisionTreeBranch [branchVariable=").append(elementVariable).
        	append (", branchState=").append (branchState).append ("]");
        return builder.toString ();
    }

    /**
     * @return <code>State</code>
     */
    public State getBranchState() {
        return branchState;
    }
    
}
