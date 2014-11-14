package org.openmarkov.core.model.network;

/**
 * A criterion has a name and the units of measure
 * @author jperez
 *
 */
public class Criterion {

	/**
	 * Name of the criterion
	 */
	private String criterionName;
	
	/**
	 * Units of measure
	 */
	private String criterionUnit;
	
    /**
     * Constant with the default criterion of a ProbNet
     */
    private final String defaultCriterion = "benefit";
    
    /**
     * Constant with the default unit of a criterion
     */
    private final String defaultUnit = "";
	
	/**
	 * Constructor with parameters
	 * @param criterionName Name of the criterion
	 * @param criterionUnit Units of measure
	 */
	public Criterion (String criterionName, String criterionUnit){
		this.criterionName = criterionName;
		this.criterionUnit = criterionUnit;
	}
	
	/**
	 * Constructor with only one parameter
	 * @param criterionName Name of the criterion
	 */
	public Criterion (String criterionName){
		this.criterionName = criterionName;
		this.criterionUnit = defaultUnit;
	}
	
	/**
	 * Empty constructor, this creates the default criterion
	 */
	public Criterion(){
		this.criterionName = defaultCriterion;
		this.criterionUnit = defaultUnit;
	}

	public String getCriterionName() {
		return criterionName;
	}

	public void setCriterionName(String criterionName) {
		this.criterionName = criterionName;
	}

	public String getCriterionUnit() {
		return criterionUnit;
	}

	public void setCriterionUnit(String criterionUnit) {
		this.criterionUnit = criterionUnit;
	}

	public String getDefaultCriterion() {
		return defaultCriterion;
	}

	@Override
	public String toString() {
		return criterionName + " " + criterionUnit;
	}
	
	
	
}
