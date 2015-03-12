package org.openmarkov.core.model.network.modelUncertainty;

import static org.junit.Assert.*;

import java.util.Hashtable;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

public class SystematicSamplingTest {
	
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
			UncertainValue uncertain = uncertainParameter.uncertainValue;
			String name = uncertain.getName();
			if (name!= null && name.length()>0){
				Integer expectedPosition = positionsParams.get(name);
				assertEquals(expectedPosition,uncertainParameter.configuration,0.0);
				ProbDensFunction probDensFunction = uncertain.getProbDensFunction();
				assertNotNull(probDensFunction.getInterval(0.6));
				assertNotNull(uncertainParameter.min(0.6));
				assertNotNull(uncertainParameter.max(0.6));
			}
		}
		
	
	}

}
