package org.openmarkov.core.model.network;

import java.util.ArrayList;
import java.util.HashMap;

import org.openmarkov.core.action.PNESupport;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.network.potential.Potential;


/** Represent a tree of <code>ClustersOfVariable</code>.
 * @version 1.0
 * @author Manuel Arias
 * @author Francisco Javier Diez
 * @since OpenMarkov 1.0
 * @see org.openmarkov.core.model.network.ClusterOfVariables
 * @see org.openmarkov.core.model.network.ClusterTreeNode */
public abstract class ClusterForest {

	// Attributes
	protected int numClusters;
	
	protected ArrayList<ClusterOfVariables> rootClusters;
	
	/** this <code>HashMap</code> stores the smallest cluster containing each 
	 * variable. */
	protected HashMap<Variable, ClusterOfVariables> variables2Clusters;

	/** Associated graph */
	protected Graph graph;
	
	/** For undo / re do operations. */
	protected PNESupport pNESupport;
	
    // Constructors
    /** Empty constructor */
    public ClusterForest() {
		rootClusters = new ArrayList<ClusterOfVariables>();
		variables2Clusters = new HashMap<Variable, ClusterOfVariables>();
		graph = new Graph();
	}

    /** @param rootCluster <code>ClusterOfVariables</code> */
	public ClusterForest(ClusterOfVariables rootCluster) {
		this();
		rootClusters.add(rootCluster);
	}

	// Methods
	/** @return <code>ArrayList</code> of <code>ClusterOfVariables</code> */
    @SuppressWarnings("unchecked")
	public ArrayList<ClusterOfVariables> getNodes() {
		ArrayList<ClusterOfVariables> rootCliques = 
			(ArrayList<ClusterOfVariables>)getRootClusters().clone();
		ArrayList<ClusterOfVariables> cliques = 
			new ArrayList<ClusterOfVariables>();
		while (rootCliques.size() != 0) {
			int lastCliqueIndex = rootCliques.size() - 1;
			HuginClique cliqueToExpand = 
				(HuginClique)rootCliques.get(lastCliqueIndex);
			rootCliques.remove(lastCliqueIndex);
			ArrayList<ClusterOfVariables> children = 
				cliqueToExpand.getChildren();
			if (children.size() == 0) {
				cliques.add(cliqueToExpand);
			} else {
				rootCliques.add(cliqueToExpand);
			}
		}
		
		return cliques;
	}

	/** @param cluster <code>ClusterOfVariables</code>. */
	public void addCluster(ClusterOfVariables cluster) {
		rootClusters.add(cluster);	
	}
	
	/** @param variable <code>Variable</code>.
	 * @return One of the clusters containing the variable; more precisely,
	 *   the cluster associated to that variable in the <code>hashMap</code> 
	 *   variables2Clusters. */
	public ClusterOfVariables getCluster(Variable variable) {
		return variables2Clusters.get(variable);
	}
	
    /** @return The collection stored in this class: <code>ArrayList</code> of 
     * <code>ClusterOfVariables</code> */
	public ArrayList<ClusterOfVariables> getRootClusters() {
		return rootClusters; 
	}

	/** @param variable <code>Variable</code>.
	 * @param potential <code>Potential</code>. */
	public void introduceFindingPotential(Variable variable, 
			Potential potential) {
	}

	public void increaseNumNodes() {
		numClusters++;
	}

	/** @param cluster <code>ClusterOfVariables</code>. */
	public void setClusterAsRoot(ClusterOfVariables cluster) {
		rootClusters.add(cluster);
	}
	
    /** @return numClusters <code>int</code> */
    public int getNumNodes() {
    	return numClusters;
    }
    
    /** @return Associated graph <code>Graph</code> */
    public Graph getGraph() {
    	return graph;
    }
    
	/** Generates a string with root clusters (more than one in construction 
	 *   time) and non root clusters.
	 * @return <code>String</code> */
	public String toString() {
		String out = new String("Root cluster");
		if (rootClusters.size() > 1) {
			out = out + "s:\n";
		} else {
			out = out + ":\n";
		}
		for (ClusterOfVariables cluster : rootClusters) {
			out = out + cluster.toString();
		}
		return out;
	}

}