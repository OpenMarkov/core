package org.openmarkov.core.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.Util;

/**
 * <code>NodePotentialEdit</code> is a simple edit that allow modify
 * the node's <code>Potential</code> values.
 * It is implemented for TablePotential Only
 *    
 * @version 1.0 21/12/10
 * @author Miguel Palacios
 */
@SuppressWarnings("serial")
public class NodePotentialEdit extends SimplePNEdit {
	/**
	 * The column of the table where is the potential
	 */
	private int col;
	/**
	 * The row of the table where is the potential
	 */
	private int row;
	/**
	 * The new value of the potential
	 */
	private Double newValue;
	/**
	 * The node
	 */
	private ProbNode probNode;
	/**
	 * A list that store the edition order 
	 */
	private LinkedList priorityList;
	/**
	 * The index of the value selected in the graphic table
	 */
	private int indexSelected;
	/**
	 * Index of the value selected
	 */
	private int potentialSelected;
	/**
	 * The potential
	 */
	private TablePotential tablePotential;
	/**
	 * the increment to get the real position of the value modified
	 */
	private int increment;
	/**
	 * The number of decimal positions in the potentials.
	 */
	private int decimalPositions = 10;
	/**
	 * the table potential 
	 */
	private double[] lastTable;
	
	private double[] newTable;
	
	private ArrayList<Variable> orderVariables = new ArrayList<Variable>();
	
	private ArrayList<Variable> newOrderVariables = new ArrayList<Variable>();

