package org.openmarkov.core.action;

import java.util.ArrayList;

import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.UncertainValue;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * <code>AddProbNodeEdit</code> is a edit that allow add a node to 
 * <code>ProbNet</code> object.
 *  
 * @version 1 23/06/11
 * @author mluque
 * 
 */

public class UncertainValuesEdit extends SimplePNEdit {
	
	private ArrayList<Double> newValuesColumn;

	private ArrayList<UncertainValue> newUncertainColumn;
	
	private ArrayList<Double> oldValuesColumn;

	private ArrayList<UncertainValue> oldUncertainColumn;

	private int basePosition;

	public int getBasePosition() {
		return basePosition;
	}



	private ProbNode probNode;
	
	private boolean isChanceVariable;
	
	public boolean isChanceVariable() {
		return isChanceVariable;
	}


	public ProbNode getProbNode() {
		return probNode;
	}



	private boolean wasNullOldUncertainValues;
	
	/**
	 * Selected column in the values table
	 */
	private int selectedColumn;


	/**
	 * Creates a new <code>AddProbNodeEdit</code> with the network where the new
	 * new node will be added and basic information about it. 
	 * @param selectedColumn 
	 * @param b 
	 * @param probNet the <code>ProbNet</code> where the new node will be added.
	 * @param newNodeName the name of the new node
	 * @param nodeType The new node type.
	 * @param cursorposition the position (coordinates X,Y) of the node.
	 *            
	 */
	public UncertainValuesEdit( ProbNode probNode, ArrayList<UncertainValue> uncertainColumn, ArrayList<Double> valuesColumn,int basePosition, int selectedColumn, boolean isChanceVariable ){
		super(probNode.getProbNet());
		
		this.probNode = probNode;
		
		this.isChanceVariable = isChanceVariable;
		
		Variable variable = probNode.getVariable();
		
		newUncertainColumn = uncertainColumn;
		
		newValuesColumn = valuesColumn;
		
		this.basePosition = basePosition;
		
		UncertainValue[] oldUncertainValues = getPotential().getUncertainTable();
		
		wasNullOldUncertainValues = oldUncertainValues==null; 
		
		oldUncertainColumn = wasNullOldUncertainValues?null:getColumn(oldUncertainValues,variable,basePosition);
		
		oldValuesColumn = getColumn(getPotential().values,variable,basePosition);
		
		
		
		this.selectedColumn = selectedColumn;
		
	}
	
	
	private ArrayList<Double> getColumn(double []values,
			Variable variable, int basePosition) {
		
		ArrayList<Double> column = new ArrayList<Double>();
		int numElements = (isChanceVariable)?variable.getNumStates():1;
		
		for (int i=0;i<numElements;i++){
			column.add(values[basePosition+i]);
		}
		return column;
	}

	private ArrayList<UncertainValue> getColumn(UncertainValue[] uncertainValues,
			Variable variable, int basePosition) {

		ArrayList<UncertainValue> column = new ArrayList<UncertainValue>();
		int numElements = (isChanceVariable)?variable.getNumStates():1;
		
		for (int i=0;i<numElements;i++){
			column.add(uncertainValues[basePosition+i]);
		}
		return column;
	}

	public int getSelectedColumn() {
		return selectedColumn;
	}


	private TablePotential getPotential(){
		return (TablePotential)(probNode.getPotentials().get(0));
	}

	

	
	public Variable getVariable(){
		return probNode.getVariable();
	}
	@Override
	public void doEdit() {
		
		TablePotential potential = getPotential();
		
			
		if (wasNullOldUncertainValues){
			potential.setUncertainTable(new UncertainValue[potential.getTableSize()]);
		}
		
		placeNewUncertainColumn(potential);
		placeNewValuesColumn(potential);
		
        
	}

	
	
	private void placeNewValuesColumn(TablePotential potential) {
		
		placeValuesColumn(potential,newValuesColumn);
	}
	
	
	private void placeOldValuesColumn(TablePotential potential) {
		
		placeValuesColumn(potential,oldValuesColumn);
	}
	
	 private void placeOldUncertainColumn(TablePotential potential) {
			
		 placeUncertainColumn(potential,oldUncertainColumn,getVariable(),basePosition);
					
	}
	 
	 private void placeNewUncertainColumn(TablePotential potential) {
			
			placeUncertainColumn(potential,newUncertainColumn,getVariable(),basePosition);
			
		}
	 
	 private void placeValuesColumn(TablePotential potential,ArrayList<Double> column) {
			
			double[] table = potential.getValues();
			
			Variable var = getVariable();
			
			for (int i=0;i<var.getNumStates();i++){
				table[i+basePosition]=column.get(i);
			}
			
		}
	
	/**
	 * It replaces a column in the uncertain values table. If parameter 'column' is null then all the replaced cells are set to null.
	 * @param potential
	 * @param column
	 * @param var
	 * @param basePosition
	 */
	static void placeUncertainColumn(TablePotential potential,ArrayList<UncertainValue> column,Variable var,int basePosition) {	
		UncertainValue[] table = (potential.getUncertainTable());
		
		for (int i=0;i<var.getNumStates();i++){
			table[i+basePosition]=(column!=null)?column.get(i):null;
		}
		
	}
	
	

	public void undo() {
		super.undo();
		
		TablePotential potential = getPotential();
		
		if (wasNullOldUncertainValues){
			potential.setUncertainTable(null);
		}
		else{
			placeOldUncertainColumn(potential);
		}
		placeOldValuesColumn(potential);

	}

}