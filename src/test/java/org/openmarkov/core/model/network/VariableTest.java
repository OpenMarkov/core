/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class VariableTest {
    
    private final int numStates = 3;
    private final String x = "X";
    private final String[] statesNames = {"0", "1", "2"};
    private Variable variable;
    
    /**
     * Compares variable1 and variable2.
     *
     * @param variable1 <code>Variable</code>
     * @param variable2 <code>Variable</code>
     * @return <code>true</code> if both variables are equal.
     */
    public static boolean equalVariables(Variable variable1, Variable variable2) {
        boolean equals = true;
        // TODO Usar en Variable.equals: if (!super.equals(variableReceived)) {...}
        if (variable1.getVariableType() == variable2.getVariableType() && variable1.isTemporal() == variable2
                .isTemporal() && variable1.getNumStates() == variable2.getNumStates() && variable1.getBaseName()
                                                                                                  .contentEquals(variable2.getBaseName()) && variable1.getName()
                                                                                                                                                      .contentEquals(variable2.getName())) {
            State[] states1 = variable1.getStates();
            State[] states2 = variable2.getStates();
            int i = 0;
            while (i < states1.length && equals) {
                if (!StateTest.equalStates(states1[i], states2[i])) {
                    equals = false;
                }
                i++;
            }
            if (equals) {
                PartitionedInterval interval1 = variable1.getPartitionedInterval();
                PartitionedInterval interval2 = variable2.getPartitionedInterval();
                
                equals = (interval1 == null && interval2 == null) || (interval1 != null && interval2 != null);
                if (equals && interval1 != null) {
                    equals = interval1.equals(interval2);
                }
            }
        }
        return equals;
    }
    
    @BeforeEach public void setUp() {
        variable = new Variable(x, numStates);
    }
    
    // Test constructors
    @Test public void testDiscreteConstructor2() {
        Variable variable = new Variable(x, numStates);
        State[] states = variable.getStates();
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(new String("" + i)));
        }
    }
    
    @Test public void testFSVariableStringInt() {
        Variable variable = new Variable(x, numStates);
        assertTrue(variable.getName().contains(x));
        assertEquals(numStates, variable.getNumStates());
        State[] states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
    }
    
    @Test public void testRenameState() throws Exception {
        // Rename not existing state (it does nothing)
        Variable variable = new Variable(x, numStates);
        boolean exceptionLaunched = false;
        try {
            variable.renameState("NoExists", "Yahoo");
            fail();
        } catch (Exception e) {
            //It should throw the exception in order to pass the test
        }
        State[] states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
        // Rename one state
        String newName = "Yahoo";
        String oldName = states[numStates - 1].getName();
        variable.renameState(oldName, newName);
        states = variable.getStates();
        assertEquals(numStates, states.length);
        for (int i = 0; i < numStates - 1; i++) {
            assertTrue(states[i].getName().contentEquals(statesNames[i]));
        }
        assertTrue(states[numStates - 1].getName().contentEquals(newName));
    }
    
    // TODO Sobrecargar equals y poner en el comentario que
    // equals ya NO consiste en comparar la dirección de memoria de dos objetos
    // TODO Cada tipo de variable tiene que tener un método equals y llamar al del padre
    
    @Test public void testGetStateIndex() {
        for (int i = 0; i < numStates; i++) {
            assertEquals(i, variable.getStateIndex(statesNames[i]));
        }
    }
    
}
