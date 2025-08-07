package org.openmarkov.core.exception;

import java.util.Arrays;
import java.util.List;

public class IntervalsAreNotMultipleOf3Exception extends BundledOpenMarkovException {
    
    public final List<Double> values;
    public final int lenghtOfCycle;
    
    
    public IntervalsAreNotMultipleOf3Exception(double[] values, int lenghtOfCycle) {
        this.values = Arrays.stream(values).boxed().toList();
        this.lenghtOfCycle = lenghtOfCycle;
    }
}
