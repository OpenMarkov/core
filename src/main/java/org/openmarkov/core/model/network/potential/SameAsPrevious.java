/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

/**
 * @author marias
 * @version 1.0
 */
@PotentialType(name = "Same as previous", family = "Temporal")
public class SameAsPrevious extends Potential
{
    // Attributes
    protected Potential shiftedPotential;
    protected int       timeDifference;
    protected ProbNet   probNet;

    // Constructors
    /**
     * Creates a potential linked to the original potential
     * @param shiftedPotential
     * @param timeDifference
     * @param probNet The net from which the variables will be taken
     * @throws NodeNotFoundException
     * @throws NodeNotFoundException 
     * @argCondition The network must contain the shifted variables
     */
    public SameAsPrevious (Potential originalPotential, ProbNet probNet, int timeDifference)
        throws NodeNotFoundException
    {
        super (originalPotential.getShiftedVariables (probNet, timeDifference),
               originalPotential.getPotentialRole ());
        this.probNet = probNet;
        this.timeDifference = timeDifference;
        this.shiftedPotential = originalPotential.copy();
        shiftedPotential.shift (probNet, timeDifference);
        this.utilityVariable = shiftedPotential.getUtilityVariable(); 
    }
    
    public SameAsPrevious (SameAsPrevious potential)
    {
        super(potential);
        this.probNet = potential.probNet;
        this.shiftedPotential = potential.shiftedPotential.copy();
        this.timeDifference = potential.timeDifference;
    }

    /**
     * Constructor for SameAsPrevious. Assumes timeDifference is 1.
     * @param probNet
     * @param variable
     * @throws NodeNotFoundException
     * @throws NodeNotFoundException 
     */
    public SameAsPrevious (ProbNet probNet, List<Variable> variables)
        throws NodeNotFoundException, NodeNotFoundException
    {
        this (probNet, variables.get(0), 1);
    }
    
    /**
     * Utility constructor
     * @param probNet
     * @param variable
     * @throws NodeNotFoundException
     * @throws NodeNotFoundException
     */
    public SameAsPrevious (ProbNet probNet, Variable variable)
            throws NodeNotFoundException, NodeNotFoundException
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
    public static boolean validate (Node node, List<Variable> variables, PotentialRole role)
    {
        return node.getVariable ().isTemporal () && node.getVariable ().getTimeSlice () > 0;
    }

    // Methods
    @Override
    public List<TablePotential> tableProject (EvidenceCase evidenceCase,
                                              InferenceOptions inferenceOptions,
                                              List<TablePotential> projectedPotentials)
        throws NonProjectablePotentialException,
        WrongCriterionException
    {
        return shiftedPotential.tableProject (evidenceCase, inferenceOptions, projectedPotentials);
    }

    public Potential getShiftedPotential ()
    {
        return shiftedPotential;
    }

    public Potential sample ()
    {
        return shiftedPotential.sample();
    }
    
    /**
     * Looks for a potential with
     * @param probNet. <code>ProbNet</code>
     * @param variable. <code>Variable</code>
     * @return Potential The potential referred to by this one
     * @throws NodeNotFoundException
     * @throws NodeNotFoundException 
     * @argCondition probNet must be a Markov Net with order = 1 because
     *               otherwise the potential returned could not be the right one
     * @precondition The previousNode must have at least one potential
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
                    Node node = probNet.getNode (probNetVariable);
                    List<Potential> potentialsNode = node.getPotentials ();
                    // Assumption: all the variables have only one
                    // potential P(C|P1,P2,...,Pn)
                    for (Potential potential : potentialsNode)
                    {
                        // finally, ensures that the potential is not another
                        // SAME_AS_PREVIOUS
                        if (!(potential instanceof SameAsPrevious))
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
                throw new NodeNotFoundException (probNet, "It does not exists a "
                                                 + "previous variable called: "
                                                 + variable.getName () + " in this probNet");
            }
        }
        else
        {
            throw new NodeNotFoundException (probNet, "Variable has not a temporal "
                                             + "type name: varName[number].");
        }
        return previousPotential;
    }

   
    @Override
    public Potential copy ()
    {
        return new SameAsPrevious(this);
    }

    @Override
    public boolean isUncertain ()
    {
        return getShiftedPotential ().isUncertain ();
    }
    
    @Override
    public String toString() {
        return super.toString() + " = SameAsPrevious";
    }

	@Override
	public void replaceNumericVariable(Variable convertedParentVariable) {
		super.replaceNumericVariable(convertedParentVariable);
		shiftedPotential.replaceNumericVariable(convertedParentVariable);
	}     
    
    
}
