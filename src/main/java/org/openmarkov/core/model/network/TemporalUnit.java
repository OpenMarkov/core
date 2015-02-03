package org.openmarkov.core.model.network;

public class TemporalUnit {

	private final double DEFAULT_SCALE = 1;
	
	private final Unit DEFAULT_UNIT = Unit.YEAR;
	
	public static enum Unit {
		YEAR,
		MONTH,
		WEEK,
		DAY,
		HOUR,
		MINUTE,
		SECOND,
		MILISECOND
	};

	private Unit unit;
	
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
	
	
}
