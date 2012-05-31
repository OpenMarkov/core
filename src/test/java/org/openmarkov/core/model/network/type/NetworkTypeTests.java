/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/


package org.openmarkov.core.model.network.type;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;
import org.openmarkov.core.action.AddProbNodeEdit;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.DistinctLinks;
import org.openmarkov.core.model.network.constraint.DistinctVariableNames;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoEmptyName;
import org.openmarkov.core.model.network.constraint.NoMultipleLinks;
import org.openmarkov.core.model.network.constraint.NoRevelationArc;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.constraint.PNConstraint;

public class NetworkTypeTests
{

    @Test 
    public void testDefaultNetworkTypeIsBayesian ()
    {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        ArrayList<PNConstraint> constraints = probNet.getConstraints ();
        Assert.assertTrue (constraints.contains (new NoEmptyName ()));
        Assert.assertTrue (constraints.contains (new DistinctVariableNames ()));
        Assert.assertTrue (constraints.contains (new OnlyChanceNodes ()));
        Assert.assertTrue (constraints.contains (new OnlyAtemporalVariables ()));
        Assert.assertFalse (constraints.contains (new OnlyTemporalVariables ()));
        Assert.assertTrue (constraints.contains (new OnlyOneAgent ()));
        Assert.assertTrue (constraints.contains (new DistinctLinks ()));
        Assert.assertTrue (constraints.contains (new NoMultipleLinks ()));
        Assert.assertTrue (constraints.contains (new OnlyDirectedLinks ()));
        Assert.assertFalse (constraints.contains (new OnlyUndirectedLinks ()));
        Assert.assertTrue (constraints.contains (new NoRevelationArc ()));
        Assert.assertTrue (constraints.contains (new NoSelfLoop ()));
        Assert.assertTrue (constraints.contains (new NoCycle ()));
    }
    
    @Test
    public void testConvertingBayesianIntoMarkov () throws ConstraintViolationException
    {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setNetworkType (MarkovNetworkType.getUniqueInstance ());
        ArrayList<PNConstraint> constraints = probNet.getConstraints ();
        Assert.assertFalse (constraints.contains (new OnlyDirectedLinks ()));
        Assert.assertTrue (constraints.contains (new OnlyUndirectedLinks ()));
    }  
    
    @Test
    public void testRemovingConstraintsNoLongerApplicable () throws ConstraintViolationException
    {
        ProbNet probNet = new ProbNet ();
        probNet.setNetworkType (InfluenceDiagramType.getUniqueInstance ());
        ArrayList<PNConstraint> constraints = probNet.getConstraints ();
        Assert.assertFalse (constraints.contains (new OnlyChanceNodes ()));
    }      
    
    @Test (expected=ConstraintViolationException.class) 
    public void testImpossibleNetworkTypeConversion () throws ConstraintViolationException, NotEnoughMemoryException, CanNotDoEditException, NonProjectablePotentialException, WrongCriterionException, DoEditException
    {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        AddProbNodeEdit addVariableEdit = new AddProbNodeEdit (probNet, new Variable("a"), NodeType.DECISION); 
        probNet.setNetworkType (InfluenceDiagramType.getUniqueInstance ());

        probNet.getPNESupport ().announceEdit(addVariableEdit);
        probNet.getPNESupport ().doEdit(addVariableEdit);
        probNet.setNetworkType (BayesianNetworkType.getUniqueInstance ());
    }      
    
    
    @Test (expected=ConstraintViolationException.class) 
    public void testAddingNotApplicableConstraints () throws ConstraintViolationException
    {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.addConstraint (new OnlyUndirectedLinks());

    }
}
