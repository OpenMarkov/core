package org.openmarkov.core.action;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.compound.NetworkTypeConstraint;

/**
 * <code>ChangeNetworkTypeEdit</code> is a edit that allow to change the 
 * <code>NetworkTypeConstraint</code> object to one network.
 * 
 *  
 * @version 1.0 21/12/10
 * @author mpalacios
 * 
 */
//TODO verify the performance when undo is executed.  
public class ChangeNetworkTypeEdit extends SimplePNEdit {
	
	/**
	 * The current NetworkTypeConstraint associated with the network
	 */
	private NetworkTypeConstraint currentNetworkTypeConstraint;
	
	/**
	 * The new NetworkTypeConstraint associated with the network
	 */
	private NetworkTypeConstraint newNetworkTypeConstraint;

	
	/**
	 * Creates a new <code>ChangeNetworkTypeEdit</code> that allow to change the 
	 *<code>NetworkTypeConstraint</code> object in the network.
	 * @param probNet the network that will be edited.
	 * @param newNetworkTypeConstraint the new <code>NetworkTypeConstraint</code>
	 * object
	 *      
	 */
	public ChangeNetworkTypeEdit(ProbNet probNet,
			NetworkTypeConstraint newNetworkTypeConstraint) {
		super(probNet);
		this.currentNetworkTypeConstraint = 
			probNet.getNetworkTypeConstraint();
		this.newNetworkTypeConstraint = newNetworkTypeConstraint;
	}

	// Methods
	@Override
	public void doEdit() {
		if (newNetworkTypeConstraint != null) {
			probNet.setNetworkTypeConstraint(newNetworkTypeConstraint);
		}
	}

	public void undo() {
		super.undo();
		if (newNetworkTypeConstraint != null) {
			probNet.setNetworkTypeConstraint(currentNetworkTypeConstraint);
		}
	}
}
