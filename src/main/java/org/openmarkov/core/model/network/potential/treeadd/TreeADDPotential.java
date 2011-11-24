package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.graph.Graph;
import org.openmarkov.core.model.graph.LabelledLink;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.PotentialsContainer;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

/** A <code>TreeADDPotential</code> is a ...
 * 
 * @author jorge
 * @author fjdiez
 * @author marias
 * @version 1.0 */
public class TreeADDPotential extends Potential implements PotentialsContainer {
	
	// Attributes
	/** The tree structure of the <code>TreeADDPotential</code> is implemented 
	 * as a <code>Graph</code> and this attribute represents that structure; 
	 * graph has no relation with any <code>ProbNet</code> in this context. */
	protected Graph graph;
	
	/** <code>node</code> of <code>graph</code> attribute. */
	protected Node root;
	
	protected ArrayList<Potential> innerPotentials= new ArrayList<Potential>();
	
	protected HashMap<String, Node> hashReferences= new HashMap<String, Node>();
	
	/** Avoid searching for a leaf (to find which is the conditioned variable)
	 * conditionedVariable and utilityVariable are mutually exclusive */
	protected Variable conditionedVariable;
	
	// Constructors
	/** @param variables. <code>ArrayList</code> of <code>Variable</code>
	 * @param graph. <code>Graph</code>
	 * @param role. <code>PotentialRole</code> */
	public TreeADDPotential(ArrayList<Variable> variables, 
			Graph graph, PotentialRole role) {
		//super (findVariables (graph), role);
		super (variables, role);
		this.graph= graph;
		type = PotentialType.TREE_ADD;
		
		if (role==PotentialRole.CONDITIONAL_PROBABILITY) {
			setConditionedVariable(variables.get(0));
		}
		
		setStartNode();
	}
	
	/** @param variables
	 * @param role */
	public TreeADDPotential(ArrayList<Variable> variables, PotentialRole role) {
		super (variables, role);
		
		graph= new Graph();
		graph.makeLinksExplicit(true);		
		
		type = PotentialType.TREE_ADD;
		
		if (role==PotentialRole.CONDITIONAL_PROBABILITY) {
			setConditionedVariable(variables.get(0));
		}
		
		setStartNode();
	}

	/** Copy constructor
	 * @param potential
	 * @throws CloneNotSupportedException */
	@SuppressWarnings("unchecked")
	public TreeADDPotential (TreeADDPotential potential) 
	throws CloneNotSupportedException {
		super ((ArrayList<Variable>) potential.getVariables().clone(), 
				potential.getPotentialRole());

		type = PotentialType.TREE_ADD;
		
		if (role==PotentialRole.CONDITIONAL_PROBABILITY) {
			setConditionedVariable(variables.get(0));
		}else if (role==PotentialRole.UTILITY) {
				setUtilityVariable(potential.getUtilityVariable());
			}
		
		graph= new Graph();
		graph.makeLinksExplicit(true);		

		HashMap<Node, Node> hashNodeSubstitution= new HashMap<Node, Node>();		
		HashMap<TablePotential, TablePotential> hashObjectSubstitution = 
			new HashMap<TablePotential, TablePotential>();
		
		// --> HACK: manual copy of the graph. It's possible that graph.copy() 
		// inserts the same Link several times
		for (Link link : potential.graph.getLinks()) {			
			if (!(link instanceof LabelledLink)) {
				throw new RuntimeException("Expected LabelledLink class: found "
						+ link.getClass().getName());
			}

			Node newNode1 = cloneNode(link.getNode1(), 
					hashNodeSubstitution, hashObjectSubstitution);
			Node newNode2 = cloneNode(link.getNode2(), 
					hashNodeSubstitution, hashObjectSubstitution);
			
			LabelledLink labelledLink= (LabelledLink) link;
			Object label= labelledLink.getLabel();
			
			if (!(label instanceof BranchData)) {
				throw new RuntimeException("Expected BranchData class: found " 
						+ label.getClass().getName());
			}
			
			new LabelledLink(newNode1, newNode2, 
					true, ((BranchData) label).clone());
			
		}
		// <-- HACK

		setStartNode();
	}

	// Methods
	public Variable getConditionedVariable() {
		if (isUtility()) {
			return null;
		}

		return conditionedVariable;
	}
	
