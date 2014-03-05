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
import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;

/**
 * @author mluque
 * The transitive reduction of the partial temporal order among decisions induced by the DAN.
 */
public class PartialOrderDAN {
	
	
	ProbNet order;
	
	public ProbNet getOrder() {
		return order;
	}


	public PartialOrderDAN(ProbNet probNet) throws ProbNodeNotFoundException, NodeNotFoundException{
		
		order = new ProbNet();
		
		//Only keep decision nodes
		for (ProbNode auxNode:probNet.getProbNodes()){
			//order.getGraph().removeLinks(auxNode.getNode());
			NodeType auxType = auxNode.getNodeType();
			//if ((auxType!=NodeType.CHANCE)&&(auxType!=NodeType.DECISION)){
			if (auxType==NodeType.DECISION){
				order.addProbNode(auxNode.getVariable(), auxType);
			}
		}
					
		//Transitive closure among decision nodes
		for (ProbNode nodeI:order.getProbNodes())
		{
			    for (ProbNode nodeJ:order.getProbNodes())
			    {
			    	if (nodeI!=nodeJ){
			    		Variable variableI = nodeI.getVariable();
			    		Variable variableJ = nodeJ.getVariable();
			    		ProbNode probNetProbNodeI = probNet.getProbNode(variableI);
			    		ProbNode probNetProbNodeJ = probNet.getProbNode(variableJ);
						if (probNet.existsPath(probNetProbNodeI,probNetProbNodeJ,true))
						{
							order.addLink(order.getProbNode(variableI), order.getProbNode(variableJ),true);
						}
			    	}
			    }
		}
		
		//Transitive reduction
			ArrayList<Link> linksToRemove = new ArrayList<>();
			for (ProbNode dec:order.getProbNodes())
			{
				    Node decNode = dec.getNode();
				    List<Link> decLinks = decNode.getLinks();
					for (int i=0;i<decLinks.size();i++)
					{
						Node nodeI = decLinks.get(i).getNode2();
						for (int j=0;j<decLinks.size();j++)
						{
							Link linkJ = decLinks.get(j);
							Node nodeJ = linkJ.getNode2();
							if ((nodeI!=nodeJ)&&order.getGraph().existsPath(nodeI,nodeJ,true))
							{
								linksToRemove.add(linkJ);
								
							}
						}
					}
			}
			for (Link auxLink:linksToRemove){
				order.getGraph().removeLink(auxLink);
			}
			System.out.println(order.toString());
		}
	
	public String toStringForGraphviz() throws ProbNodeNotFoundException {
	
		String content = null;
		
		ProbNet probNet = this.getOrder();
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
		
		
		
		return content;
		
	}
	

	private String getNameWithQuotes(ProbNet probNet,Node node) throws ProbNodeNotFoundException {
		return "\""+probNet.getProbNode(node).getVariable().getName()+"\"";
		
	}

}
