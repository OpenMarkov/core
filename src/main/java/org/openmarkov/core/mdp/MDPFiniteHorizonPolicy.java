package org.openmarkov.core.mdp;

public abstract class MDPFiniteHorizonPolicy {

	public abstract MDPPolicy getTimeSlicePolicy(int timeSlot);

	public abstract void setTimeSlicePolicy(int timeSlot, MDPPolicy mdpPolicy);
}
