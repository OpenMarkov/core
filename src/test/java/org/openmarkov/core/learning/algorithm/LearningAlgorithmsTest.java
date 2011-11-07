package org.openmarkov.core.learning.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.learning.editionsgenerator.EditionsGenerator;
import org.openmarkov.core.learning.metric.Metric;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.compound.BNConstraint;
import org.openmarkov.core.model.network.potential.TablePotential;


public class LearningAlgorithmsTest {
	
	private String netName = "learnTestDataBase.dbc";
	
	private final double maxError = 1E-6;
	
	private double alpha = 0.5;
	
	private HillClimbingAlgorithm learn;	
	private ProbNet probNet;	
	private Metric metric;
	private EditionsGenerator editionsGenerator;
	
	@Before
	public void setUp() throws Exception {
		ArrayList<Integer> variableIndex = new ArrayList<Integer>();
		
		probNet = DataBaseIO.openDataBaseFile(IOTests.testsPath + netName);
		try{
            probNet.addConstraint(
            		BNConstraint.getUniqueInstance(), false);
        } catch(ConstraintViolationException ex){}
        
        for (int i = 0; i < probNet.getNumNodes(); i++){
        	variableIndex.add(new Integer(i));
        }
        int[][] cases = DataBaseIO.getCases();
        
        metric = new BayesianMetric(probNet, cases, alpha);
		
        editionsGenerator = new HillClimbingEditionsGenerator(probNet, 
    			null, metric);
    
        learn = new HillClimbingAlgorithm(probNet, null, alpha, 
    		editionsGenerator, cases);
        learn.setListeners();
	}

