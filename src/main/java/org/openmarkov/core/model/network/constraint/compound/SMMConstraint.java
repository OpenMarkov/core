package org.openmarkov.core.model.network.constraint.compound;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.NoCycles;
import org.openmarkov.core.model.network.constraint.NoSelfLoops;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;
import org.openmarkov.core.model.network.constraint.ValidLimitValue;

public class SMMConstraint extends NetworkTypeConstraint {
			// Attributes
		private static SMMConstraint constraint = null;
		
		// Constructor
		private SMMConstraint() {
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
		public static SMMConstraint getUniqueInstance() {
			if (constraint == null) {
				constraint = new SMMConstraint();
			}
			return constraint;
		}
		
}


