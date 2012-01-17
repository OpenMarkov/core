/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import java.util.ArrayList;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/**
 * Implements the tuning canonical model, first designed for its use in the
 * Optifox project It is limited to variables with only 3 possible values
 * @author Iñigo
 */
public class TuningModelPotential extends ICIPotential
{
    /**
     * The canonical model is limited to a child with only tree states
     */
    private static final int    NUM_STATES = 3;
    /**
     * List of Z variables we are going to use in the canonical model
     */
    private ArrayList<Variable> zVariables;

    /**
     * Constructor for TuningModelPotential.
     * @param variables
     * @param role
     */
    @SuppressWarnings("serial")
    public TuningModelPotential (final Variable child)
    {
        super (ICIModelType.TUNING, new ArrayList<Variable> ()
            {
                {
                    add (child);
                }
            }, PotentialRole.CONDITIONAL_PROBABILITY);
        zVariables = new ArrayList<Variable> ();
    }

    /**
     * Adds a parent to the family with its corresponding parameters
     * @param parent
     * @param parameters the four parameters that define the link in the
     *            following order: c<sub><i>i</i></sub><sup>++</sup>,
     *            c<sub><i>i</i></sub><sup>+-</sup>,
     *            c<sub><i>i</i></sub><sup>-+</sup>,
     *            c<sub><i>i</i></sub><sup>--</sup>
     */
    public void addParent (Variable parent, double[] parameters)
    {
        variables.add (parent);
        // Construct table given the parameters
        double[] values = new double[9];
        values[0] = parameters[3]; // c--
        values[1] = 1 - parameters[2] - parameters[3]; // 1 - c-+ - c--
        values[2] = parameters[2]; // c-+
        values[3] = 0.0;
        values[4] = 1.0;
        values[5] = 0.0;
        values[6] = parameters[1]; // c+-
        values[7] = 1 - parameters[2] - parameters[3]; // 1 - c++ - c+-
        values[8] = parameters[0]; // c++
        ArrayList<Variable> linkVariables = new ArrayList<Variable> ();
        // Z variable with Y's states
        Variable zVariable = new Variable ("z" + parent.getName (), variables.get (0).getStates ());
        linkVariables.add (zVariable);
        // Parent
        linkVariables.add (parent);
        this.addSubPotential (new TablePotential (linkVariables, role, values));
        zVariables.add (zVariable);
    }

    /**
     * Returns a list of projected potentials
     * @param evidenceCase. <code>EvidenceCase</code>
     * @return <code>ArrayList</code> of <code>Potential</code>
     */
    @Override
    public ArrayList<TablePotential> tableProject (EvidenceCase evidenceCase,
                                                   InferenceOptions inferenceOptions)
        throws NonProjectablePotentialException,
        NotEnoughMemoryException,
        WrongCriterionException
    {
        ArrayList<TablePotential> projectedPotentials = new ArrayList<TablePotential> ();
        projectedPotentials.add (getTuningFunctionPotential ().tableProject (evidenceCase, null).get (0));
        for (TablePotential subPotential : subPotentials)
        {
            projectedPotentials.add (subPotential.tableProject (evidenceCase, null).get (0));
        }
        return projectedPotentials;
    }

    /**
     * @return The conditional probability table given by this potential
     */
    @Override
    public TablePotential getCPT ()
        throws NotEnoughMemoryException
    {
        ArrayList<TablePotential> potentials = new ArrayList<TablePotential> (subPotentials);
        potentials.add (0, getTuningFunctionPotential ());
        // Eliminate zVariables through marginalization
        return DiscretePotentialOperations.multiplyAndMarginalize (potentials, variables,
                                                                   zVariables);
    }

    /**
     * Creates a table potential to compute the outcome of the f(tuning)
     * function
     * @return a TablePotential containing the probabilities of the tuning function
     * @throws NotEnoughMemoryException
     */
    private TablePotential getTuningFunctionPotential ()
        throws NotEnoughMemoryException
    {
        // Build the list of variables: child node first, z variables
        ArrayList<Variable> tuningFunctionVariables = new ArrayList<Variable> (zVariables);
        tuningFunctionVariables.add (0, variables.get (0));
        TablePotential tablePotential = new TablePotential (tuningFunctionVariables, role);
        // Set the values for the deterministic tuning function
        for (int i = 0; i < tablePotential.values.length; i += NUM_STATES)
        {
            int index = i / NUM_STATES;
            int netNumIncr = 0;
            for (int j = 0; j < zVariables.size (); ++j)
            {
                // netNumIncr = -1 if v-, netNumIncr = 0 if v0, netNumIncr = 1
                // if v+
                netNumIncr += (index % NUM_STATES) - 1;
                index /= 3;
            }
            // tuning function
            tablePotential.values[i] = (netNumIncr < 0) ? 1.0 : 0.0;
            tablePotential.values[i + 1] = (netNumIncr == 0) ? 1.0 : 0.0;
            tablePotential.values[i + 2] = (netNumIncr > 0) ? 1.0 : 0.0;
        }
        return tablePotential;
    }
}