	@Test
	public void testLearning() throws Exception {
		double[] probabilities;
		
		ProbNet learned = learn.run();
		
		//check the structure of the learned net
		//present links
		assertTrue(learned.getProbNode("A").getNode().isParent(learned.
				getProbNode("B").getNode()));
		assertTrue(learned.getProbNode("C").getNode().isParent(learned.
				getProbNode("B").getNode()));
		assertTrue(learned.getProbNode("C").getNode().isParent(learned.
				getProbNode("A").getNode()));
		assertTrue(learned.getProbNode("A").getNode().isParent(learned.
				getProbNode("D").getNode()));
		assertTrue(learned.getProbNode("C").getNode().isParent(learned.
				getProbNode("D").getNode()));
		assertTrue(learned.getProbNode("D").getNode().isParent(learned.
				getProbNode("E").getNode()));
		
		//non-present links
		assertFalse(learned.getProbNode("B").getNode().isParent(learned.
				getProbNode("A").getNode()));
		assertFalse(learned.getProbNode("A").getNode().isParent(learned.
				getProbNode("C").getNode()));
		assertFalse(learned.getProbNode("D").getNode().isParent(learned.
				getProbNode("A").getNode()));
		assertFalse(learned.getProbNode("E").getNode().isParent(learned.
				getProbNode("A").getNode()));
		assertFalse(learned.getProbNode("F").getNode().isParent(learned.
				getProbNode("A").getNode()));
		assertFalse(learned.getProbNode("D").getNode().isParent(learned.
				getProbNode("B").getNode()));
		assertFalse(learned.getProbNode("E").getNode().isParent(learned.
				getProbNode("B").getNode()));
		assertFalse(learned.getProbNode("F").getNode().isParent(learned.
				getProbNode("B").getNode()));
		assertFalse(learned.getProbNode("B").getNode().isParent(learned.
				getProbNode("C").getNode()));
		assertFalse(learned.getProbNode("D").getNode().isParent(learned.
				getProbNode("C").getNode()));
		assertFalse(learned.getProbNode("E").getNode().isParent(learned.
				getProbNode("C").getNode()));
		assertFalse(learned.getProbNode("F").getNode().isParent(learned.
				getProbNode("C").getNode()));
		assertFalse(learned.getProbNode("B").getNode().isParent(learned.
				getProbNode("D").getNode()));
		assertFalse(learned.getProbNode("E").getNode().isParent(learned.
				getProbNode("D").getNode()));
		assertFalse(learned.getProbNode("F").getNode().isParent(learned.
				getProbNode("D").getNode()));
		assertFalse(learned.getProbNode("A").getNode().isParent(learned.
				getProbNode("E").getNode()));
		assertFalse(learned.getProbNode("B").getNode().isParent(learned.
				getProbNode("E").getNode()));
		assertFalse(learned.getProbNode("C").getNode().isParent(learned.
				getProbNode("E").getNode()));
		assertFalse(learned.getProbNode("F").getNode().isParent(learned.
				getProbNode("E").getNode()));
		assertFalse(learned.getProbNode("A").getNode().isParent(learned.
				getProbNode("F").getNode()));
		assertFalse(learned.getProbNode("B").getNode().isParent(learned.
				getProbNode("F").getNode()));
		assertFalse(learned.getProbNode("C").getNode().isParent(learned.
				getProbNode("F").getNode()));
		assertFalse(learned.getProbNode("D").getNode().isParent(learned.
				getProbNode("F").getNode()));
		assertFalse(learned.getProbNode("E").getNode().isParent(learned.
				getProbNode("F").getNode()));
		
		//chek the CPTs
		probabilities = ((TablePotential)learned.getProbNode("A").
				getPotentials().get(0)).getValues();
		assertEquals(0.0472222222222222, probabilities[0], maxError);
		assertEquals(0.8835294117647059, probabilities[1], maxError);
		assertEquals(0.610236220472441, probabilities[2], maxError);
		assertEquals(0.6158088235294118, probabilities[3], maxError);
		assertEquals(0.9527777777777777, probabilities[4], maxError);
		assertEquals(0.11647058823529412, probabilities[5], maxError);
		assertEquals(0.38976377952755903, probabilities[6], maxError);
		assertEquals(0.38419117647058826, probabilities[7], maxError);
		probabilities = ((TablePotential)learned.getProbNode("B").
				getPotentials().get(0)).getValues();
		assertEquals(0.6028971028971029, probabilities[0], maxError);
		assertEquals(0.3971028971028971, probabilities[1], maxError);
		probabilities = ((TablePotential)learned.getProbNode("C").
				getPotentials().get(0)).getValues();
		assertEquals(0.043859649122807015, probabilities[0], maxError);
		assertEquals(0.216666666666666667, probabilities[1], maxError);
		assertEquals(0.8948170731707317, probabilities[2], maxError);
		assertEquals(0.3819444444444444, probabilities[3], maxError);
		assertEquals(0.8389830508474576, probabilities[4], maxError);
		assertEquals(0.7515337423312883, probabilities[5], maxError);
		assertEquals(0.36554621848739494, probabilities[6], maxError);
		assertEquals(0.18055555555555555, probabilities[7], maxError);
		assertEquals(0.956140350877193, probabilities[8], maxError);
		assertEquals(0.78333333333333333, probabilities[9], maxError);
		assertEquals(0.10518292682926829, probabilities[10], maxError);
		assertEquals(0.61805555555555556, probabilities[11], maxError);
		assertEquals(0.16101694915254236, probabilities[12], maxError);
		assertEquals(0.24846625766871167, probabilities[13], maxError);
		assertEquals(0.634453781512605 , probabilities[14], maxError);
		assertEquals(0.81944444444444444, probabilities[15], maxError);
		probabilities = ((TablePotential)learned.getProbNode("D").
				getPotentials().get(0)).getValues();
		assertEquals(0.10270700636942676, probabilities[0], maxError);
		assertEquals(0.7981283422459893, probabilities[1], maxError);
		assertEquals(0.8972929936305732, probabilities[2], maxError);
		assertEquals(0.2018716577540107, probabilities[3], maxError);
		probabilities = ((TablePotential)learned.getProbNode("E").
				getPotentials().get(0)).getValues();
		assertEquals(0.36213786213786214, probabilities[0], maxError);
		assertEquals(0.6378621378621379, probabilities[1], maxError);
		probabilities = ((TablePotential)learned.getProbNode("F").
				getPotentials().get(0)).getValues();
		assertEquals(0.500999000999001, probabilities[0], maxError);
		assertEquals(0.499000999000999, probabilities[1], maxError);
	}
}
