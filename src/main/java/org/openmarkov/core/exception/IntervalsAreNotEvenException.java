package org.openmarkov.core.exception;

import java.util.Arrays;
import java.util.List;

public class IntervalsAreNotEvenException extends BundledOpenMarkovException {
    
    public final List<Double> values;
    public final int lenghtOfCycle;
    
    public IntervalsAreNotEvenException(double[] values, int lenghtOfCycle) {
        this.values = Arrays.stream(values).boxed().toList();
        this.lenghtOfCycle = lenghtOfCycle;
    }
    
}