	public void setConditionedVariable (Variable variable) {
		if (!innerPotentials.isEmpty()) {
			throw new RuntimeException("The tree must have no leaves to " +
					"change the conditioned variable");
		}
		
		setUtilityVariable(null);
		
		conditionedVariable = variable;
	}

	/* (non-Javadoc)
	 * @see openmarkov.networks.Potential#setUtilityVariable(openmarkov.networks.Variable)
	 */
	@Override
	public void setUtilityVariable(Variable utilityVariable) {
		if (!innerPotentials.isEmpty()) {
			throw new RuntimeException("The tree must have no leaves to " +
					"change the utility variable");
		}
		
		super.setUtilityVariable (utilityVariable);
		
		if (utilityVariable!=null) {
			conditionedVariable = null;
		}
    }
	
	private Node cloneNode (Node oldNode, 
			HashMap<Node, Node> hashNodeSubstitution, 
			HashMap<TablePotential, TablePotential> hashObjectSubstitution) {
		Node newNode= null;
		
		if (hashNodeSubstitution.containsKey (oldNode)) {
			newNode= hashNodeSubstitution.get (oldNode);
		}
		else {
			Object obj= oldNode.getObject();

			if (obj instanceof TablePotential) {
				TablePotential oldPotential= (TablePotential) obj;
				
				if (hashObjectSubstitution.containsKey (oldPotential)) {
					// This leaf has been cloned in a previous step
					obj= hashObjectSubstitution.get (oldPotential);
				}
				else {
					double []oldTable= oldPotential.getValues().clone();
					TablePotential newPotential= new TablePotential (oldPotential.getVariables(), oldPotential.getPotentialRole(), oldTable);
					newPotential.setUtilityVariable (oldPotential.getUtilityVariable());
					if (oldPotential.getUncertainTable() != null){
						newPotential.setUncertainTable(oldPotential.getUncertainTable().clone());
					}
					addPotential (newPotential);
					obj= newPotential;

					// The conversion is stored: maybe this leaf is shared with another branch
					hashObjectSubstitution.put (oldPotential, newPotential);
				}
			}
			
			newNode= new Node (graph, obj);
			hashNodeSubstitution.put(oldNode, newNode);
		}
		
		return newNode;
	}
	
	/** Get the single node without parents. If more than one is not a tree 
	 * and throws an exception */
	public void setStartNode() {
		root= null;
		for (Node node : graph.getNodes()) {
			if (node.getParents().isEmpty()) {
				if (root!=null) {
					throw new RuntimeException("Not a tree structure");									
				}
				
				root= node;
			}
		}
	}
	
	/** @return root. <code>Node</code> */
	public Node getRoot() {
		return root;
	}
	
	/** @return graph. <code>Graph</code> */
	public Graph getGraph() {
		return graph;
	}
		
	public HashMap<String, Node> getHashReferences() {
		return hashReferences;
	}
	
	@Override
	public void addPotential(Potential potential) {
		innerPotentials.add (potential);
	}

	public void addNodeReference (String referenceName, Node node) {
		hashReferences.put (referenceName, node);
	}
	
	public String getReference (Node node) {
		String result= null;
		for (Entry<String, Node> entry : hashReferences.entrySet()) {
			if (entry.getValue()==node) {
				result= entry.getKey();
				break;
			}
		}

		return result;
	}
	
	@Override
	public boolean removePotential(Potential potential) {
		return innerPotentials.remove (potential);
	}

	public boolean removeNodeReference (String referenceName) {
		return hashReferences.remove (referenceName) != null;
	}
	
	@Override
	public void setPotentials(ArrayList<Potential> potential) {
		innerPotentials= potential;
	}

	// TODO Implementar este metodo para que pueda inducir hallazgos
	/*
	@Override
	public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase)
			throws IncompatibleEvidenceException {
		return null;
	}
	*/

	@Override
	public ArrayList<Potential> getPotentials(Variable variable) {
        ArrayList<Potential> potentials = new ArrayList<Potential>();
        for (Potential potential : innerPotentials) {
            if (potential.getVariables().contains(variable)) {
                potentials.add(potential);
            }
        }
        return potentials;
	}

