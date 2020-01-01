/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import java.io.File;

/**
 * This class contains the parameter for doing Monte Carlo simulations
 * @author cyago
 * @version 1.0 25/08/2019
 * @version 1.1 25/09/2019 -added log options
 * @version 1.2 16/12/2019 -added calculation and file options
 */
public class MonteCarloOptions implements Cloneable {

    //Simulation Options
	private int numSimulations = 1;
	private int numTrialSets =1;


	//Log options
	private boolean onlySummary = true;
	private boolean stateLog =false;
	private boolean eventLog =false;
	private boolean scheduledEventLog =false;

	//Result options
    private boolean mean = true;
    private boolean trimmedMean = true;
    private boolean median = false;
	private boolean sum = false;


	//File options
	private String inputFileName = "";
	private File inputFile;


	public MonteCarloOptions() {
	}

	public MonteCarloOptions(MonteCarloOptions monteCarloOptions) {
		this.setNumSimulations(monteCarloOptions.numSimulations);
		this.setNumTrialSets(monteCarloOptions.numTrialSets);
		this.setOnlySummary(monteCarloOptions.isOnlySummary());
		this.stateLog = monteCarloOptions.isStateLog();
		this.eventLog = monteCarloOptions.isEventLog();
		this.scheduledEventLog = monteCarloOptions.isScheduledEventLog();
		this.mean= monteCarloOptions.isMean();
		this.trimmedMean = monteCarloOptions.isTrimmedMean();
		this.median = monteCarloOptions.isMedian();

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

	/**
	 * This method returns true if any of the one-simulation level log options is set
	 * @return
	 */
	public boolean isOneSimulationLevelLog(){
		return (stateLog|| eventLog || scheduledEventLog);
	}

	public boolean isOnlySummary() {
		return onlySummary;
	}

	public void setOnlySummary(boolean onlySummary) {
		this.onlySummary = onlySummary;
	}

    public boolean isMean() {
        return mean;
    }

    public void setMean(boolean mean) {
        this.mean = mean;
    }

    public boolean isTrimmedMean() {
        return trimmedMean;
    }

    public void setTrimmedMean(boolean trimmedMean) {
        this.trimmedMean = trimmedMean;
    }

    public boolean isMedian() {
        return median;
    }

    public void setMedian(boolean median) {
        this.median = median;
    }
	public boolean isSum() { return sum;}
    public void setSum(boolean sum) {
        this.sum = sum;
    }

    public boolean getSum() {
		return sum;
    }

    //Input File Options

	public File getInputFile() {
		return inputFile;
	}

	/**
	 * Sets the File which will be used for inference and its filename
	 * @param inputFile
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
		this.inputFileName = inputFile.getAbsolutePath();
	}

	public String getInputFileName() {
		return inputFileName;
	}


}
