package org.openmarkov.core.inference;

public class MulticriteriaOptions implements Cloneable{

	public enum Type {
	    UNICRITERION,
	    COST_EFFECTIVENESS
	}
	
	private Type multicriteriaType;
	
	private String mainUnit;
	
	public MulticriteriaOptions() {
		this.mainUnit = " ";
		this.multicriteriaType = Type.UNICRITERION;
	}
	
	public MulticriteriaOptions(Type multicriteriaType, String mainUnit){
		this.multicriteriaType = multicriteriaType;
		this.mainUnit = mainUnit;
	}

	public MulticriteriaOptions(MulticriteriaOptions multiCriteriaOptions) {
		this.multicriteriaType = multiCriteriaOptions.getMulticriteriaType();
		this.mainUnit = multiCriteriaOptions.getMainUnit();
	}

	public Type getMulticriteriaType() {
		return multicriteriaType;
	}

	public void setMulticriteriaType(Type multicriteriaType) {
		this.multicriteriaType = multicriteriaType;
	}

	public String getMainUnit() {
		return mainUnit;
	}

	public void setMainUnit(String mainUnit) {
		this.mainUnit = mainUnit;
	}
	
	public MulticriteriaOptions clone(){
		return new MulticriteriaOptions(this);
	}
	
}