	@Override
	public Potential shift(ProbNet probNet, int timeSlice)
			throws ProbNodeNotFoundException, NotEnoughMemoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TablePotential> tableProject(EvidenceCase evidenceCase, 
			InferenceOptions inferenceOptions)
	throws NonProjectablePotentialException, NotEnoughMemoryException, 
	WrongCriterionException {
		
		ArrayList<TablePotential> projectedPotentials = 
			new ArrayList<TablePotential>();
		projectedPotentials.add(singleTableProject(root, evidenceCase, 
				inferenceOptions));
		return projectedPotentials;
	}
	
	
	/**
	 * @param node. Root of this sub-tree
	 * @param evidence
	 * @param inferenceOptions. <code>InferenceOptions</code>
	 * @see InferenceOptions
	 * @return
	 * @throws NotEnoughMemoryException
	 * @throws NonProjectablePotentialException
	 * @throws WrongCriterionException
	 */
	private TablePotential singleTableProject(Node node, 
			EvidenceCase evidence,
			InferenceOptions inferenceOptions) 
			throws NotEnoughMemoryException, NonProjectablePotentialException, 
			WrongCriterionException {
		TablePotential projectedPotential;
		Object object = node.getObject();
		// TODO Admit the possibility of returning a list of potentials
		// for each node in a Tree/ADD
		
		if (evidence == null) {
			evidence = new EvidenceCase();
		}
		if (object instanceof Potential) {
			ArrayList<TablePotential>projectedPotentials = 
				((Potential)object).tableProject(evidence, inferenceOptions);
			projectedPotential = 
				DiscretePotentialOperations.multiply(projectedPotentials);
		} else {
			// node is an inner node
			Variable variable = (Variable)object;
			if (evidence.contains(variable)) {
				// projects the potential of the child node denoted by the evidence
				Finding finding = evidence.getFinding(variable);
				for (Link link : node.getLinks()) {
					if (contains((LabelledLink)link, finding)) {
						return singleTableProject(
								link.getNode2(), evidence, inferenceOptions);
					}
				}
				// if no link corresponds to this finding...
				throw new NonProjectablePotentialException("The tree " + this +
						" contains no branch for the following finding: " + 
						finding);

			} else {
				if (variable.getVariableType() == VariableType.NUMERIC) {
					throw new NonProjectablePotentialException("The tree " +
						this + " contains the numeric variable " + 
						variable.getName() +
						" that is not observed (it is not part of the " +
						"evidence).");
				} else {
					// variable is finite-states or discretized
					return tableProjectFSNode(
							node, variable, evidence, inferenceOptions);
				}
			}
		}
		return projectedPotential;
	}
	
	/** @param variable A finite-states or discretized variable
	 * @param node A node that represents this variable 
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	private TablePotential tableProjectFSNode(Node node,
			Variable variable, EvidenceCase evidence, 
			InferenceOptions inferenceOptions) throws NotEnoughMemoryException, 
			NonProjectablePotentialException, WrongCriterionException {
		ArrayList<Link> links = node.getLinks();
		// A list of the potentials obtained from the children of node
		ArrayList<TablePotential> projectedPotentials = 
			new ArrayList<TablePotential>();
		for (Link link : links) {
		    if (link.getNode1() == node) {
		    	// node is the parent (it might be the child)
		        projectedPotentials.add(singleTableProject(
		        		link, variable, evidence, inferenceOptions));
		    }
		}
		TablePotential sum = 
			DiscretePotentialOperations.sum(projectedPotentials);
		if (sum.isUtility() && sum.getUtilityVariable() == null) {
			sum.setUtilityVariable(utilityVariable);
		}
		return sum;
	}

	/** @param variable A finite-states or discretized variable
	 * @param link A link such that its first node represents variable
	 * @throws NotEnoughMemoryException 
	 * @throws WrongCriterionException 
	 * @throws NonProjectablePotentialException */
	private TablePotential singleTableProject(Link link, Variable variable, 
			EvidenceCase evidence, 
			InferenceOptions inferenceOptions) throws NotEnoughMemoryException, 
			NonProjectablePotentialException, WrongCriterionException {
		BranchData branchData = (BranchData)((LabelledLink)link).getLabel();
		ArrayList<TablePotential> deltaPotentials = 
			new ArrayList<TablePotential>();
		for (State state : branchData.getBranchStates()) {
			deltaPotentials.add(variable.deltaTablePotential(state));
		}
		ArrayList<TablePotential> factorPotentials = 
			new ArrayList<TablePotential>();
		factorPotentials.add(singleTableProject(link.getNode2(), evidence, 
				inferenceOptions));
		factorPotentials.add(DiscretePotentialOperations.sum(deltaPotentials));
		return DiscretePotentialOperations.multiply(factorPotentials);
	}
	
