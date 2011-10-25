package org.openmarkov.core.model.network.constraint.compound;

import openmarkov.gui.constraints.ValidName;
import openmarkov.gui.constraints.ValidState;
import openmarkov.networks.constraints.DistinctVariableNames;
import openmarkov.networks.constraints.NoEmptyName;
import openmarkov.networks.constraints.NoSelfLoops;
import openmarkov.networks.constraints.OnlyChanceNodes;
import openmarkov.networks.constraints.OnlyUndirectedLinks;
import openmarkov.networks.constraints.ValidLimitValue;

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

		constraints.add(ValidName.getUniqueInstance());
		constraints.add(ValidState.getUniqueInstance());
		constraints.add(ValidLimitValue.getUniqueInstance());
	}

	// Methods
	public static MNConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new MNConstraint();
		}
		return constraint;
	}
	
	
	
}
