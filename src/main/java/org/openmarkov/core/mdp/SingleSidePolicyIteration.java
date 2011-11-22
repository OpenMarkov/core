package org.openmarkov.core.mdp;

import java.util.ArrayList;
import java.util.Iterator;

import org.openmarkov.core.model.network.ProbNet;


/** Asynchronous Single-sided Policy Iteration (Singh&Gullapalli 1993)
 * Articulo: Asynchronous Modified Policy Iteration with Single-sided Updates
 * 
 * Repasar: no converge correctamente si el n� de iteraciones utilizado para comprobar la convergencia
 * no garantiza que se hayan actualizado estados relevantes.
 * 
 * @author Jorge
 *
 */
public class SingleSidePolicyIteration extends
		MDPEvaluation {

	/**
	 * @param id
	 * @throws Exception
	 */
	public SingleSidePolicyIteration(ProbNet id) throws Exception {
		super(id);
		operatorsSequence= new ArrayList<AsyncOperator>();
		bGenerateRandomOperators= true;
	}

	/**
	 * @author Jorge
	 *
	 */
	protected class AsyncOperator {
		/**
		 * 
		 */
		boolean bSingleActionOperator;
		/**
		 * 
		 */
		public MDPVariableConfig state;
		/**
		 * 
		 */
		public MDPVariableConfig action;
		
		/**
		 * @param state
		 * @param action
		 */
		public AsyncOperator (MDPVariableConfig state, MDPVariableConfig action) {
			bSingleActionOperator= action!=null;
			this.state= state;
			this.action= action;
		}
	}
	
	/** Local asynchronous operators
	 */
	ArrayList<AsyncOperator> operatorsSequence;
	
	/**
	 * @param state
	 * @param action
	 */
	void addActionOperator (MDPVariableConfig state, MDPVariableConfig action) {
		if (action==null) {
			throw new IllegalArgumentException();
		}
		
		operatorsSequence.add (new AsyncOperator (state, action));
	}

	/**
	 * @param state
	 */
	void addEvalOperator (MDPVariableConfig state) {
		operatorsSequence.add (new AsyncOperator (state, null));
	}
	
	/** TODO implementar �Subir a la clase base MDPAlgorithm?
	 * @return
	 */
	boolean testConvergenceConditions() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 */
	boolean bGenerateRandomOperators;
	
	/**
	 * 
	 */
	private Iterator<AsyncOperator> itOperators;
	
	
	/**
	 * @return
	 */
	public AsyncOperator getNextAsyncOperator() {
		if (bGenerateRandomOperators) {
			AsyncOperator op= null;
			
			if (Math.random()<0.85) {
				// Solo estado
				MDPVariableConfig config= mdpVariablePriori.config_iterator();
				config.iPos= (int) Math.floor (mdpVariablePriori.potEstado.getTableSize()*Math.random());
				op= new AsyncOperator(config,null);
			}
			else {
				// estado+accion
				MDPVariableConfig config= mdpVariablePriori.config_iterator();
				MDPVariableConfig action= mdpActions.config_iterator();
				config.iPos= (int) Math.floor (mdpVariablePriori.potEstado.getTableSize()*Math.random());
				action.iPos= (int) Math.floor (mdpActions.potEstado.getTableSize()*Math.random());
				op= new AsyncOperator(config,action);
			}
			
			return op;
		}
		else {
			// Si la lista de operadores se ha terminado, comenzar de nuevo
			if (itOperators==null || !itOperators.hasNext()) {
				itOperators= operatorsSequence.iterator();
			}
			
			return itOperators.next();			
		}
	}
	
	/* (non-Javadoc)
	 * @see openmarkov.pruebas.jorge.ValueIteration#run(openmarkov.pruebas.jorge.MDPParams)
	 */
	@Override
	public void run(MDPParams params) throws Exception {
		myPolicy= new MDPTablePolicy (mdpVariablePriori, mdpActions);
		valueFunction= new MDPTableValueFunction(mdpVariablePriori);
		MDPTableValueFunction lastValueFunction= new MDPTableValueFunction(mdpVariablePriori);
		
		// Compute the desired value between iterations (stopping criteria)
		double dSigma= Double.MAX_VALUE;
		double dMinSigma= params.getEpsilon()*(1-params.getDiscountRate())/params.getDiscountRate();

		// Parametrizar en MDPParamsASPI
		int numIterationsForStopTest= 5*mdpVariablePriori.potEstado.getTableSize() * mdpActions.potEstado.getTableSize();
		
		// Iterate while stopping criteria is not reached
		for (int numIters=0;;numIters++) {
			stats.incrementNumIterations(1);
			
			// Recorrer uno a uno la tabla de estados posibles
			if (numIters>numIterationsForStopTest) {
				// Se recalcula el valor de sigma, utilizado como criterio de convergencia
				dSigma= valueFunction.calculateNormMax(lastValueFunction);
				
				if ( dSigma <= dMinSigma ) {
					break;
				}
				
				// Copia de la etapa anterior para test de parada
				lastValueFunction.copyFrom(valueFunction);

				// Esperar otra vez "k" iteraciones para hacer un nuevo test de parada
				numIters= 0;
			}

			AsyncOperator op= getNextAsyncOperator();
			MDPVariableConfig action= myPolicy.getPolicy(op.state);

			/* TODO: �Si para un estado se quiere aplicar la accion que ya est� marcada en la politica, saltar
			 * Mejorar este codigo, no esta bien romper el encapsulamiento de la accion
			 */
			if (op.action!=null && action.iPos==op.action.iPos && op.bSingleActionOperator) {
				continue;
			}
			
			double dValue= valueFunction.getValue(op.state);
			
			if (op.bSingleActionOperator) {
				// Aplicar operador Tx
				double dValueNext= bellmanUpdate(op.state, op.action, params, valueFunction);
				
				if (dValueNext>dValue) {
					myPolicy.setPolicy(op.state, op.action);
					valueFunction.setValue(op.state, dValueNext);
				}
			}
			else {
				// Aplicar operador Bx
				double dValueNext= bellmanUpdate(op.state, action, params, valueFunction);

				// Single-side operator
				valueFunction.setValue(op.state, Math.max(dValue, dValueNext));
			}
		}
	}		
}
