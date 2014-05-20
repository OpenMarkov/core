package org.openmarkov.core.model.network.potential.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SameAsPrevious;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

public class PotentialManagerTest {

	PotentialManager manager = null;
	Variable variableA;
	Variable variableB;
	Variable variableC;
	Variable variableU;
	ProbNet probNet;
	Node nodeU;
			
	@Before
    public void setUp() throws Exception {
		manager = new PotentialManager();
		
		probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance()); 
		variableA = new Variable("A", "no", "yes");
		variableB = new Variable("B", "no", "yes");
		variableC = new Variable("C", "no", "yes");
		variableU = new Variable("U");
		
		probNet.addNode(variableA, NodeType.CHANCE);
		probNet.addNode(variableB, NodeType.CHANCE);
		probNet.addNode(variableC, NodeType.CHANCE);
		nodeU = probNet.addNode(variableU, NodeType.UTILITY);
		
		probNet.addLink(variableB, variableA, true);
		probNet.addLink(variableC, variableA, true);
		probNet.addLink(variableB, variableU, true);
		probNet.addLink(variableC, variableU, true);
		probNet.addLink(variableA, variableU, true);
		
		Potential potentialU = new UniformPotential(variableU, Arrays.asList(variableA, variableB, variableC));
		nodeU.setPotential(potentialU);
    }
    
    @Test
    public void testGetAllPotentialsNames() {
    	Set<String> potentialNames = manager.getAllPotentialsNames();
    	Assert.assertNotNull(potentialNames);
    	Assert.assertFalse(potentialNames.isEmpty());
    }
    
    @Test
    public void testGetByName() {
    	List<Variable> variables = Arrays.asList(variableA, variableB, variableC);
    	PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
    	
    	Set<String> potentialNames = manager.getAllPotentialsNames();
    	
    	for(String potentialType : potentialNames)
    	{
    		if(!potentialType.equals(PotentialManager.getPotentialName(SameAsPrevious.class)))
    		{
		    	Potential potential =  manager.getByName(potentialType, probNet, variables, role);
		    	Assert.assertNotNull(potential);
		    	Assert.assertEquals(potentialType, PotentialManager.getPotentialName(potential.getClass()));
    		}
    	}
    }  
    
    @Test
    public void testGetByNameUtility() {
    	List<Variable> variables = Arrays.asList(variableA, variableB, variableC);
    	
    	List<String> potentialNames = manager.getFilteredPotentials(nodeU);
    	
    	for(String potentialType : potentialNames)
    	{
	    	Potential potential =  manager.getByName(potentialType, probNet, variableU, variables);
	    	Assert.assertNotNull(potential);
	    	Assert.assertEquals(PotentialRole.UTILITY, potential.getPotentialRole());
	    	Assert.assertEquals(potentialType, PotentialManager.getPotentialName(potential.getClass()));
    	}
    }      
}
