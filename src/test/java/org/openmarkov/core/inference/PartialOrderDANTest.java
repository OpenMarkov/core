/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.inference;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.inference.PartialOrderDAN;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NetsFactory;
import org.openmarkov.core.model.network.ProbNet;

/**
 * @author manolo
 *
 */
public class PartialOrderDANTest {
	
	
//	@Test
	/*public void testDiabetesDAN() throws NodeNotFoundException, ProbNodeNotFoundException {
		ProbNet diabetesDAN = NetsFactory.buildDiabetesDAN();
		PartialTemporalOrder order = new PartialTemporalOrder(diabetesDAN);
		printOrderToGraphviz(order,"/home/manolo/testTemporalOrder.dot");
	}*/
	
	@Test
	public void testThreePhasesOfTestsDAN() throws NodeNotFoundException, ProbNodeNotFoundException {
		//ProbNet threePhasesOfTestsDAN = NetsFactory.buildThreePhasesOfTestsDAN();
		//PartialOrderDAN order = new PartialOrderDAN(threePhasesOfTestsDAN);
		int i = 0;
		//printOrderToGraphviz(order,"/home/manolo/testTemporalOrder.dot");
	}

	private void printOrderToGraphviz(PartialOrderDAN order, String string) throws ProbNodeNotFoundException {
		DataOutputStream out=null;
		
		try {
			out = new DataOutputStream(
						 new FileOutputStream(string));
		}
		catch (IOException e) {
			System.err.println("Couldn't open: "+string);
			System.exit(-1);
		}
		String content = null;
		
		ProbNet probNet = order.getOrder();
		List<Link> links = probNet.getGraph().getLinks();
		content = "digraph G {\n";
		
		for (Node node:probNet.getGraph().getNodes()){
			String strType = null;;
			switch (probNet.getProbNode(node).getNodeType()){
			case CHANCE:
				strType = "ellipse";
				break;
			case DECISION:
				strType = "decision";
				break;
			}
			content = content + getNameWithQuotes(probNet, node) + "[shape="+strType+"]\n";
		}
		
		for (Link link:links){
			Node node1 = link.getNode1();
			Node node2 = link.getNode2();
			
			content = content +  getNameWithQuotes(probNet,node1)+ "-> "+ getNameWithQuotes(probNet,node2)+"\n";
			
			
		}
		content = content + "}\n";
		
		System.out.print(content);
		try {
			out.writeBytes(content);
		}
		catch (IOException e) {
			System.err.println("Couldn't write in output");
			System.exit(-1);
		}
		
	}

	private String getNameWithQuotes(ProbNet probNet,Node node) throws ProbNodeNotFoundException {
		return "\""+probNet.getProbNode(node).getVariable().getName()+"\"";
		
	}

}
