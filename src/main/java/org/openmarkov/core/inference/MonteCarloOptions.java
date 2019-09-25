/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

/**
 * This class contains the parameter for doing Monte Carlo simulations
 * @author cyago
 * @version 1.0 25/08/2019
 * @version 1.1 25/09/2019 -added log options
 */
public class MonteCarloOptions implements Cloneable {


	private int numSimulations;
	private int numTrialSets;

	private boolean stateLog =false;
	private boolean eventLog =false;
	private boolean scheduledEventLog =false;




	public MonteCarloOptions() {
		// Number of simulations that will be carried out per serie
		numSimulations = 1; // Because a newly created net has 0 slices
		// Number of series
		numTrialSets = 1;
	}

	public MonteCarloOptions(MonteCarloOptions monteCarloOptions) {
		this.setNumSimulations(monteCarloOptions.numSimulations);
		this.setNumTrialSets(monteCarloOptions.numTrialSets);
		this.stateLog = monteCarloOptions.isStateLog();
		this.eventLog = monteCarloOptions.isEventLog();
		this.scheduledEventLog = monteCarloOptions.isScheduledEventLog();
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

	public int getNumTrialSets() {
		return numTrialSets;
	}

	public void setNumTrialSets(int numTrialSets) {
		this.numTrialSets = numTrialSets;
	}


	public boolean isStateLog() {
		return stateLog;
	}

	public void setStateLog(boolean stateLog) {
		this.stateLog = stateLog;
	}

	public boolean isEventLog() {
		return eventLog;
	}

	public void setEventLog(boolean eventLog) {
		this.eventLog = eventLog;
	}

	public boolean isScheduledEventLog() {
		return scheduledEventLog;
	}

	public void setScheduledEventLog(boolean scheduledEventLog) {
		this.scheduledEventLog = scheduledEventLog;
	}
	
	
	
}
