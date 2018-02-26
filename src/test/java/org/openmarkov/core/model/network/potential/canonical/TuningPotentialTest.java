/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Variable;

public class TuningPotentialTest
{
    
    private double admissibleError = 0.000000001;

    private TuningPotential tuningModelPotential;
    
    
    // Initialization
    @Before
    public void setUp() throws Exception {
        
        // Define the variables
        Variable dT = new Variable("dT", "down", "st.quo", "up");
        Variable dM = new Variable("dM", "down", "st.quo", "up");
        Variable dG = new Variable("dG", "down", "st.quo", "up");
        Variable nerveSoft = new Variable("Nerve_Soft", "Softer", "St.quo", "Louder");

        List<Variable> variables = new ArrayList<>();
        variables.add (nerveSoft);
        variables.add (dT);
        variables.add (dM);
        variables.add (dG);
        
        tuningModelPotential = new TuningPotential (variables);

        tuningModelPotential.setNoisyParameters(dT, new double[] {1.0, 0.0, 0.0, 1.0});
        tuningModelPotential.setNoisyParameters(dM, new double[] {0.1, 0.2, 0.2, 0.1});
        tuningModelPotential.setNoisyParameters(dG, new double[] {1.0, 0.0, 0.0, 1.0});
    }    
    
    @Test
    public void testGetCPT() throws NonProjectablePotentialException, WrongCriterionException {
        double[] cPTValues = tuningModelPotential.getCPT().values;
        assertEquals(1.0, cPTValues[0], admissibleError);
        assertEquals(0.0, cPTValues[1], admissibleError);
        assertEquals(0.8, cPTValues[3], admissibleError);
        assertEquals(0.2, cPTValues[4], admissibleError);
        assertEquals(1.0, cPTValues[18], admissibleError);
        assertEquals(1.0, cPTValues[80], admissibleError);
    }    
}