	/**  */
	private boolean contains(LabelledLink link, Finding finding) {
		BranchData branchData = (BranchData)link.getLabel();
		Variable variable = finding.getVariable();
		if (variable.getVariableType() == VariableType.NUMERIC) {
			for (BranchInterval interval : branchData.getBranchIntervals()) {
				if (interval.containsValue(finding.getNumericalValue())) {
					return true;
				}
			}
		} else {
			// the variable is finite-states or discretized
			for (State state : branchData.getBranchStates()) {
				if (finding.getState().contentEquals(state.getName())) {
					return true;
				}
			}
		}
		return false;	
	}
	
//	/** Recursive method that projects the evidence.
//	 * @param root. <code>Node</code>
//	 * @param projectedRoot. <code>Node</code> 
//	 * @param evidenceCase. <code>EvidenceCase</code>
//	 * @return Projected <code>TreeADDPotential</code> 
//	 * @throws NotEnoughMemoryException 
//	 * @throws WrongCriterionException */
//	private Node treeProject(
//			Node root, Node projectedRoot, EvidenceCase evidenceCase) 
//	throws NotEnoughMemoryException, WrongCriterionException {
//		Object object = root.getObject();
//		if (object.getClass() == Variable.class) { // No terminal node
//			Variable rootVariable = (Variable)object;
//			// It the variable is in the evidence replace root for child 
//			// in projected graph
//			if (evidenceCase.contains(rootVariable)) {
//				int evidenceStateIndex = 
//					evidenceCase.getFinding(rootVariable).getStateIndex();
//				ArrayList<Node> children = root.getChildren();
//				Graph graph = root.getGraph();
//				// Looks for the branch that corresponds to the evidence
//				for (Node child : children) {
//					LabelledLink link = 
//						(LabelledLink)graph.getLink(root, child, true);
//					BranchData branchData = (BranchData)link.getLabel();
//					if (branchData.containsStateName(
//							rootVariable.getStateName(evidenceStateIndex))) {
//						// and replace the projectedRoot for the projectedChild
//						Graph projectedGraph = projectedRoot.getGraph(); 
//						Node projectedChild = 
//							new Node(projectedGraph, child.getObject());
//						replaceNode(
//								projectedGraph, projectedChild, projectedRoot);
//						projectedRoot = 
//							treeProject(child, projectedChild, evidenceCase);
//						break;
//					}
//				}
//			}
//		} else {// Object in node is a leaf (terminal node), and contains a CPT
//			TablePotential leaf = (TablePotential)object;
//			if (evidenceCase.existsEvidence(leaf.getVariables())) {
//				TablePotential projectedLeaf = 
//					leaf.tableProject(evidenceCase, null).get(0);
//				projectedRoot.setObject(projectedLeaf);
//			} else {
//				projectedRoot.setObject(leaf);
//			}
//		}
//		return projectedRoot;
//	}

	/**
	 * @throws NotEnoughMemoryException   */
	public TablePotential getTablePotential(Node root) 
	throws NotEnoughMemoryException {
		Object object = root.getObject();
		if (object.getClass() == Variable.class) {
			// Gets potentials of each children
			Variable variable = (Variable)object;
			ArrayList<Node> children = root.getChildren();
			ArrayList<TablePotential> childrenPotentials = 
				new ArrayList<TablePotential>(children.size());
			Graph graph = root.getGraph();
			for (Node child : children) {
				LabelledLink link = (LabelledLink)
					graph.getLink(root, child, true);
				childrenPotentials.add(multiply(variable, link, child));
			}
			return DiscretePotentialOperations.multiply(childrenPotentials);
		} else {
			return (TablePotential)object;
		}
	}

