package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

@SuppressWarnings("serial")
public class SetPotentialEdit extends SimplePNEdit {
	private PotentialType lastPotentialType;
	private Potential lastPotential;
	private PotentialType newPotentialType;
	private Variable variable;
	private Potential newPotential = null;
	
	/**
	 * Creates a new SetPotentialEdit object that sets the a new potential 
	 * with the type specified for the probNode object.  
	 * 
	 * @param probNode
	 * 		The probNode that contains the potential to modify
	 * @param newPotentialType
	 * 		The potential type of the new potential to be created 
	 */
	public SetPotentialEdit(ProbNode probNode, PotentialType newPotentialType ){
		super ( probNode.getProbNet() );
		
		this.variable = probNode.getVariable();
		if ( !(probNode.getNodeType()== NodeType.DECISION && 
				probNode.getPolicyType() == PolicyType.OPTIMAL)){
			lastPotential = probNode.getPotentials().get( 0 );
		}
		
		this.newPotentialType = newPotentialType;
		
	}
	/**
	 * SetPotentialEdit object that changes the last Potential 
	 * with the potential specified for the probNode object.  
	 * 
	 * @param probNode
	 * 		The probNode that contains the potential to set.
	 * @param newPotentialType
	 * 		The new potential object 
	 */
	public SetPotentialEdit(ProbNode probNode, Potential potential ){
		super ( probNode.getProbNet() );
		this.variable = probNode.getVariable();
		lastPotential = probNode.getPotentials().get( 0 );
		this.newPotentialType = potential.getPotentialType();
		newPotential = potential;
	}
	

	@Override
	public void doEdit() throws DoEditException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		ProbNode probNode =probNet.getProbNode(variable);
		PotentialRole role;
		if ( (probNode.getNodeType()== NodeType.DECISION && 
				probNode.getPolicyType() == PolicyType.OPTIMAL)){
			role = PotentialRole.POLICY;
			variables.add(variable);
			for (Node node:probNode.getNode().getParents()){
				variables.add(((ProbNode)node.getObject()).getVariable());
			}
			
		}else{
			variables = lastPotential.getVariables();
			role = lastPotential.getPotentialRole();
		}
		ArrayList<Potential> potentials = new ArrayList <Potential>();
		if ( newPotential == null ){
			switch (newPotentialType){
			case UNIFORM:
				newPotential = new UniformPotential(variables, role);
				break;
			case TABLE:
				try {
					newPotential = new TablePotential( variables, role);
				} catch (NotEnoughMemoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case TREE_ADD:
				// Creates a potential over the defined variables
				newPotential= new TreeADDPotential(variables,variables.get(1),role);
				break;
			case CYCLE_LENGTH_SHIFT:
				newPotential = 
					new CycleLengthShift( variables);
				break;
			case SAME_AS_PREVIOUS:
				try {
					//TODO revisar el paso del parámetro timeSlice
					newPotential = new SameAsPrevious(
						SameAsPrevious.getPotential(probNet, variable), 
						1, probNet);
				} catch (NodeNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case SUM:
				newPotential = new SumPotential(
						variables, probNet.getProbNodes(variables), role);
				break;
			case PRODUCT:
				newPotential = new ProductPotential(
						variables, probNet.getProbNodes(variables), role);
				break;
			}
		}
		
		if ( !(probNode.getNodeType()== NodeType.DECISION && 
				probNode.getPolicyType() == PolicyType.OPTIMAL)){
			if ( lastPotential.isUtility() && !( lastPotential instanceof 
					TreeADDPotential && newPotential instanceof TreeADDPotential ) ) {
				newPotential.setUtilityVariable(
						lastPotential.getUtilityVariable());
			}
		}else{
			probNet.getProbNode(variable).setPolicyType(PolicyType.PROBABILISTIC);
		}
		potentials.add(newPotential);
		probNet.getProbNode(variable).setPotentials(potentials);
		
	}
	
	public void undo(){
		super.undo();
		ProbNode probNode = probNet.getProbNode( variable );
		ArrayList<Potential> potentials = new ArrayList <Potential>();
		if ( lastPotential != null){
			potentials.add(lastPotential);
		}else if (probNode.getNodeType() == NodeType.DECISION){
			probNode.setPolicyType(PolicyType.OPTIMAL);
			
		}
		probNode.setPotentials(potentials);
	}

	public PotentialType getNewPotentialType() {
		// TODO Auto-generated method stub
		return newPotentialType;
	}
	public Potential getNewPotential(){
		return newPotential;
	}

}
