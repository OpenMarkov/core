/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.model.network.potential.canonical;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.Variable;

public class TuningModelPotentialTest
{
    
    private double admissibleError = 0.000000001;

    private TuningModelPotential tuningModelPotential;
    
    
    // Initialization
    @Before
    public void setUp() throws Exception {
        
        // Define the variables
        Variable dT = new Variable("dT", "down", "st.quo", "up");
        Variable dM = new Variable("dM", "down", "st.quo", "up");
        Variable dG = new Variable("dG", "down", "st.quo", "up");
        Variable nerveSoft = new Variable("Nerve_Soft", "Softer", "St.quo", "Louder");

        ArrayList<Variable> variables = new ArrayList<Variable> ();
        variables.add (nerveSoft);
        variables.add (dT);
        variables.add (dM);
        variables.add (dG);
        
        tuningModelPotential = new TuningModelPotential (variables);

        tuningModelPotential.setNoisyParameters(dT, new double[] {1.0, 0.0, 0.0, 1.0});
        tuningModelPotential.setNoisyParameters(dM, new double[] {0.1, 0.2, 0.2, 0.1});
        tuningModelPotential.setNoisyParameters(dG, new double[] {1.0, 0.0, 0.0, 1.0});
    }    
    
    @Test
    public void testGetCPT() throws NotEnoughMemoryException, NonProjectablePotentialException, WrongCriterionException {
        double[] cPTValues = tuningModelPotential.getCPT().values;
        assertEquals(1.0, cPTValues[0], admissibleError);
        assertEquals(0.0, cPTValues[1], admissibleError);
        assertEquals(0.8, cPTValues[3], admissibleError);
        assertEquals(0.2, cPTValues[4], admissibleError);
        assertEquals(1.0, cPTValues[18], admissibleError);
        assertEquals(1.0, cPTValues[80], admissibleError);
    }    
}
