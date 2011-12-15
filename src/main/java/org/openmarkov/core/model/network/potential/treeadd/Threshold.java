package org.openmarkov.core.model.network.potential.treeadd;

public class Threshold {
	
	/*
	 * A threshold is defined by a float value and a boolean that indicates if the value delimits a closed or an open interval
	 * on the left and on the right side. There are two possibilities )[, ](	
	 */
	protected float limit;
	protected boolean belongsToLeft; // if false --> )[ ; if true --> ](
	
	
	public Threshold(float limit, boolean belongsToLeft){
		this.limit = limit;
		this.belongsToLeft = belongsToLeft;
		
	}
	
	public float getLimit() {
		return this.limit;
	}
	
		
	/**@return true if the value is above the limit value of the threshold object
	 * @param value to check
	 * **/
	public boolean isBelow(float value){
		//if (value > this.limit || (value == this.limit && !this.belongsToLeft)) {
		if (value > this.limit){
			return true;
		}else{ return false; }
	}
	
	/**@return true if the value is below the limit value of the threshold object
	 * @param value to check
	 * **/
	public boolean isAbove(float value){
		//if (value < this.limit || (value == this.limit && this.belongsToLeft)) {
		if (value < this.limit){
			return true;
		}else{ return false; }
	}
	

}
