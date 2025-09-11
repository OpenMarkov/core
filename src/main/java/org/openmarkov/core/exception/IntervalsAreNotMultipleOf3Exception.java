package org.openmarkov.core.exception;

import java.util.Arrays;
import java.util.List;

public class IntervalsAreNotMultipleOf3Exception extends Exception implements IBundledOpenMarkovException {
    
    public final List<Double> values;
    public final int lenghtOfCycle;
    
    //TODO: Does this really happen in the GUI? It might be a RuntimeException.
    public IntervalsAreNotMultipleOf3Exception(double[] values, int lenghtOfCycle) {
        this.values = Arrays.stream(values).boxed().toList();
        this.lenghtOfCycle = lenghtOfCycle;
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
}
