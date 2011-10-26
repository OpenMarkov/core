package org.openmarkov.core.model.network;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.PotentialOperations;


/** A <code>ClusterOfVariables</code> is a node in a <code>ClusterTree</code>.
 *  <p> 
 *  This is in general a hypernode, in the sense that it contains a set of 
 *  variables, and each of these variables is represented by a
 *  <code>Node</code>.  
 * @author marias
 * @author fjdiez */
public abstract class ClusterOfVariables {
	
	// Attributes for performance test
	public static int collectEvidenceInvocations = 0;

	public static int distributeEvidenceInvocations = 0;

	/** @frozen
	 * <code>node</code> that supplies the structural aspect. */
	protected Node node;
	
	/** Used to form the cluster's name. */
	protected static String clusterNamePrefix = "Cluster.";
	
	/** Cluster's name. */
	protected String name;
	
	/** Potentials whose variables are all in the cluster. */
	protected ArrayList<Potential> priorPotentials;
	
	/** An <code>evidencePotential</code> has only one variable with a
	 * probability of 1.0 one and only one state and 0.0 in the others. */
	protected ArrayList<Potential> evidencePotentials;

	/** Resulting potential of multiplying prior and evidence potentials by the
	 * messages received from all its neighbors. */
	protected Potential posteriorPotential = null;

	/** Variables in common with another <code>ClusterOfVariables</code>. */
	public ArrayList<Variable> separatorVariables;
	
	/** Variables in this cluster */
	protected ArrayList<Variable> clusterVariables;

	/** Message created in the collect evidence phase, it goes from children to
	 * parents with the separator variables. */
	protected Potential upgoingMessage = null;
	
	/** Message created in the distribute evidence phase, it goes from parents
	 * to children. */
	protected Potential downgoingMessage = null;
	
	// Constructor
    /** @param clusterForest <code>ClusterForest</code>.
     * @param variables <code>ArrayList</code> of <code>Variable</code>s.
     * @param logFile <code>LogFile</code>. */
	public ClusterOfVariables(ClusterForest clusterForest, 
			ArrayList<Variable> variables) {
		node = new Node(clusterForest.getGraph(), this);
		clusterVariables = variables;
//		super(null, variables, NodeType.CLUSTER);
		((ClusterForest)clusterForest).increaseNumNodes();
		name = clusterNamePrefix + (clusterForest.getNumNodes() - 1);
		priorPotentials = new ArrayList<Potential>();
		evidencePotentials = new ArrayList<Potential>();
		separatorVariables = new ArrayList<Variable>();
	}

	// Methods
	/** @return <code>ArrayList</code> of <code>ClusterOfVariables</code> */
	public ArrayList<ClusterOfVariables> getChildren() {
		ArrayList<ClusterOfVariables> clusterChildren = 
			new ArrayList<ClusterOfVariables>();
		ArrayList<Node> children = node.getChildren();
		for (Node node : children) {
			clusterChildren.add((ClusterOfVariables)node.getObject());
		}
		return clusterChildren;
	}
	
	/** @return <code>ArrayList</code> of <code>ClusterOfVariables</code> */
	public ArrayList<ClusterOfVariables> getParents() {
		ArrayList<ClusterOfVariables> clusterParents = 
			new ArrayList<ClusterOfVariables>();
		ArrayList<Node> parents = node.getParents();
		for (Node node : parents) {
			clusterParents.add((ClusterOfVariables)node.getObject());
		}
		return clusterParents;
	}
	
	/** @consultation
	 * @return The <code>Object</code> associated to this node that contains an 
	 * <code>ArrayList</code> of <code>Variable</code>s. */
	@SuppressWarnings("unchecked")
	public ArrayList<Variable> getVariables() {
		return (ArrayList<Variable>)clusterVariables.clone();
	}
	
	/** @param potential <code>Potential</code>*/
	public void addPriorPotential(Potential potential) {
		priorPotentials.add(potential);
	}

