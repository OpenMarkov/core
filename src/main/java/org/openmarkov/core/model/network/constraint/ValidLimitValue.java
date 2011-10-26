package org.openmarkov.core.model.network.constraint;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;

import org.openmarkov.core.action.PNEdit;
import org.openmarkov.core.action.PNUndoableEditEvent;
import org.openmarkov.core.exception.CanNotDoEditException;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;

public class ValidLimitValue implements PNConstraint {
		// Attributes.
		private static ValidLimitValue constraint = null;
		
		private String message;
		
		// Constructor
		/** This constructor is private to not allow anyone to invoke it. */
		private ValidLimitValue() {
		}
		
		// Methods
		/** Singleton pattern.
		 * @return The unique instance. 
		 *  <code>DistinctVariableNames</code> */
		public static PNConstraint getUniqueInstance() {
			if (constraint == null) {
				constraint = new ValidLimitValue();
			}
			return constraint;
		}
		
		@Override
		public boolean checkEvent(UndoableEditEvent event) 
		throws NotEnoughMemoryException, NonProjectablePotentialException, 
		WrongCriterionException {
			ArrayList<PNEdit> edits = UtilConstraints.
				getEditsType(event, NodePartitionedIntervalEdit.class);
			ProbNet probNet = ((PNUndoableEditEvent)event).getProbNet();
			
			
			// get new variables names
			ArrayList<ProbNode> probNodes = new ArrayList<ProbNode>();
			ArrayList<Boolean> lowers = new ArrayList<Boolean>();
			ArrayList<Double> newValues = new ArrayList<Double>();
			ArrayList<Integer> indexStates = new ArrayList<Integer>();
			for (PNEdit edit : edits) {
				if ( ( ( NodePartitionedIntervalEdit )edit ).getStateAction() == 
					StateAction.MODIFYVALUEINTERVAL) {
				
					probNodes.add( ( (NodePartitionedIntervalEdit)edit ).
							getProbNode() );
					lowers.add( ( (NodePartitionedIntervalEdit)edit ).
							getLower() );
					newValues.add( ( (NodePartitionedIntervalEdit)edit ).
							getNewValue() );
					indexStates.add( ( (NodePartitionedIntervalEdit)edit ).
							getIndexState() );	
				}
			}
			
			// check that new variables have distinct names
			int numNewValues = newValues.size();
			for (int i = 0; i < numNewValues ; i++) {
				if (lowers.get(i)){
					if ( indexStates.get(i) > 0 && newValues.get(i) <= 
						probNodes.get(i).getVariable().getPartitionedInterval().
						getLimit(indexStates.get( i ) - 1)){
						message = "The value can not to be smaller or equals than" +
								" the previous lower limit value";
						return false;
					}else if ( newValues.get(i) >= probNodes.get(i).getVariable().
					getPartitionedInterval().getLimit(indexStates.get( i ) + 1) ){
						message = "The new value can not to be bigger or equals " +
								"than the current upper limit value";
						return false;
					}	
				}else{
					if ( indexStates.get(i) < probNodes.get(i).getVariable().
							getPartitionedInterval().getLimits().length - 2	&& 
								newValues.get(i) >= probNodes.get(i).getVariable().
							getPartitionedInterval().getLimit(indexStates.get(
									i ) + 2 ) ){
						message = "The value can not to be bigger or equals than" +
						" the next upper limit value";
						return false;
					}else if ( newValues.get(i) <= probNodes.get(i).getVariable().
							getPartitionedInterval().getLimit(indexStates.get(
									i ) ) ){
						message = "The new value can not to be bigger or equals " +
								"than the current lower limit value";
						return false;
					}
				}
			}
			
			return true;
		}

		@Override
		public boolean checkProbNet(ProbNet probNet) {
			//TODO Agregar la codificación correspondiente a la verificación del
			//estado actual de la red con respecto a los intervalos de una 
			// variable discretizada (y continua?).
			
			/*ArrayList<Variable> variablesProbNet = probNet.getVariables();
			ArrayList<String> variablesProbNetNames = new ArrayList<String>();
			for (Variable variable : variablesProbNet) {
				variablesProbNetNames.add(variable.getName());
			}
			
			// check that new variables have distinct names
			int numVariables = variablesProbNetNames.size();
			for (int i = 0; i < numVariables - 1; i++) {
				for (int j = i + 1; j < numVariables; j++) {
					if (variablesProbNetNames.get(i)
							.compareTo(variablesProbNetNames.get(j)) == 0) {
						return false;
					}
				}
			}*/
			
			// TODO Implementar el método, porque no es correcto devolver true
			return true;
		}

		@Override
		public void undoableEditWillHappen(PNUndoableEditEvent event)
		throws ConstraintViolationException, CanNotDoEditException, 
		NotEnoughMemoryException, NonProjectablePotentialException, 
		WrongCriterionException {
			if (!checkEvent(event)) {
				throw new ConstraintViolationException(message);
			}
		}

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
		}

		public String toString() {
			return this.getClass().getName();
		}

		@Override
		public void undoEditHappened(PNUndoableEditEvent event) {
			// TODO Auto-generated method stub
			
		}


}
