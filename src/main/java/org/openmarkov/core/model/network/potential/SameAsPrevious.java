/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;
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
    protected ProbNet   probNet;

    // Constructors
    /**
     * @param probNet
     * @param variable
     */
    public SameAsPrevious (ProbNet probNet, List<Variable> variables)
        throws NodeNotFoundException, NodeNotFoundException
    {
        super (variables, PotentialRole.UNSPECIFIED);
        this.probNet = probNet;
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
         super (variable, new ArrayList<Variable>());
         this.probNet = probNet;
     }    

    
    /**
     * Copy constructor
     * @param potential
     */
    public SameAsPrevious (SameAsPrevious potential)
    {
        super(potential);
        this.probNet = potential.probNet;
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
    	Potential shiftedPotential;
		try {
			shiftedPotential = getOriginalPotential(probNet, getConditionedVariable());
	    	int timeDiff = getConditionedVariable().getTimeSlice() - shiftedPotential.getConditionedVariable().getTimeSlice();
	    	shiftedPotential.shift(probNet, timeDiff);
		} catch (NodeNotFoundException e) {
			throw new NonProjectablePotentialException(e.getMessage(), e);
		}
        return shiftedPotential.tableProject (evidenceCase, inferenceOptions, projectedPotentials);
    }

    public Potential getOriginalPotential ()
    {
    	Potential originalPotential = null;
    	try
    	{
    		originalPotential = getOriginalPotential(probNet, getConditionedVariable());
    	}catch(NodeNotFoundException e)
    	{e.printStackTrace();}
        return originalPotential;
    }

    public Potential sample ()
    {
    	Potential shiftedPotential = null;
		try {
			shiftedPotential = getOriginalPotential(probNet, getConditionedVariable());
	    	int timeDiff = getConditionedVariable().getTimeSlice() - shiftedPotential.getConditionedVariable().getTimeSlice();
	    	shiftedPotential.shift(probNet, timeDiff);
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}

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
//    private static Potential getPotential (ProbNet probNet, Variable variable)
//        throws NodeNotFoundException
//    {
//        String simpleName = variable.getName ();
//        Potential previousPotential = null;
//        int indexC = simpleName.lastIndexOf (" [");
//        if (indexC != -1)
//        {
//            // For each variable in probNet...
//            List<Variable> variables = probNet.getVariables ();
//            simpleName = simpleName.substring (0, indexC);
//            String simpleNameExtended = new String (simpleName + " [");
//            // ... looks for a variable that starts with variable.getName()+" ["
//            for (Variable probNetVariable : variables)
//            {
//                if (probNetVariable.getName ().startsWith (simpleNameExtended))
//                {
//                    // ...then get its potentials
//                    Node node = probNet.getNode (probNetVariable);
//                    List<Potential> potentialsNode = node.getPotentials ();
//                    // Assumption: all the variables have only one
//                    // potential P(C|P1,P2,...,Pn)
//                    for (Potential potential : potentialsNode)
//                    {
//                        // finally, ensures that the potential is not another
//                        // SAME_AS_PREVIOUS
//                        if (!(potential instanceof SameAsPrevious))
//                        {
//                            previousPotential = potential;
//                            break;
//                        }
//                    }
//                    if (previousPotential != null)
//                    {
//                        break;
//                    }
//                }
//            }
//            if (previousPotential == null)
//            {// There is no previous variable
//                throw new NodeNotFoundException (probNet, "It does not exists a "
//                                                 + "previous variable called: "
//                                                 + variable.getName () + " in this probNet");
//            }
//        }
//        else
//        {
//            throw new NodeNotFoundException (probNet, "Variable has not a temporal "
//                                             + "type name: varName[number].");
//        }
//        return previousPotential;
//    }

    private static Potential getOriginalPotential (ProbNet probNet, Variable variable)
            throws NodeNotFoundException
        {
    	     Potential previousPotential = null;
            if (variable.isTemporal())
            {
            	int timeSlice = variable.getTimeSlice();
            	Variable previousVariable = null;
                while (timeSlice > 0 && previousVariable == null)
                {
                	try
                	{
                		previousVariable = probNet.getVariable(variable.getBaseName(), --timeSlice);
                		previousPotential = probNet.getNode(previousVariable).getPotentials().get(0);
                		if(previousPotential instanceof SameAsPrevious)
                		{
                			previousVariable = null;
                		}
                	}catch(NodeNotFoundException e)
                	{}
                }
                if (previousVariable == null)
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
        return getOriginalPotential ().isUncertain ();
    }
    
    @Override
    public String toString() {
        return super.toString() + " = SameAsPrevious";
    }

	@Override
	public void replaceNumericVariable(Variable convertedParentVariable) {
		super.replaceNumericVariable(convertedParentVariable);
	}     
    
    
}
