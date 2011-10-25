package org.openmarkov.core.model.network.constraint.compound;

import openmarkov.gui.constraints.ValidName;
import openmarkov.gui.constraints.ValidState;
import openmarkov.networks.constraints.AllChanceVariablesHaveChancePotentials;
import openmarkov.networks.constraints.NoCycles;
import openmarkov.networks.constraints.NoSelfLoops;
import openmarkov.networks.constraints.OnlyDirectedLinks;
import openmarkov.networks.constraints.UtilityNodes;
import openmarkov.networks.constraints.ValidLimitValue;

public class IDConstraint extends NetworkTypeConstraint {

	// Attributes
	private static IDConstraint constraint = null;
	
	// Constructor
	private IDConstraint() {
		constraints.add(NoCycles.getUniqueInstance());
		constraints.add(
				AllChanceVariablesHaveChancePotentials.getUniqueInstance());
		constraints.add(NoSelfLoops.getUniqueInstance());
		constraints.add(OnlyDirectedLinks.getUniqueInstance());
		constraints.add(UtilityNodes.getUniqueInstance());
		constraints.add(ValidName.getUniqueInstance());
		constraints.add(ValidState.getUniqueInstance());
		constraints.add(ValidLimitValue.getUniqueInstance());
	}

	// Methods
	public static IDConstraint getUniqueInstance() {
		if (constraint == null) {
			constraint = new IDConstraint();
		}
		return constraint;
	}
	
}
