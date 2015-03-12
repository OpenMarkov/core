package org.openmarkov.core.model.network;

public class TemporalUnit {

	private final double DEFAULT_SCALE = 1;
	
	private final Unit DEFAULT_UNIT = Unit.YEAR;
	
	/**
	 * Possible units
	 *
	 */
	public static enum Unit {
		YEAR,
		MONTH,
		WEEK,
		DAY,
		HOUR,
		MINUTE,
		SECOND,
		MILISECOND
	}
	
	/**
	 * Cycles of each unit in a year. The order must be the same as in the units (Year, Month, Week, ...)
	 */
	static double [] cyclesInAYear = {1, 12, 52, 365, 8760, 525600, 31536000, 31536000E3};
	
	/**
	 * Temporal units of discounts
	 *
	 */
	public static enum DiscountUnit {
		YEAR,
		CYCLE
	}

	/**
	 * Selected unit
	 */
	private Unit unit;
	
	/**
	 * Scale of the unit
	 */
	private double scale;
	
	public TemporalUnit(){
		this.unit = DEFAULT_UNIT;
		this.scale = DEFAULT_SCALE;
	}
	
	public TemporalUnit(Unit unit){
		this.unit = unit;
		this.scale = DEFAULT_SCALE;
	}
	
	public TemporalUnit(Unit unit, double scale){
		this.unit = unit;
		this.scale = scale;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public TemporalUnit clone() {
		TemporalUnit temporalUnit = new TemporalUnit(this.unit, this.scale);
		return temporalUnit;
	}
	
	/**
	 * Get the adjusted discount in cycles
	 * @param cycleUnit ProbNet cycle selected unit
	 * @param cycleLength ProbNet cycle length
	 * @param unitToBeConverted Actual discount unit
	 * @param discount Value of the discount
	 * @return Discount per cycle length
	 */
	public static double getTemporalAdjustedDiscount(Unit cycleUnit, double cycleLength, DiscountUnit unitToBeConverted, double discount){
		if(unitToBeConverted.equals(DiscountUnit.YEAR)){
			double rate = cyclesInAYear[cycleUnit.ordinal()]/cycleLength;
			double newDiscount = Math.pow(1 + discount, 1/rate) - 1.0;
			return newDiscount;
		}else { // if(unitToBeConverted.equals(DiscountUnit.CYCLE)){
			return discount;
		}
	}
	
	
}
