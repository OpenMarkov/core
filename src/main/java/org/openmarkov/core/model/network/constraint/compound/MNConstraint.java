package org.openmarkov.core.model.network.constraint.compound;

import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.NoEmptyName;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;

public class MNConstraint extends NetworkTypeConstraint {

	// Attributes
	private static MNConstraint constraint = null;
	
	// Constructor
	private MNConstraint() {
		constraints.add(NoEmptyName.getUniqueInstance());
		constraints.add(DistinctVariableNames.getUniqueInstance());
		constraints.add(OnlyChanceNodes.getUniqueInstance());
		constraints.add(OnlyUndirectedLinks.getUniqueInstance());
		// TODO Create OnlyAtemporalVariables constraint
//		constraints.add(OnlyAtemporalVariables.getUniqueInstance());
		constraints.add(NoSelfLoops.getUniqueInstance());
	}

	// Methods
	public static MNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new MNConstraint();
		}
		return constraint;
	}
	
	
	
}
