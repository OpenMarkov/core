package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Enumerated class with deterministic axis variation types
 */
public enum DeterministicAxisVariationType {

    // Axis variation types for deterministic analysis

    /**
     * Percentage of the parameter probability
     */
    POPP ("SensitivityAnalysis.Axis.Variation.POPP"),

    /**
     * Same variation as the X axis
     */
    PREV ("SensitivityAnalysis.Axis.Variation.PREV"),

    /**
     * Percentage over reference value
     */
    PORV ("SensitivityAnalysis.Axis.Variation.PORV"),

    /**
     * Ratio over reference value
     */
    RORV ("SensitivityAnalysis.Axis.Variation.RORV"),

    /**
     * User defined interval between 0 and 1
     */
    UDIN ("SensitivityAnalysis.Axis.Variation.UDIN");

    private final String display;

    DeterministicAxisVariationType(String display) {
        this.display = display;
    }


    @Override
    public String toString() {
        return display;
    }
}

