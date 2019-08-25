/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

/**
 * This class contains the parameter for doing a Monte Carlo simulation
 * @author cyago
 * @version 1.0 25/08/2019
 */
public class MonteCarloOptions implements Cloneable {

	private int numSimulations;
	private int numSeries;


	public MonteCarloOptions() {
		// Number of simulations that will be carried out per serie
		numSimulations = 1; // Because a newly created net has 0 slices
		// Number of series
		setNumSeries(1);
	}

	public MonteCarloOptions(MonteCarloOptions monteCarloOptions) {
		this.setNumSimulations(monteCarloOptions.numSimulations);
		this.setNumSeries(monteCarloOptions.numSeries);
	}

	public int getNumSimulations() {
		return numSimulations;
	}

	public void setNumSimulations(int numSimulations) {
		this.numSimulations = numSimulations;
	}

	public MonteCarloOptions clone() {
		return new MonteCarloOptions(this);
	}

	public int getNumSeries() {
		return numSeries;
	}

	public void setNumSeries(int numSeries) {
		this.numSeries = numSeries;
	}
}
