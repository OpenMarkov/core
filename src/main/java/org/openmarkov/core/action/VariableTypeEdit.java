package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UniformPotential;

public class VariableTypeEdit extends SimplePNEdit {
	//private ProbNet probNet;
	private ProbNode probNode;
	private VariableType newType;
	private VariableType currentType;
	
	public VariableTypeEdit(ProbNode probNode, 
			VariableType newType){
		//this.probNet = probNet;
		this.probNode = probNode;
		this.newType = newType;
		this.currentType = probNode.getVariable().getVariableType();
		
	}
	@Override
	public void doEdit() throws DoEditException {
		ArrayList<Node> nodes;
		if (currentType.compareTo(VariableType.NUMERIC) == 0){
			probNode.getVariable().setStates(probNode.getProbNet().getDefaultStates());
			ArrayList <Variable> variables = new ArrayList<Variable> ();
			if (probNode.getNodeType() != NodeType.UTILITY){
				variables.add(probNode.getVariable());
			}
			
			for (Node node: probNode.getNode().getParents()){
				variables.add(((ProbNode)node.getObject()).getVariable());
			}
			UniformPotential uniformPotential = new UniformPotential(variables, 
					probNode.getPotentials().get(0).getPotentialRole());
			
			ArrayList<Potential> potentials = new ArrayList<Potential>(1);
			potentials.add(uniformPotential);
			
			probNode.setPotentials(potentials);
					probNode.setUniformPotential();
			nodes = probNode.getNode().getChildren();
			for (Node node:nodes){
				ProbNode child = (ProbNode)node.getObject();
				child.setUniformPotential();
			}
		}
		probNode.getVariable().setVariableType(newType);
	}
	@Override
	public void undo(){
		super.undo();
		probNode.getVariable().setVariableType(currentType);
	}
	public VariableType getNewVariableType(){
		return newType;
	}
	
	
	public ProbNode getProbNode()
	{
		
		return this.probNode;
	}

}
