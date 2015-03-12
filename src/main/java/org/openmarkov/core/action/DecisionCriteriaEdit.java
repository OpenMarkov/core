
package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.jmx.Agent;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;

@SuppressWarnings("serial")
public class DecisionCriteriaEdit extends SimplePNEdit
{
    private String                     criterionName;
    private StateAction                stateAction;
    // private StringsWithProperties lastAgents;
    private List<Criterion>            lastCriteria;
    private String 	                   newName;
    private int						   criterionIndex;

    public DecisionCriteriaEdit (ProbNet probnet,
                                 StateAction stateAction,
                                 String newName,
                                 String agentName,
                                 int criterionIndex)
    {
        super (probnet);
        this.criterionName = agentName;
        this.stateAction = stateAction;
        this.newName = newName;
        this.criterionIndex = criterionIndex;
        /*
        if (probnet.getAgents () != null)
        {
            this.lastCriteria = new ArrayList<Criterion> (probnet.getDecisionCriteria ());
        }
        else
        {
            this.lastCriteria = probnet.getDecisionCriteria ();
        }*/
        
        this.lastCriteria = new ArrayList<Criterion> (probnet.getDecisionCriteria ());
    }

    @Override
    public void doEdit ()
        throws DoEditException
    {
        // StringsWithProperties agents = probNet.getAgents();
        List<Criterion> criteria = probNet.getDecisionCriteria ();
        Criterion criterion = null;
        switch (stateAction)
        {
            case ADD :
                criterion = new Criterion (criterionName);
                // agents.put(agentName);
                criteria.add (criterion);
                break;
            case REMOVE :
                /*
            	for (Criterion criterio : criteria)
                {
                    if (criterio.getCriterionName().equals (criterionName))
                    {
                        criterion = criterio;
                    }
                }
                criteria.remove (criterion);
                */
            	criteria.remove(criterionIndex);
                // TODO assign criteria to node
                // it is also necessary to delete this criteria from the node it
                // was assigned to
                /*
                 * if (criteria != null) { for (Node node :
                 * probNet.getNodes()) { if
                 * (node.getVariable().getDecisionCriteria
                 * ().getString().equals(criteriaName)) {
                 * node.getVariable().setDecisionCriteria(null); } } }
                 */
                if (criteria.size () == 0)
                {
                    criteria = null;
                }
                break;
            case DOWN :
                // StringsWithProperties newAgentsDown = new
                // StringsWithProperties();
            	
            	Criterion swapDown = criteria.get(criterionIndex);
        		criteria.set(criterionIndex, criteria.get(criterionIndex+1));
        		criteria.set(criterionIndex+1, swapDown);
        		            	
            	/*
            	ArrayList<Criterion> newCriteriasDown = new ArrayList<Criterion> ();
                for (int i = 0; i < dataTable.length; i++)
                {
                    // newAgentsDown.put((String)dataTable[i][0]);
                    newCriteriasDown.add (new Criterion ((String) dataTable[i][0]));
                }
                probNet.setDecisionCriteria (newCriteriasDown);
                */
                break;
            case UP :
                // StringsWithProperties newAgentsUp = new
                // StringsWithProperties();
            	/*
                ArrayList<Criterion> newCriteriasUp = new ArrayList<Criterion> ();
                for (int i = 0; i < dataTable.length; i++)
                {
                    // newAgentsUp.put((String)dataTable[i][0]);
                    newCriteriasUp.add (new Criterion ((String) dataTable[i][0]));
                }
                probNet.setDecisionCriteria (newCriteriasUp);
                */
            	
            	Criterion swapUp = criteria.get(criterionIndex);
        		criteria.set(criterionIndex, criteria.get(criterionIndex-1));
        		criteria.set(criterionIndex-1, swapUp);
        		
                break;
            case RENAME :
                // agents.rename(agentName, newName);
                // StringsWithProperties newAgentsRename = new
                // StringsWithProperties();

            	if(criteria.get(criterionIndex).equals(criterionName)){
            		criteria.get(criterionIndex).setCriterionName(newName);
            	}
            	/*
            	for (Criterion criterio : criteria)
                {
                    if (criterio.getCriterionName().equals (criterionName))
                    {
                        criterio.setCriterionName(newName);
                    }
                }*/
            	/*
                for (int i = 0; i < dataTable.length; i++)
                {
                    // newAgentsRename.put((String)dataTable[i][0]);
                    newCriteriasRename.add (new Criterion ((String) dataTable[i][0]));

                }*/
                // We substitute the new name in the nodes they had that criterion
                for(Node node : probNet.getNodes()){
                	// Only Utility nodes have criterion
                	if(node.getNodeType() == NodeType.UTILITY &&
                			// we get the utility nodes with no empty criterion
                			node.getVariable().getDecisionCriterion()!= null && 
                			// we get the nodes with the same criterion as criterionName
                			node.getVariable().getDecisionCriterion().getCriterionName().equals(criterionName)){
                			// We change the name of the criterion in those nodes
                			node.getVariable().getDecisionCriterion().setCriterionName(newName);
                		
                	}
                }

                
                //probNet.setDecisionCriteria (newCriteriasRename);
                break;
		default:
			break;
        }
    }
    
    

    public StateAction getStateAction() {
		return stateAction;
	}

	public String getNewName() {
		return newName;
	}
	

	public List<Criterion> getLastCriteria() {
		return lastCriteria;
	}

	public void setLastCriteria(List<Criterion> lastCriteria) {
		this.lastCriteria = lastCriteria;
	}

	@Override
    public void undo ()
    {
        super.undo ();
        probNet.setDecisionCriteria (lastCriteria);
        
        
    }
}