	/** @consultation
	 * @return priorPotentials <code>ArrayList</code> of 
	 *   <code>Potential</code>s. */
	public ArrayList<Potential> getAssignedPotentials() {
		return priorPotentials;
	}
	
	/** Calculates the marginalizated multiplicacion of: <code>priorPotentials, 
	 *   evidencePotentials</code> and the recursively collected evidence from
	 *   the children of this <code>ClusterOfVariables</code>.
	 * @param storageLevel If its value is 2 the collected evidence is 
	 *   stored in the <code>posteriorPotential</code> without been marginalized
	 *   <code>int</code>
	 * @return The marginalized multiplication (<code>Potential</code>).
	 * @throws NotEnoughMemoryException 
	 * @throws <code>PotentialOperationException</code>. */
	@SuppressWarnings("unchecked")
	public Potential collectEvidence(int storageLevel) 
			throws NotEnoughMemoryException {
		if (upgoingMessage != null) { // It has been calculated before
			return upgoingMessage;
		}
		
		collectEvidenceInvocations++;
		
		// adds the prior potentials and evidence potentials
		ArrayList<Potential> potentials = 
			(ArrayList<Potential>)priorPotentials.clone();
		potentials.addAll(evidencePotentials);
		
		// recursively invokes collectEvidence on its children
		// and add the collected potentials
		ArrayList<ClusterOfVariables> children = 
			(ArrayList<ClusterOfVariables>)((Object)getChildren());
		for (ClusterOfVariables child : children) {
			potentials.add(child.collectEvidence(storageLevel));
		}
		
		boolean isRootClique = separatorVariables.size() == 0;
		
		if (storageLevel == 2) {
			posteriorPotential = 
				DiscretePotentialOperations.multiply(potentials);
			if (!isRootClique) {
				upgoingMessage = DiscretePotentialOperations.marginalize(
					posteriorPotential,	separatorVariables);
			} else {
				upgoingMessage = posteriorPotential;
			}
			return upgoingMessage;
		}
		
		if (storageLevel == 1) {
			if (!isRootClique) {
				upgoingMessage = 
						DiscretePotentialOperations.multiplyAndMarginalize(
								potentials, separatorVariables);
			} else {
				upgoingMessage = 
					DiscretePotentialOperations.multiply(potentials);
			}
			return upgoingMessage;
		}
		
		Potential collectedEvidence = null;
		if (!isRootClique) {
			collectedEvidence = 
					DiscretePotentialOperations.multiplyAndMarginalize(
							potentials, separatorVariables);
 		} else {
 			collectedEvidence = 
 				DiscretePotentialOperations.multiply(potentials);
 		}
		return collectedEvidence;
	}
	
	/** Sends a message to each child. The message is the multiplication of:
	 * <code>priorPotentials, evidencePotentials</code> and the upgoing messages
	 * from its other children 
	 * @param storageLevel 
	 * @throws NotEnoughMemoryException */
	@SuppressWarnings("unchecked")
	public void distributeEvidence(int storageLevel) 
			throws NotEnoughMemoryException {
		// stores the product of priorPotentials, evidencePotentials, and
		// the downgoingMessage
		Potential intermediateProduct = getIntermediateProduct();
		
		// sends a downgoingMessage to each child;
		// this message is the product of the intermediateProduct multiplied
		// by the upgoing messages from its other children
		ArrayList<ClusterOfVariables> children = 
			(ArrayList<ClusterOfVariables>)((Object)getChildren());
		
		ArrayList<ClusterOfVariables> otherChildren = null;
		for (ClusterOfVariables child : children) {
			
			ArrayList<Potential> potentials = new ArrayList<Potential>();
			potentials.add(intermediateProduct);
			
			otherChildren = new ArrayList<ClusterOfVariables>();
			otherChildren.addAll(children);
			otherChildren.remove(child);
			for (ClusterOfVariables otherChild : otherChildren) {
				potentials.add(otherChild.getUpgoingMessage(storageLevel));
			}
			child.setDowngoingPotential(
				DiscretePotentialOperations.multiplyAndMarginalize(
					potentials, child.getSeparatorVariables()));
			
			child.setPosteriorPotential(
				child.getPosteriorPotential(storageLevel));
		}
	}
	
