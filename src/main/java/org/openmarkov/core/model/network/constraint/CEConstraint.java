package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.model.network.constraint.compound.NetworkTypeConstraint;

public class CEConstraint extends NetworkTypeConstraint {

	// Attributes
	private static CEConstraint constraint = null;
	
	// Constructor
	private CEConstraint() throws ConstraintViolationException {
		constraints.add(NoCycles.getUniqueInstance());
		constraints.add(
				AllChanceVariablesHaveChancePotentials.getUniqueInstance());
		constraints.add(NoSelfLoops.getUniqueInstance());
		constraints.add(OnlyDirectedLinks.getUniqueInstance());
		constraints.add(UtilityNodes.getUniqueInstance());
		constraints.add(AtLeastOneCostPotential.getUniqueInstance());
		constraints.add(AtLeastOneEffectivenessPotential.getUniqueInstance());
	}

	// Methods
	public static CEConstraint getUniqueInstance() 
			throws ConstraintViolationException {
		if (constraint == null) {
			constraint = new CEConstraint();
		}
		return constraint;
	}
	


}
