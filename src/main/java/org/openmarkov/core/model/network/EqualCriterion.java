/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;


/**
 * A criterion has a name and the units of measure, subclass equal due to I don't know how equality should be treated in other netorks
 * TODO Implement equal method in Criterion or change the name of this class
 *
 * @author cyago
 * @version 1.0 implemented comparable
 * @version 2.0 25/04/2020 implements equal method. I need compare equality no order. Refactored from ComparableCriterion
 */
public class EqualCriterion extends Criterion {

	/**
	 * Constant with the default unit of a criterion
	 * Copied from Criterion because it is private
	 */
	private final static String defaultUnit = "---";


	/**
	 * Constructor with only one parameter
	 *
	 * @param criterionName Name of the criterion
	 */
	public EqualCriterion(String criterionName) {
		super(criterionName, defaultUnit);
	}



	public EqualCriterion(Criterion  criterion) {
		super(criterion);
	}


	/**
	 * Indicates whether some other object is "equal to" this one.
	 * Two EqualCriterion are equal if they have the same name
	 */
	@Override
	public boolean equals(Object obj) {
//		String s =this.getCriterionName();
//		String s1 = ((EqualCriterion)obj).getCriterionName();
		boolean result = this.getCriterionName().equals(((EqualCriterion)obj).getCriterionName());
		return result;
	}

	/**
	 * For using HashMap I need the same hashcode
	 * @return 1
	 */
	@Override
	public int hashCode() {
		return 1;
	}


}
