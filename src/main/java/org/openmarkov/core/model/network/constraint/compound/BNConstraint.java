package org.openmarkov.core.model.network.constraint.compound;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;

public class BNConstraint extends NetworkTypeConstraint {

	// Attributes
	private static BNConstraint constraint = null;
	
	// Constructor
	private BNConstraint() {
		constraints.add(NoCycles.getUniqueInstance());
		constraints.add(
				AllChanceVariablesHaveChancePotentials.getUniqueInstance());
		constraints.add(NoSelfLoops.getUniqueInstance());
		constraints.add(OnlyChanceNodes.getUniqueInstance());
		constraints.add(OnlyDirectedLinks.getUniqueInstance());
	}

	// Methods
	public static BNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new BNConstraint();
		}
		return constraint;
	}
	
}