	// Constructor
	/**
	 * Creates a new <code>NodePotentialEdit</code> specifying the node to be 
	 * edited, the new value of the potential, the row and column where is the
	 * value to be modified and a priority list for potentials updating.   
	 * 
	 * @param probNode the node to be edited
	 * @param newValue the new value
	 * @param col the column in the edited table
	 * @param row the row in the edited table
	 * @param priorityList the priority lists for potentials update. 
	 * */
	public NodePotentialEdit(ProbNode probNode,Double 
			newValue, int row, int col, LinkedList priorityList) {
		super(probNode.getProbNet());
		this.probNode = probNode;
		this.row = row;
		this.col = col;
		this.newValue = newValue;
		this.priorityList = priorityList;
		this.indexSelected = probNode.getVariable().getNumStates()- ( 
				row-probNode.getNode().getNumParents() + 1 );
		//values table original order
		this.lastTable = ( (TablePotential) probNode.getPotentials().get(0) ).
			getValues().clone();
		
		
		orderVariables = probNode.getPotentials().get(0).getVariables();
		// reorder the variables like appear in PotentialsDialog
		int end=-1;
		if ( orderVariables.size() > 0 ){
			if (!(probNode.getNodeType() == NodeType.UTILITY )){
				newOrderVariables.add(orderVariables.get(0));
				end = 0;
			}
			for (int i = orderVariables.size()-1; i>end; i--){
				newOrderVariables.add(orderVariables.get(i));
				
			}
			
		}
		
		
		//Reorder the values table of TablePotential 
		try {
			this.tablePotential = DiscretePotentialOperations.reorder( (TablePotential) probNode.
					getPotentials().get(0), newOrderVariables);
		} catch (NotEnoughMemoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//values table reordered
		this.newTable = tablePotential.getValues().clone();
		
		
		if (!( tablePotential.getDimensions() == null) ){
			this.increment = tablePotential.getDimensions()[0] * (col-1);
		}else{
			increment = 0;
		}
		
		//The potentialSelected is the index in the values table reordered of 
		//the value edited 
		switch (probNode.getNodeType()){
		case CHANCE:
		case DECISION:
		case UTILITY:
			this.potentialSelected = Util.toPositionOnPotentialReordered(row, col, 
					probNode.getVariable().getNumStates(), probNode.getNode().
					getNumParents());
			break;
		
			
		}
		
	}

	// Methods
	@Override
	/** @throws exception <code>Exception</code> */
	public void doEdit() throws DoEditException {
		if (probNode.getNodeType() == NodeType.CHANCE || 
				probNode.getNodeType() == NodeType.DECISION){
			if (priorityList.isEmpty()){
				//User is editing a new column of potentials //node
				priorityList = getPriorityListInitialization();
			}else{
				//the user is editing a the same column of potentials that last
				//time
				priorityList.remove(new Integer (potentialSelected));
				priorityList.add(potentialSelected);
			}
			Iterator listIterator = priorityList.listIterator();
			newTable[potentialSelected] = newValue;
			Double sum = 0.0;
			Double rest = 0.0;
			int pos=0;
		
			while (listIterator.hasNext()== true){
				pos = (Integer) listIterator.next();
				sum = roundingDouble(sum + newTable[pos]);
				//sum += newTable[pos];
			}
			rest = Math.abs(roundingDouble(1-sum));
			//rest = Math.abs( 1 - sum );
		
			if (sum > 1.0){
				listIterator = priorityList.listIterator();
				while (listIterator.hasNext()== true && rest != 0){
					pos = (Integer) listIterator.next();
					rest = roundingDouble(rest - newTable[pos]);
					//rest = rest - newTable[pos];
					if (rest < 0){
						newTable[pos] = Math.abs(rest);
						break;
					}else
						newTable[pos] = 0;
				
					}
			}else{
				pos = (Integer) priorityList.getFirst();
				newTable[pos] = roundingDouble(newTable[pos] + rest);
				//newTable[pos] = newTable[pos] + rest;
			}
				
			
		}else{
			newTable[potentialSelected] = newValue;
		}
		tablePotential.setValues(newTable);
		//back to the original order of valuesTable 
		try {
			this.tablePotential = DiscretePotentialOperations.reorder( 
					this.tablePotential, orderVariables);
		} catch (NotEnoughMemoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList <Potential> potentials = new ArrayList<Potential>();
		potentials.add(tablePotential);
		probNode.setPotentials(potentials);
	}

	public void undo() {
		super.undo();
		tablePotential.setValues(lastTable);
	}
	public void redo(){
		this.setTypicalRedo(false);
		super.redo();
		//reorder the tablePotential 
		try {
			this.tablePotential = DiscretePotentialOperations.reorder( 
					this.tablePotential, newOrderVariables);
		} catch (NotEnoughMemoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tablePotential.setValues(newTable);
		//back to the original order of the tablePotential 
		try {
			this.tablePotential = DiscretePotentialOperations.reorder( 
					this.tablePotential, orderVariables);
		} catch (NotEnoughMemoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList <Potential> potentials = new ArrayList<Potential>();
		potentials.add(tablePotential);
		probNode.setPotentials(potentials);
	}

	
	/**Gets the node edited 
	 * @return variable1 <code>Variable</code> */
	public ProbNode getProbNode() {
		return probNode;
	}
	/** Gets the table-potential of the node 
	 * @return variable1 <code>Variable</code> */
	public TablePotential getPotential() {
		return tablePotential;
	}

	/**
	 * Gets the priority list initialization
	 * @return the priority list initialized with the the value edited in the 
	 * last place of the list
	 */	
	private LinkedList getPriorityListInitialization(){
	
		for (int i = 0; i < probNode.getVariable().getNumStates(); i++){
			if (i!=indexSelected)
				priorityList.add(i + increment);
		}
		priorityList.add(indexSelected + increment);
		return priorityList;
		
	}
	/**
	 * Gets the priority list
	 * @return the priority list
	 */
	public LinkedList getPriorityList(){
		
		return priorityList;
		
	}
	
	private double roundingDouble(double number) {

		double positions = Math.pow( 10, (double) decimalPositions );
		return Math.round( number * positions ) / positions;
	}

	/**
	 * Gets the row position associated to value edited if priorityList exists
	 * @param position position of the value in the array of values
	 * @return the position in the table
	 */
	public int getRowPosition(int position) {
		
		return Util.toPositionOnJtable(position, col, probNode.getVariable().
				getNumStates(), probNode.getNode().getNumParents());
			
		
	}
	/**
	 * Gets the row position associated to value edited if priorityList no exists
	 * @param position position of the value in the array of values
	 * @return the position in the table
	 */
	public int getRowPosition() {
		return  row;
	}
	/**
	 * Gets the column where the value is edited
	 * @return the column edited
	 */
	public int getColumnPosition() {
		return col;
	}
	private int getRelativePosition(int position){
 
		return 0;
	}
    
}