	/** @param variable in the parent node. <code>Variable</code>
	 * @param link The link between the node with variable and child. 
	 *  <code>LabelledLink</code>
	 * @param child. <code>Node</code>
	 * @return A <code>TablePotential</code> containing the multiplication of 
	 * the potentials of the children of variable. 
	 * @throws NotEnoughMemoryException */
	private TablePotential multiply(Variable variable, LabelledLink link,
			Node child) throws NotEnoughMemoryException {
		BranchData branchData = (BranchData)link.getLabel();
		Collection<State> branchStates = branchData.getBranchStates();
		// Multiplies childs for a potential like Var(0,1,0,1) where 1's
		// are the states contained in branchStates
		ArrayList<TablePotential> potentialFindings = 
			new ArrayList<TablePotential>(branchStates.size());
		for (State state : branchStates) {
			try {
				TablePotential potential = 
					variable.deltaTablePotential(state.getName());
			    potentialFindings.add(potential);
			} catch (InvalidStateException e) {
				// Unreachable code
				throw new Error("Invalid state multiplying TreeAddPotential");
			}
		}
		// Create the adding of the potential of the findings 
		ArrayList<Variable> variables = new ArrayList<Variable>(1);
		variables.add(variable);
		TablePotential addedPotential =	new TablePotential(variables, role);
		for (TablePotential potentialFinding : potentialFindings) {
			for (int i = 0; i < addedPotential.values.length; i++) {
				addedPotential.values[i] += potentialFinding.values[i];
			}
		}
		
		ArrayList<TablePotential> operands = new ArrayList<TablePotential>(2);
		operands.add(getTablePotential(child));
		operands.add(addedPotential);
		return DiscretePotentialOperations.multiply(operands);
	}

	/** Replace node <code>willBeReplaced</code> by <code>willReplace</code> in
	 * <code>graph</code>
	 * @param graph. <code>Graph</code>
	 * @param willReplace. <code>Node</code>
	 * @param willBeReplaced. <code>Node</code> */
	private void replaceNode(Graph graph, Node willReplace, Node willBeReplaced) 
	{
		// Replace children
		ArrayList<Node> children = willBeReplaced.getChildren();
		for (Node child : children) {
			graph.removeLink(willBeReplaced, child, true);
			graph.addLink(willBeReplaced, child, true);
		} 
		// Replace parents
		ArrayList<Node> parents = willBeReplaced.getParents();
		for (Node parent : parents) {
			graph.removeLink(parent, willBeReplaced, true);
			graph.addLink(parent, willBeReplaced, true);
		}		
	}
	
	/** @throws NotEnoughMemoryException 
	 * @returns a sampled potential. By default, itself, i.e., not sampled. */
	public Potential sample(Variable simulationIndexVariable) 
	throws NotEnoughMemoryException {
		TreeADDPotential sampledTreeADD = null;
		try {
			// creates a clone
			sampledTreeADD = new TreeADDPotential(this);
			for (Node sampledTreeADDNode : sampledTreeADD.graph.getNodes()) {
				Object object = sampledTreeADDNode.getObject();
				if (object instanceof Potential) {
					// creates a sampled potential
					Potential unsampledPotential = (Potential)object;
					Potential sampledPotential = 
						unsampledPotential.sample(simulationIndexVariable);
					// makes a reference to the sampled potential from the node
					sampledTreeADDNode.setObject(sampledPotential);
					// makes another reference from the list of innerPotentials
					int index =	
						sampledTreeADD.innerPotentials.indexOf(unsampledPotential);
					if (index < 0 ) {
						System.out.println("-1");
					}
					sampledTreeADD.innerPotentials.set(index, 
							sampledPotential);
				}
			}
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sampledTreeADD;
	}

//	protected final int indentLevel = 3;
//	protected final int incrementIndent = 3;
//
//	public String toString() {
//		StringBuffer buffer = new StringBuffer(super.toString() + "\n");
//		int indentLevel = 3;
//		Object object = root.getObject();
//		printTree(buffer, object, indentLevel);
//		return buffer.toString();
//	}
//
//	private void printTree(StringBuffer buffer, Object node, int indentLevel) {
//		if (Potential.class.isAssignableFrom(node.getClass())) { // Is a leaf
//			for (int i = 0; i < indentLevel; i++) {
//				buffer.append(' ');
//			}
//			buffer.append(node.toString());
//		} else {
//			TODO Terminar. Este método es recursivo
//		}
//	}
	
}
