/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;

/**
 * @author marias
 * @version 1.0
 */
@RelationPotentialType(name = "Same as previous", family = "Temporal")
public class SameAsPrevious extends Potential
{
    // Attributes
    protected Potential originalPotential;
    protected int       timeDifference;
    protected ProbNet   probNet;

    // Constructors
    /**
     * Creates a potential linked to the original potential
     * @param originalPotential
     * @param timeDifference
     * @param probNet The net from which the variables will be taken
     * @throws NodeNotFoundException
     * @argCondition The network must contain the shifted variables
     */
    public SameAsPrevious (Potential potential, ProbNet probNet, int timeDifference)
        throws NodeNotFoundException
    {
        super (potential.getShiftedVariables (probNet, timeDifference),
               potential.getPotentialRole ());
        this.originalPotential = potential;
        this.probNet = probNet;
        this.timeDifference = timeDifference;
        if (isUtility ())
        {
            Variable originalUtilityVariable = originalPotential.getUtilityVariable ();
            utilityVariable = probNet.getShiftedVariable (originalUtilityVariable, timeDifference);
        }
        try {
            originalPotential = originalPotential.shift (probNet, timeDifference);
        } catch (ProbNodeNotFoundException e) {
            e.printStackTrace();
        }
        type = PotentialType.SAME_AS_PREVIOUS;
    }

    /**
     * Constructor for SameAsPrevious. Assumes timeDifference is 1.
     * @param probNet
     * @param variable
     * @throws NodeNotFoundException
     */
    public SameAsPrevious (ProbNet probNet, Variable variable)
        throws NodeNotFoundException
    {
        this (probNet, variable, 1);
    }

    public SameAsPrevious (ProbNet probNet, Variable variable, int timeDifference)
        throws NodeNotFoundException
    {
        this (getPotential (probNet, variable), probNet, timeDifference);
    }

    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role
     * @param variables
     * @param role
     */
    public static boolean validate (ProbNode probNode, List<Variable> variables, PotentialRole role)
    {
        return probNode.getVariable ().isTemporal () && probNode.getVariable ().getTimeSlice () > 0;
    }

    // Methods
    @Override
    public List<TablePotential> tableProject (EvidenceCase evidenceCase,
                                              InferenceOptions inferenceOptions)
        throws NonProjectablePotentialException,
        WrongCriterionException
    {
        return originalPotential.tableProject (evidenceCase, inferenceOptions);
    }

    public Potential getOriginalPotential ()
    {
        return originalPotential;
    }

    public Potential sample ()
    {
        return originalPotential.sample();
    }
    
    @Override
    // TODO Quitar error
    public Potential shift (ProbNet probNet, int timeSlice)
        throws ProbNodeNotFoundException
    {
        throw new Error ("We have invoked SameAsPrevious.shift()");
    }

    /**
     * Looks for a potential with
     * @param probNet. <code>ProbNet</code>
     * @param variable. <code>Variable</code>
     * @return Potential The potential referred to by this one
     * @throws NodeNotFoundException
     * @argCondition probNet must be a Markov Net with order = 1 because
     *               otherwise the potential returned could not be the right one
     * @precondition The previousProbNode must have at least one potential
     *               assigned
     */
    private static Potential getPotential (ProbNet probNet, Variable variable)
        throws NodeNotFoundException
    {
        String simpleName = variable.getName ();
        Potential previousPotential = null;
        int indexC = simpleName.lastIndexOf (" [");
        if (indexC != -1)
        {
            // For each variable in probNet...
            List<Variable> variables = probNet.getVariables ();
            simpleName = simpleName.substring (0, indexC);
            String simpleNameExtended = new String (simpleName + " [");
            // ... looks for a variable that starts with variable.getName()+" ["
            for (Variable probNetVariable : variables)
            {
                if (probNetVariable.getName ().startsWith (simpleNameExtended))
                {
                    // ...then get its potentials
                    ProbNode probNode = probNet.getProbNode (probNetVariable);
                    List<Potential> potentialsNode = probNode.getPotentials ();
                    // Assumption: all the variables have only one
                    // potential P(C|P1,P2,...,Pn)
                    for (Potential potential : potentialsNode)
                    {
                        // finally, ensures that the potential is not another
                        // SAME_AS_PREVIOUS
                        if (potential.getPotentialType () != PotentialType.SAME_AS_PREVIOUS)
                        {
                            previousPotential = potential;
                            break;
                        }
                    }
                    if (previousPotential != null)
                    {
                        break;
                    }
                }
            }
            if (previousPotential == null)
            {// There is no previous variable
                throw new NodeNotFoundException ("It does not exists a "
                                                 + "previous variable called: "
                                                 + variable.getName () + " in this probNet");
            }
        }
        else
        {
            throw new NodeNotFoundException ("Variable has no a temporal "
                                             + "type name: varName[number].");
        }
        return previousPotential;
    }

    @Override
    public Potential copy ()
    {
        Potential newPotential = null;
        try
        {
            newPotential = new SameAsPrevious (originalPotential, probNet, timeDifference);
        }
        catch (NodeNotFoundException e)
        { /* Can never happen */
        }
        return newPotential;
    }

    @Override
    public boolean isUncertain ()
    {
        return getOriginalPotential ().isUncertain ();
    }
}
