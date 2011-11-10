package org.openmarkov.core.mdp;

public class MDPFiniteHorizonTablePolicy extends MDPFiniteHorizonPolicy {
	
	MDPTablePolicy []timeSlots;
	
	public MDPFiniteHorizonTablePolicy (int horizonLength, MDPVariable mdpVariable, MDPVariable mdpActions ) {
		timeSlots= new MDPTablePolicy [horizonLength];
		
		for (int i=0; i< horizonLength; i++) {
			timeSlots[i]= new MDPTablePolicy (mdpVariable, mdpActions);
		}
	}
	
	@Override
	public MDPPolicy getTimeSlicePolicy(int timeSlot) {
		// TODO Auto-generated method stub
		return timeSlots[timeSlot];
	}

	@Override
	public void setTimeSlicePolicy(int timeSlot, MDPPolicy mdpPolicy) {
		assert (mdpPolicy instanceof MDPTablePolicy);
		
		// TODO Auto-generated method stub
		timeSlots[timeSlot]= (MDPTablePolicy) mdpPolicy;
	}

}
