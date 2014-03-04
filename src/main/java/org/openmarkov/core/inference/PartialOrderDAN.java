/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/
package org.openmarkov.core.inference;


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
		
		Graph orderGraph = order.getGraph();
		
		//Remove all the links in orderGraph
		/*for (Link auxLink:orderGraph.getLinks()){
			orderGraph.removeLink(auxLink);
		}*/
				
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
							order.addLink(variableI, variableJ,true);
						}
			    	}
			    }
		}
		
		//Transitive reduction
			ArrayList<Link> linksToRemove = new ArrayList<>();
			for (ProbNode dec:order.getProbNodes())
			{
				    Node decNode = dec.getNode();
					List<Node> childrenOfDec = decNode.getChildren();
					for (int i=0;i<childrenOfDec.size();i++)
					{
						Node nodeI = childrenOfDec.get(i);
						for (int j=0;j<childrenOfDec.size();j++)
						{
							Node nodeJ = childrenOfDec.get(j);
							if ((nodeI!=nodeJ)&&orderGraph.existsPath(nodeI,nodeJ,true))
							{
								linksToRemove.add(orderGraph.getLink(decNode, nodeJ,true));
							}
						}
					}
			}
			for (Link auxLink:linksToRemove){
				orderGraph.removeLink(auxLink);
			}
			System.out.println(order.toString());
		}

}