	/** @return The product of prior potentials, evidence potentials and the
	 *  downgoing message if it exists (not exists in root clusters). 
	 * @throws NotEnoughMemoryException */
	private Potential getIntermediateProduct() throws NotEnoughMemoryException {
		// adds the prior potentials and evidence potentials
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		potentials.addAll(priorPotentials);
		potentials.addAll(evidencePotentials);
		
		// downgoingMessage is null for root clusters
		if (downgoingMessage != null) {
			potentials.add(downgoingMessage);
		}
		
		// stores the product of priorPotentials, evidencePotentials, and
		// the downgoingMessage
		if (potentials.size() == 0) {
			return null;
		}
		return DiscretePotentialOperations.multiply(potentials);
	}
	
	@SuppressWarnings("unchecked")
	/** @param child <code>ClusterOfVariables</code>.
	 * @param storageLevel <code>int</code>. 
	 * @return The <code>Potential</code> sended to <code>child</code>. */
	protected Potential getDowngoingPotential(ClusterOfVariables child, 
			int storageLevel) throws Exception {
		Potential intermediateProduct = getIntermediateProduct();
		
		// sends a downgoingMessage to each child;
		// this message is the product of the intermediateProduct multiplied
		// by the upgoing messages from its other children
		ArrayList<ClusterOfVariables> children = 
			(ArrayList<ClusterOfVariables>)((Object)getChildren());
		
		ArrayList<ClusterOfVariables> otherChildren = null;
			
		ArrayList<Potential> potentials = new ArrayList<Potential>();
		if (intermediateProduct != null) {
			potentials.add(intermediateProduct);
		}
			
		otherChildren = new ArrayList<ClusterOfVariables>();
		otherChildren.addAll(children);
		otherChildren.remove(child);
		for (ClusterOfVariables otherChild : otherChildren) {
			potentials.add(otherChild.getUpgoingMessage(storageLevel));
		}
		return PotentialOperations.multiplyAndMarginalize(
			potentials, child.getSeparatorVariables());
	}

	/** @param storageLevel <code>int</code>.
	 * @return posteriorPotential <code>Potential</code>.
	 * @throws NotEnoughMemoryException 
	 * @throws <code>PotentialOperationException</code>. */
	@SuppressWarnings("unchecked")
	public Potential getPosteriorPotential(int storageLevel) 
			throws NotEnoughMemoryException {
		if (posteriorPotential != null) {
			return posteriorPotential;
		}

		// adds the prior potentials and evidence potentials
		ArrayList<Potential> potentials = 
			(ArrayList<Potential>) priorPotentials.clone();
		potentials.addAll(evidencePotentials);

		// recursively invokes collectEvidence on its children
		// and add the collected potentials
		ArrayList<ClusterOfVariables> children = 
			(ArrayList<ClusterOfVariables>) ((Object) getChildren());
		for (ClusterOfVariables child : children) {
			potentials.add(child.collectEvidence(storageLevel));
		}
		return DiscretePotentialOperations.multiply(potentials);
	}

	/** @param posteriorPotential <code>Potential</code>. */
	public void setPosteriorPotential(Potential posteriorPotential) {
		this.posteriorPotential = posteriorPotential;
	}

	/** @param potential <code>Potential</code>. */
	public void addEvidencePotential(Potential potential) {
		evidencePotentials.add(potential);
	}

	/** @return separatorVariables <code>ArrayList</code> of
	 *   <code>Variable</code>s. */
	public ArrayList<Variable> getSeparatorVariables() {
		return separatorVariables;
	}

