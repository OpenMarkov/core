package org.openmarkov.core.model.network.constraint.compound;

import org.apache.log4j.Logger;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.ProbNet;

/** This class has a collection of static methods. Each method is a factory that
 * 	creates an empty <code>ProbNet</code> with one compound constraint 
 *  associated that defines the type of network. */
public class PNFactories {

	/** Static method that returns an empty bayesian network: a 
	 *  <code>ProbNet</code> with a
	 *  <code>openmarkov.networks.constraints.compound.BNConstraint</code>. */
	public static ProbNet getEmptyBN() {
		//ProbNet probNet = new ProbNet();
		ProbNet probNet = new ProbNet(BNConstraint.getUniqueInstance());
		/*try {
			probNet.addConstraint(
					BNConstraint.getUniqueInstance(), true);
		} catch (ConstraintViolationException e) { // Not possible exception
			ExceptionsHandler.handleException(e, null, true);
		}*/
		return probNet;
	}
	
	/** Static method that returns an empty influence diagram: a 
	 *  <code>ProbNet</code> with a
	 *  <code>openmarkov.networks.constraints.compound.IDConstraint</code>. */
	public static ProbNet getEmptyID() {
		ProbNet probNet = new ProbNet();
		try {
			probNet.addConstraint(
					IDConstraint.getUniqueInstance(), false);
		} catch (ConstraintViolationException e) {
			Logger.getLogger(PNFactories.class).fatal(e);
		}
		return probNet;
	}
	
}
