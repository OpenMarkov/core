package org.openmarkov.core.model.network.constraint.compound;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;

public class MDPConstraint extends NetworkTypeConstraint {
	// Attributes
private static MDPConstraint constraint = null;

// Constructor
private MDPConstraint() {
	constraints.add(NoCycles.getUniqueInstance());
	constraints.add(
			AllChanceVariablesHaveChancePotentials.getUniqueInstance());
	constraints.add(NoSelfLoops.getUniqueInstance());
	constraints.add(OnlyDirectedLinks.getUniqueInstance());
	constraints.add(UtilityNodes.getUniqueInstance());
}

// Methods
public static MDPConstraint getUniqueInstance() {
	if (constraint == null) {
		constraint = new MDPConstraint();
	}
	return constraint;
}

}
