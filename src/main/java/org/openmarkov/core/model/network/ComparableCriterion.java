/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

/**
 * A criterion has a name and the units of measure, subclass to implement Comparable.
 *
 * @author cyago
 * @version 1.0
 */
public class ComparableCriterion extends Criterion implements Comparable<ComparableCriterion> {

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
	public ComparableCriterion(String criterionName) {
		super(criterionName, defaultUnit);
	}



	public ComparableCriterion(Criterion  criterion) {
		super(criterion);
	}

	@Override
	public int compareTo(ComparableCriterion o) {
		return (o.getCriterionName().compareTo(this.getCriterionName()));
	}



}
