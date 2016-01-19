package org.openmarkov.core.model.network.modelUncertainty;

/**
 * Created by Jorge on 30/06/2015.
 */
public enum DeterministicAxisVariationType {

    // Axis variation types for deterministic analysis

    /**
     * Percentage of the parameter probability
     */
    POPP ("Options.VT_POPP"),

    /**
     * Same variation as the X axis
     */
    PREV ("Options.VT_PREV"),

    /**
     * Percentage over reference value
     */
    PORV ("Options.VT_PORV"),

    /**
     * Ratio over reference value
     */
    RORV ("Options.VT_RORV"),

    /**
     * User defined interval between 0 and 1
     */
    UDIN ("Options.VT_UDIN");

    private final String display;

    DeterministicAxisVariationType(String display) {
        this.display = display;
    }


    @Override
    public String toString() {
        return display;
    }
}

