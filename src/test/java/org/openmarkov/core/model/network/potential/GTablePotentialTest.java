/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GTablePotential}.
 *
 * <p>Covers constructor behaviour (sizes, invariants) and the known side-effect
 * that {@code values[]} is allocated but always stays all-zero — an artefact of
 * extending {@link TablePotential} while not using the double array.
 * This is relevant for the planned GTablePotential re-parenting (Rediseño 1,
 * phase 5).
 *
 * @author Manuel Arias
 */
class GTablePotentialTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Variable var(String name, int numStates) {
        State[] states = new State[numStates];
        for (int i = 0; i < numStates; i++) states[i] = new State("s" + i);
        return new Variable(name, states);
    }

    // ------------------------------------------------------------------
    // Constructor: single variable
    // ------------------------------------------------------------------

    @Test
    void singleVariableConstructorCreatesCorrectSizeElementTable() {
        Variable v = var("X", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(v));
        // elementTable initial capacity == tableSize == 3
        // It is empty (no elements added yet)
        assertEquals(0, g.elementTable.size());
        // But we can add exactly tableSize elements
        g.elementTable.add("a");
        g.elementTable.add("b");
        g.elementTable.add("c");
        assertEquals(3, g.elementTable.size());
    }

    @Test
    void singleVariableConstructorAllocatesValuesArrayAllZero() {
        // Known side-effect: super(variables, null) allocates values[] with tableSize entries.
        Variable v = var("X", 4);
        GTablePotential<String> g = new GTablePotential<>(List.of(v));
        // values[] must be allocated (not null)
        assertNotNull(g.values);
        assertEquals(4, g.values.length, "values[] length should equal tableSize");
        for (double d : g.values) {
            assertEquals(0.0, d, "GTablePotential.values[] must be all-zero (unused)");
        }
    }

    // ------------------------------------------------------------------
    // Constructor: two variables
    // ------------------------------------------------------------------

    @Test
    void twoVariableConstructorHasCorrectTableSize() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(x, y));
        // tableSize = 2 * 3 = 6
        assertNotNull(g.values);
        assertEquals(6, g.values.length);
    }

    @Test
    void twoVariableConstructorElementTableHasCorrectCapacity() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(x, y));
        // Fill elementTable to capacity (tableSize)
        for (int i = 0; i < 6; i++) g.elementTable.add(i);
        assertEquals(6, g.elementTable.size());
    }

    // ------------------------------------------------------------------
    // Constructor: three variables
    // ------------------------------------------------------------------

    @Test
    void threeVariableConstructorHasCorrectTableSize() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        Variable z = var("Z", 4);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y, z));
        assertEquals(24, g.values.length, "2*3*4 = 24");
    }

    // ------------------------------------------------------------------
    // Constructor: null / empty variables
    // ------------------------------------------------------------------

    @Test
    void nullVariablesConstructorCreatesConstantPotential() {
        GTablePotential<String> g = new GTablePotential<>((List<Variable>) null);
        assertNotNull(g.elementTable);
        assertEquals(0, g.elementTable.size()); // capacity = 1 but no elements added
    }

    @Test
    void emptyVariablesConstructorCreatesConstantPotential() {
        GTablePotential<String> g = new GTablePotential<>(List.of());
        assertNotNull(g.elementTable);
        assertEquals(0, g.elementTable.size());
    }

    // ------------------------------------------------------------------
    // Constructor with role and pre-built elementTable
    // ------------------------------------------------------------------

    @Test
    void roleAndElementTableConstructorSetsElementTable() {
        Variable v = var("X", 2);
        List<String> table = List.of("alpha", "beta");
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertSame(table, g.elementTable);
    }

    @Test
    void roleAndElementTableConstructorStillHasZeroValuesArray() {
        Variable v = var("X", 2);
        List<Integer> table = List.of(10, 20);
        GTablePotential<Integer> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertEquals(2, g.values.length);
        assertEquals(0.0, g.values[0]);
        assertEquals(0.0, g.values[1]);
    }

    // ------------------------------------------------------------------
    // Invariant: values[] always all-zero after element writes
    // ------------------------------------------------------------------

    @Test
    void writingToElementTableDoesNotChangeValuesArray() {
        Variable v = var("X", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED);
        g.elementTable.add("a");
        g.elementTable.add("b");
        g.elementTable.add("c");

        for (double d : g.values) {
            assertEquals(0.0, d,
                    "Writing to elementTable must not modify values[] (invariant)");
        }
    }

    // ------------------------------------------------------------------
    // getVariables / dimensions / offsets (inherited from AbstractIndexedPotential)
    // ------------------------------------------------------------------

    @Test
    void getVariablesReturnsCorrectVariables() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        assertEquals(List.of(x, y), g.getVariables());
    }

    @Test
    void getDimensionsAreCorrect() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        int[] dims = g.getDimensions();
        assertEquals(2, dims[0]);
        assertEquals(3, dims[1]);
    }

    @Test
    void getOffsetsAreCorrect() {
        Variable x = var("X", 2);
        Variable y = var("Y", 3);
        GTablePotential<String> g = new GTablePotential<>(List.of(x, y));
        int[] off = g.getOffsets();
        assertEquals(1, off[0], "offset[0] = 1");
        assertEquals(2, off[1], "offset[1] = numStates(X) * offset[0] = 2");
    }

    // ------------------------------------------------------------------
    // Role — known issue: super(variables, null) discards the role arg
    // ------------------------------------------------------------------

    /**
     * Documents a known quirk: the (variables, role) constructor calls
     * {@code super(variables, null)}, so the role parameter is silently
     * discarded and {@link GTablePotential#getPotentialRole()} always returns
     * {@code null}.
     */
    @Test
    void roleParameterIsDiscardedBecauseConstructorPassesNullToSuper() {
        Variable v = var("X", 2);
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED);
        // Known behaviour: super(variables, null) ignores the role argument.
        assertNull(g.getPotentialRole(),
                "GTablePotential(vars, role) passes null to super, so role is always null."
                + " This is a known design issue relevant to re-parenting (Rediseño 3).");
    }

    /**
     * The (variables, role, elementTable) constructor also discards the role
     * for the same reason.
     */
    @Test
    void roleParameterInThreeArgConstructorIsAlsoDiscarded() {
        Variable v = var("X", 2);
        List<String> table = List.of("a", "b");
        GTablePotential<String> g = new GTablePotential<>(List.of(v), PotentialRole.UNSPECIFIED, table);
        assertNull(g.getPotentialRole(),
                "Three-arg constructor also passes null to super, role is always null.");
    }
}
