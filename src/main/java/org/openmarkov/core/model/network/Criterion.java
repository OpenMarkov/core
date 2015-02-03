package org.openmarkov.core.model.network;

import org.openmarkov.core.model.network.TemporalUnit.Unit;

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
	public enum CostEffectivenessType {
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
    private double scale;
    
    /**
     * In the cost-effectiveness, specifies if the criterion acts as a cost or as a effectiveness 
     */
    private CostEffectivenessType ce_criterion;
    
    /**
     * In temporal evolution analysis, the rate of discount of the criterion
     */
    private double discount;
    
    /**
     * In temporal evolution analysis, the measure units for the discount of the criterion
     */
    private TemporalUnit.Unit discountUnit;
    
	/**
	 * Constructor with parameters
	 * @param criterionName Name of the criterion
	 * @param criterionUnit Units of measure
	 */
	public Criterion (String criterionName, String criterionUnit){
		this.criterionName = criterionName;
		this.criterionUnit = criterionUnit;
		this.discount = 0;
		this.scale = 1;
		this.discountUnit = TemporalUnit.Unit.YEAR;
		this.ce_criterion = CostEffectivenessType.Null;
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
	
	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public CostEffectivenessType getCe_criterion() {
		return ce_criterion;
	}

	public void setCe_criterion(CostEffectivenessType ce_criterion) {
		this.ce_criterion = ce_criterion;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public TemporalUnit.Unit getDiscountUnit() {
		return this.discountUnit;
	}

	public void setDiscountUnit(TemporalUnit.Unit discountUnit) {
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
		criterion.setCe_criterion(this.getCe_criterion());
		criterion.setDiscount(this.getDiscount());
		criterion.setScale(this.getScale());
		criterion.setDiscountUnit(this.getDiscountUnit());
		return criterion;
	}

	
	

	
	
	
	
}
