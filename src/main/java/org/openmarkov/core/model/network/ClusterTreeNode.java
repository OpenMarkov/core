package org.openmarkov.core.model.network;

import java.util.ArrayList;

import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.potential.TablePotential;


/** A <code>ClusterTreeNode</code> is a set of variables.
 * @author manuel
 * @author fjdiez 
 * @see org.openmarkov.core.model.network.ClusterForest
 * @see org.openmarkov.core.model.network.ClusterTreeNode
 * @version 1.0 */
public class ClusterTreeNode extends Node {

	// Attributes
	protected ArrayList<Variable> fsVariables;
	
	protected TablePotential tablePotential;
	
    // Constructor
	/** @param probNet. <code>ProbNet</code>.
	 * @param object. <code>Object</code>. */
	public ClusterTreeNode(ProbNet probNet, Object object) {
        super(probNet.getGraph(), object);
	}
	
	// Methods
	/** @param tablePotential. <code>TablePotential</code> */
	public void setPotential(TablePotential tablePotential) {
		this.tablePotential = tablePotential;
		fsVariables.addAll(tablePotential.getVariables());
	}
	
	/** @return The <code>TablePotential</code>. */
	public TablePotential getPotential() {
		return tablePotential;
	}
	
	/** @param fsVariables <code>ArrayList</code> of <code>Variable</code>s */
	public void setVariables(ArrayList<Variable> fsVariables) {
		this.fsVariables = fsVariables;
	}
	
	/** @return variables <code>ArrayList</code> of <code>Variable</code>. */
	public ArrayList<Variable> getVariables() {
		return fsVariables;
	}

}
