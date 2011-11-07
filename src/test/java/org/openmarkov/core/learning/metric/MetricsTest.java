package org.openmarkov.core.learning.metric;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.action.AddLinkEdit;
import org.openmarkov.core.action.InvertLinkEdit;
import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.action.RemoveLinkEdit;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.compound.BNConstraint;


public class MetricsTest {
	
	ProbNet probNet;
	
	BayesianMetric bayesianMetric;
	EntropyMetric entropyMetric;
	BDMetric bdMetric;
	AICMetric aicMetric;
	K2Metric k2Metric;
	MDLMetric mdlMetric;
	
	private PNESupport pNESupport;
	
	private double alpha = 0.5;
	
	private String netName = "learnTestDataBase.dbc";
	
	private final double maxError = 1E-6;
	
	@Before
	public void setUp() throws Exception {
		ArrayList<Integer> variableIndex = new ArrayList<Integer>();
		
		probNet = DataBaseIO.openDataBaseFile(IOTests.testsPath + netName);
		try{
            probNet.addConstraint(
            		BNConstraint.getUniqueInstance(), false);
        } catch(ConstraintViolationException ex){}
        
        probNet = DataBaseIO.openDataBaseFile(IOTests.testsPath + netName);
		try{
            probNet.addConstraint(
            		BNConstraint.getUniqueInstance(), false);
        } catch(ConstraintViolationException ex){}
        
        for (int i = 0; i < probNet.getNumNodes(); i++){
        	variableIndex.add(new Integer(i));
        }
        int[][] cases = DataBaseIO.getCases();
        
        bayesianMetric = new BayesianMetric(probNet, cases, alpha);
        entropyMetric = new EntropyMetric(probNet, cases);
        bdMetric = new BDMetric(probNet, cases, alpha);
        aicMetric = new AICMetric(probNet, cases);
        k2Metric = new K2Metric(probNet, cases);
        mdlMetric = new MDLMetric(probNet, cases);
        
        if (pNESupport == null) {
            this.pNESupport = new PNESupport(probNet, false);
        }
        pNESupport.addUndoableEditListener(bayesianMetric);
        pNESupport.addUndoableEditListener(entropyMetric);
        pNESupport.addUndoableEditListener(bdMetric);
        pNESupport.addUndoableEditListener(aicMetric);
        pNESupport.addUndoableEditListener(k2Metric);
        pNESupport.addUndoableEditListener(mdlMetric);
	}

	@Test
	public void testScores() {
		double score = bayesianMetric.getScore();
		assertEquals(score, -4003.30495017, maxError);
		score = entropyMetric.getScore();
		assertEquals(score, -3981.22537135, maxError);
		score = bdMetric.getScore();
		assertEquals(score, -4003.30495017, maxError);
		score = aicMetric.getScore();
		assertEquals(score, -3987.22537135, maxError);
		score = k2Metric.getScore();
		assertEquals(score, -4000.78240936, maxError);
		score = mdlMetric.getScore();
		assertEquals(score, -4001.94863718, maxError);
	}

	@Test
	public void testScoreLinkAdded() throws NotEnoughMemoryException, 
			ProbNodeNotFoundException {
		AddLinkEdit edition = new AddLinkEdit(probNet, 
				probNet.getVariable("E"), probNet.getVariable("D"), true);
		double score = bayesianMetric.score(edition);
		assertEquals(score, 257.74000879, maxError);
		score = entropyMetric.score(edition);
		assertEquals(score, 260.68843349, maxError);
		score = bdMetric.score(edition);
		assertEquals(score, 257.07201665, maxError);
		score = aicMetric.score(edition);
		assertEquals(score, 259.68843349, maxError);
		score = k2Metric.score(edition);
		assertEquals(score, 257.51502261, maxError);
		score = mdlMetric.score(edition);
		assertEquals(score, 257.23455585, maxError);
	}

	@Test
	public void testScoreLinkInverted() throws Exception {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet,
				probNet.getVariable("E"), probNet.getVariable("D"), true);

		pNESupport.announceEdit(auxiliarEdition);
        pNESupport.doEdit(auxiliarEdition);
        
		InvertLinkEdit edition = new InvertLinkEdit(probNet, 
				probNet.getVariable("E"), probNet.getVariable("D"), true);
		double score = bayesianMetric.score(edition);
		assertEquals(score, -0.00626448, maxError);
		score = entropyMetric.score(edition);
		assertEquals(score, -1.136868e-13, maxError);
		score = bdMetric.score(edition);
		assertEquals(score, 4.5474735e-13, maxError);
		score = aicMetric.score(edition);
		assertEquals(score, 1.1368683e-13, maxError);
		score = k2Metric.score(edition);
		assertEquals(score, -0.01248867, maxError);
		score = mdlMetric.score(edition);
		assertEquals(score, -1.1368683e-13, maxError);
	}
	
	@Test
	public void testScoreLinkRemoved() throws Exception {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet, 
				probNet.getVariable("D"), probNet.getVariable("E"), true);
		
		pNESupport.announceEdit(auxiliarEdition);
        pNESupport.doEdit(auxiliarEdition);
        
		RemoveLinkEdit edition = new RemoveLinkEdit(probNet, 
				probNet.getVariable("D"), probNet.getVariable("E"), true);
		double score = bayesianMetric.score(edition);
		assertEquals(score, -257.73374431, maxError);
		score = entropyMetric.score(edition);
		assertEquals(score, -260.68843349, maxError);
		score = bdMetric.score(edition);
		assertEquals(score, -257.07201665, maxError);
		score = aicMetric.score(edition);
		assertEquals(score, -259.68843349, maxError);
		score = k2Metric.score(edition);
		assertEquals(score, -257.50253393, maxError);
		score = mdlMetric.score(edition);
		assertEquals(score, -257.23455585, maxError);
	}
}
