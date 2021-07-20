/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Enumerated class with deterministic axis variation types
 */
public enum DeterministicAxisVariationType {

	// Axis variation types for deterministic analysis

	/**
	 * Percentage of the parameter probability
	 */
	POPP("POPP"),

	/**
	 * Same variation as the X axis
	 */
	PREV("PREV"),

	/**
	 * Percentage over reference value
	 */
	PORV("PORV"),

	/**
	 * Ratio over reference value
	 */
	RORV("RORV"),

	/**
	 * User defined interval between 0 and 1
	 */
	UDIN("UDIN");

	private final String display;
	
	private final String displaySpiderLegend;
	
	DeterministicAxisVariationType(String suffix) {
		String prefix = "SensitivityAnalysis.Axis.";
		this.display = prefix + "Variation." +suffix;
		this.displaySpiderLegend = prefix + "VariationSpiderLegend." + suffix;
	}

	@Override public String toString() {
		return display;
	}
	
	public String toStringSpiderLegend() {
		return displaySpiderLegend;
	}
}

