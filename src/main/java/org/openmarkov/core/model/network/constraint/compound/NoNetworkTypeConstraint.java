package org.openmarkov.core.model.network.constraint.compound;

public class NoNetworkTypeConstraint extends NetworkTypeConstraint {

	// Attributes
	private static NoNetworkTypeConstraint constraint = null;
	
	// Constructor
	private NoNetworkTypeConstraint() {
	}

	// Methods
	public static NoNetworkTypeConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new NoNetworkTypeConstraint();
		}
		return constraint;
	}
	
}
