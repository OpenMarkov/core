package org.openmarkov.core.model.network;

import org.openmarkov.core.model.network.CycleLength.Unit;

/**
 * A criterion has a name and the units of measure
 * @author jperez
 *
 */
public class Criterion implements Cloneable {

	/**
	 * Emum with the values of Cost and Effectiveness for the CE Analysis
	 *
	 */
	public enum CECriterion {
	    Null,
		Cost,
	    Effectiveness
	}
	
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
    private final static String defaultCriterion = "---";
    
    /**
     * Constant with the default unit of a criterion
     */
    private final static String defaultUnit = " ";
    
    /**
     * In the unicriteria analysis, the scale of this criterion above the main criterion choosed
     */
    private double unicriteriaScale;
    
    /**
     * In the cost-effectiveness analysis, the scale of this criterion
     */
    private double ceScale;
    
    /**
     * In the cost-effectiveness, specifies if the criterion acts as a cost or as effectiveness 
     */
    private CECriterion ceCriterion;
    
    /**
     * In temporal evolution analysis, the rate of discount of the criterion
     */
    private double discount;
    
    /**
     * In temporal evolution analysis, the measure units for the discount of the criterion
     */
    private CycleLength.DiscountUnit discountUnit;
    
	/**
	 * Constructor with parameters
	 * @param criterionName Name of the criterion
	 * @param criterionUnit Units of measure
	 */
	public Criterion (String criterionName, String criterionUnit){
		this.criterionName = criterionName;
		this.criterionUnit = criterionUnit;
		this.discount = 0;
		this.unicriteriaScale = 1;
		this.ceScale = 1;
		this.discountUnit = CycleLength.DiscountUnit.YEAR;
		this.ceCriterion = CECriterion.Null;
	}
	
	/**
	 * Constructor with only one parameter
	 * @param criterionName Name of the criterion
	 */
	public Criterion (String criterionName){
		this(criterionName, defaultUnit);
	}
	
	/**
	 * Empty constructor, this creates the default criterion
	 */
	public Criterion(){
		this(defaultCriterion, defaultUnit);
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
	
	public double getUnicriteriaScale() {
		return unicriteriaScale;
	}

	public void setUnicriteriaScale(double scale) {
		this.unicriteriaScale = scale;
	}

	public double getCeScale() {
		return ceScale;
	}

	public void setCeScale(double ceScale) {
		this.ceScale = ceScale;
	}

	public CECriterion getCECriterion() {
		return ceCriterion;
	}

	public void setCECriterion(CECriterion ce_criterion) {
		this.ceCriterion = ce_criterion;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public CycleLength.DiscountUnit getDiscountUnit() {
		return this.discountUnit;
	}

	public void setDiscountUnit(CycleLength.DiscountUnit discountUnit) {
		this.discountUnit = discountUnit;
	}
	
	
	
	@Override
	public String toString() {
		return criterionName + " " + criterionUnit;
	}


	/**
	 * Gets a copy of the criterion in a new object
	 * @return copied criterion
	 */
	public Criterion clone() {
		Criterion criterion = new Criterion(this.criterionName, this.criterionUnit);
		criterion.setCECriterion(this.getCECriterion());
		criterion.setDiscount(this.getDiscount());
		criterion.setUnicriteriaScale(this.getUnicriteriaScale());
		criterion.setCeScale(this.getCeScale());
		criterion.setDiscountUnit(this.getDiscountUnit());
		return criterion;
	}

	/**
	 * Copy the attributes of the new criterion in the current object
	 * @param newCriterion
	 */
	public void copy(Criterion newCriterion) {
		this.ceCriterion = newCriterion.getCECriterion();
		this.criterionName = newCriterion.getCriterionName();
		this.criterionUnit = newCriterion.getCriterionUnit();
		this.discount = newCriterion.getDiscount();
		this.discountUnit = newCriterion.getDiscountUnit();
		this.unicriteriaScale = newCriterion.getUnicriteriaScale();
		this.ceScale = newCriterion.getCeScale();
	}

	
	

	
	
	
	
}