	/** @argCondition <code>separatorVariables</code> must be included in 
	 * <code>cliqueVariables</code>.
	 * @param separatorVariables <code>ArrayList</code> of 
	 *   <code>Variable</code>s. */
	public void setSeparatorVariables(ArrayList<Variable> separatorVariables) {
		this.separatorVariables = separatorVariables;
	}

	/** @param storageLevel <code>int</code>.
	 * @return upgoingMessage <code>Potential</code>.
	 * @throws NotEnoughMemoryException 
	 * @throws <code>PotentialOperationException</code>. */
	public Potential getUpgoingMessage(int storageLevel) 
			throws NotEnoughMemoryException {
		if (upgoingMessage != null) {
			return upgoingMessage;
		}
		return collectEvidence(storageLevel);
	}

	/** @return  name <code>String</code>. */
	public String getName() {
		return name;
	}
	
	/** @return associated node. <code>Node</code> */
	public Node getNode() {
		return node;
	}
	
	/** Multiplies the priorPotentials and substitute them by the product. 
	 * It does the same recursively in its children 
	 * @throws NotEnoughMemoryException 
	 * @throws <code>PotentialOperationException</code>. */
	@SuppressWarnings("unchecked")
	public void compilePriorPotentials() throws NotEnoughMemoryException {
		if (separatorVariables.size() == 0) { // root clique, without separator
			if (priorPotentials.size() > 1) {
				Potential priorPotential = 
					DiscretePotentialOperations.multiply(priorPotentials);
				priorPotentials.clear();
				priorPotentials.add(priorPotential);
			}
		} else { // no root clique, with separator
			Potential priorPotential = 
				DiscretePotentialOperations.multiplyAndMarginalize(
					priorPotentials, getVariables());
			priorPotentials.clear();
			priorPotentials.add(priorPotential);
		}
		
		// recursive call
		ArrayList<ClusterOfVariables> children = 
			(ArrayList<ClusterOfVariables>) ((Object) getChildren());
		for (ClusterOfVariables child : children) {
			child.compilePriorPotentials();
		}
	}
	
	/** Overrides <code>toString</code> method. Mainly for test purposes. */
	public String toString() {
		String string = new String(name + ": [");
		for (int i = 0; i < clusterVariables.size(); i++) {
			string = string + clusterVariables.get(i).getName();
			if (i < clusterVariables.size() - 1) {
				string = string + ", ";
			}
		}
		string = string + "].";
		if ((separatorVariables != null) && (separatorVariables.size() > 0)) {
			string = string + "  Separator: [";
			for (int i = 0; i < separatorVariables.size(); i++) {
				string = string + separatorVariables.get(i).getName();
				if (i < separatorVariables.size() - 1) {
					string = string + ", ";
				}
			}
			string = string + "]";
		} else {
			string = string + "  No separator";
		}
		string = string + "\n";
		
		if (posteriorPotential != null) {
			string = string + "posteriorPotential: ";
			string = string + ((Potential) posteriorPotential).toString()
					+ "\n";
		}
		
		if (priorPotentials != null) {
			string = string + "priorPotentials: " + priorPotentials.size()
					+ "\n";
			for (Potential potential : priorPotentials) {
				string = string + ((Potential) potential).toString() + "\n";
			}
		}
		
		if (evidencePotentials != null) {
			string = string + "evidencePotentials: "
					+ evidencePotentials.size() + "\n";
			for (Potential potential : evidencePotentials) {
				string = string + ((Potential) potential).toString() + "\n";
			}
		}
		
		if (upgoingMessage != null) {
			string = string + "upgoingMessage: ";
			string = string + ((Potential)upgoingMessage).toString()
					+ "\n";
		}
		
		if (downgoingMessage != null) {
			string = string + "downgoingMessage: ";
			string = string + ((Potential)downgoingMessage).toString()
					+ "\n";
		}
		
		return string;
	}

	/** @param potential <code>Potential</code>. */
	protected void setDowngoingPotential(Potential potential) {
		downgoingMessage = potential;
	}

}
