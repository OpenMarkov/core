package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.factory.NetsFactory;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.modelUncertainty.SensitivityAnalysisFactory;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TableDeltaPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.inference.InferenceAlgorithmBNTest;

public class SystematicSamplingTest {
	
	static String iterationVariableName = "Iteration";
	
	@Test
	public void testIDDecideTestSA(){
		
		ProbNet net = SensitivityAnalysisFactory.buildIDDecideTestSA();
		Hashtable<String,Integer> positionsParams;
		
		positionsParams = new Hashtable<>();
		positionsParams.put("sensitivity", 11);
		positionsParams.put("prevalence", 1);
		positionsParams.put("specificity", 4);
		positionsParams.put("utility non-treated disease", 1);
		positionsParams.put("utility not treated", 2);
		positionsParams.put("utility treated disease", 3);
		
		List<UncertainParameter> uncertainParams = SystematicSampling.getUncertainParameters(net);
		
		for (UncertainParameter uncertainParameter:uncertainParams){
			if (uncertainParameter.hasName()) {
				UncertainValue uncertain = uncertainParameter.uncertainValue;
				Integer expectedPosition = positionsParams.get(uncertain.getName());
				assertEquals(expectedPosition,uncertainParameter.configuration,0.0);
				ProbDensFunction probDensFunction = uncertain.getProbDensFunction();
				assertNotNull(probDensFunction.getInterval(0.6));
				assertNotNull(uncertainParameter.min(0.6));
				assertNotNull(uncertainParameter.max(0.6));
			}
		}	
	}
	
	
	@Test
	public void testSimpleIDWithoutDecisionsSATriangular(){
		testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsTriangular(),5,0.0,1.0);
	}
	
	@Test
	public void testSimpleIDWithoutDecisionsSABeta(){
		testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsBeta(),5,0.0,1.0);
	}
	
	@Test
	public void testSimpleIDWithoutDecisionsDiseaseFourStates(){
		testSampleNetwork(SensitivityAnalysisFactory.createSimpleIDWithoutDecisionsDiseaseFourStates(),4,0.1,0.5);
	}
		
		
	public void testSampleNetwork(ProbNet net,int numIntervals,double min,double max){	
		
		ProbNet sampledNet;
		TablePotential pot = null;		
		List<UncertainParameter> uncertainParams = SystematicSampling.getUncertainParameters(net);
		for (UncertainParameter uncert:uncertainParams){
			if (uncert.hasName()){
				sampledNet = sampleNetworkProbParam(net, uncert, numIntervals,min,max);
				try {
					Potential potential = sampledNet.getPotentials(
							sampledNet.getVariable(NetsFactory.diseaseName)).get(0);
					if (potential instanceof TableDeltaPotential) {
						pot = ((TableDeltaPotential) potential).getTablePotential();
					} else {
						pot = (TablePotential) potential;
					}
				} catch (NodeNotFoundException e) {
					e.printStackTrace();
				}
				InferenceAlgorithmBNTest.checkIsAConditionalProbability(pot);
			}
		}		
	}
	
	public static ProbNet sampleNetworkProbParam(
			ProbNet originalNet, UncertainParameter uncertainParameter, int numIntervals,double min,double max) {
		return SystematicSampling.sampleNetwork(
			originalNet, uncertainParameter, min,max,numIntervals,iterationVariableName);
	}

}
