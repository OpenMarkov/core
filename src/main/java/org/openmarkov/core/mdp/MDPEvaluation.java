/**
 * 
 */
package org.openmarkov.core.mdp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.model.graph.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;


/** Provides basic functionality of MDP algorithms 
 *
 * @version 1.0
 * @author jorgefs
 */
public abstract class MDPEvaluation {
	/** Influence diagram that contains the MDP. This ID should follow the convention (Factored MDPs)
	 */
	protected ProbNet id;
	
	/** State variables of the factored MDP.
	 */
	// protected ArrayList<Variable> vblesEstado;

	/** Statistical information (algorithm performance)
	 */
	protected EvaluationStats stats;

	/** Composite variable, used to hide the complexity of the factored MDP;
	 *  Allows simpler implementations of MDP algorithms.
	 */
	protected MDPVariable mdpVariablePriori;

	/** Composite variable, used to hide the complexity of the factored MDP;
	 *  Allows simpler implementations of MDP algorithms.
	 */
	protected MDPVariable mdpVariablePosteriori;
	
	/**
	 * 
	 */
	protected MDPVariable mdpActions;

	/**
	 * 
	 */
	MDPTablePolicy myPolicy;
	
	/**
	 * 
	 */
	MDPTableReward rewardFunction;
	
	/**
	 * 
	 */
	MDPTableValueFunction valueFunction;
	
	/**
	 * 
	 */
	MDPTableValueFunction newValueFunction;
	
	/** Constructor
	 * 
	 * @param id influence diagram representing an MDP
	 * @throws Exception 
	 */
	public MDPEvaluation(ProbNet id) throws Exception {
		setId(id);

		rewardFunction= new MDPTableReward(id);		
	}

	/** Updates the reference to the influence diagram and stores the
	 * state variables in <code>vblesEstado</code> 
	 * 
	 *  TODO: debe cambiar con el nuevo convenio sugerido por Javier
	 * 
	 * @param id influence diagram to be stored
	 * @throws NotEnoughMemoryException 
	 * @throws MDPEvaluationException 
	 */
	private void setId(ProbNet id) throws NotEnoughMemoryException, MDPEvaluationException {
		// Si cambia el MDP, se necesitan nuevos contadores
		stats= new EvaluationStats();
		
		this.id = id;
		ArrayList<Variable> vblesEstadoPriori= new ArrayList<Variable>();
		ArrayList<Variable> vblesEstadoPosteriori= new ArrayList<Variable>();
		
		for (ProbNode x : id.getProbNodes(NodeType.CHANCE)) {
			//-->Jorge 2009 09 19
			if (!isTemporal(x)) {
				throw new MDPEvaluationException("Temporal index expected for var " + x);
			}
			//<--Jorge 2009 09 19
			
			// Se toma como referente para la variable de estado aquella con �ndice 1 
			if (getTemporalIndex(x)==1){
				vblesEstadoPosteriori.add(x.getVariable());
			}
			else if (getTemporalIndex(x)==0){
				vblesEstadoPriori.add(x.getVariable());
			}
			else {
				throw new MDPEvaluationException("Temporal index unallowed for var " + x);
			}
		}
		
		ArrayList<Variable> vblesDecision= new ArrayList<Variable>();

		for (ProbNode x : id.getProbNodes(NodeType.DECISION)) {
			//-->Jorge 2009 09 19
			if (!isTemporal(x)) {
				throw new MDPEvaluationException("Temporal index expected for action " + x);
			}
			//<--Jorge 2009 09 19
			
			// Se toma como referente para la variable de estado aquella con �ndice 1 
			if (getTemporalIndex(x)==1){
				vblesDecision.add(x.getVariable());
			}
		}
		
		// Checking actions consistency
		for (Variable a : vblesDecision) {
			String sPrev= getTemporalName(a) + "_0";

			ProbNode p1= id.getProbNode(a);
			ProbNode p0;
			try {
				p0 = id.getProbNode(sPrev);
			} catch (ProbNodeNotFoundException e) {
				throw new MDPEvaluationException(
						"Temporal index 0 not found for action " + a);
			}
			
			boolean actionAdded= false;
			for (Variable x : vblesEstadoPosteriori) {
				Node node= id.getProbNode(x).getNode();
				
				if (p0.getNode().isParent(node) && p1.getNode().isParent(node)) {
					// La accion 'a' en los slices 0 y 1 alteran al menos una de las vbles de estado
					//
					actionAdded= true;
					vblesEstadoPriori.add(p0.getVariable());
					break;
				}
			}
			
			if (!actionAdded){
				for (ProbNode u : id.getProbNodes(NodeType.UTILITY)) {
					Node node= u.getNode();
					
					if (p0.getNode().isParent(node)) {
						// La accion 'a' del slices 0 altera al menos una de las vbles de utilidad
						//
						actionAdded= true;
						vblesEstadoPriori.add(p0.getVariable());
						break;
					}
				}
			}
		}
		
		mdpVariablePriori= new MDPVariable (vblesEstadoPriori);
		mdpVariablePosteriori= new MDPVariable (vblesEstadoPosteriori);
		mdpActions= new MDPVariable (vblesDecision);		
	}

	/** Getter method for ID attribute
	 * @return the influence diagram
	 */
	public ProbNet getId() {
		return id;
	}

	/** Getter method of the statistical data attribute
	 * 
	 * @return
	 */
	public EvaluationStats getStats() {
		return stats;
	}

	/**
	 * 
	 * @return
	 */
	public MDPVariable getMdpVariablePriori() {
		return mdpVariablePriori;
	}

	public MDPVariable getMdpVariablePosteriori() {
		return mdpVariablePosteriori;
	}
	
	/**
	 * 
	 * @return
	 */
	public MDPVariable getMdpActions() {
		return mdpActions;
	}

	
	/** Run the algorithm on the selected ID
	 * 
	 * @param params parameters to be used (stopping criteria, gamma)
	 * @throws NotEnoughMemoryException
	 * @throws Exception
	 */
	public abstract void run(MDPParams params) throws NotEnoughMemoryException, Exception;
	
	/**
	 * @return Devuelve la mejor politica encontrada por el algoritmo
	 */
	public MDPPolicy getPolicy() {
		return myPolicy;
	}
	
	/** Returns the best policy found by the algorithm
	 * @return the best policy found by the algorithm
	 */
	public MDPValueFunction getValueFunction() {
		return valueFunction;
	}
	
	/** Returns the immediate reward function
	 * 
	 * @return the immediate reward function
	 */
	public MDPReward getInmediateReward() {
		return rewardFunction;
	}
	
	/** Check if a node of an MDP is temporary or not according to the convention
	 * 
	 * @param n node to be tested
	 * @return <code>true</code> when is temporal
	 */
	protected static boolean isTemporal(ProbNode n) {
		Pattern p = Pattern.compile("[^_]*_[01]");
		Matcher m = p.matcher(n.getName());

		return m.matches();
	}

	/** Check if a variable of an MDP is temporary or not according to the convention
	 * 
	 * @param n variable to be tested
	 * @return True if the variable is temporal
	 */
	protected static boolean isTemporal(Variable n) {
		Pattern p = Pattern.compile("[^_]*_[01]");
		Matcher m = p.matcher(n.getName());

		return m.matches();
	}
	
	/** Returns the non-temporal name of an existing temporal node
	 * 
	 * @param n temporal node
	 * @return the non-temporal name of the node
	 */
	protected static String getTemporalName(ProbNode n) {
		assert (isTemporal(n));

		Pattern p = Pattern.compile("_");
		String[] items = p.split(n.getName());

		return items[0];
	}

	/** Returns the non-temporal name of an existing temporal variable
	 * 
	 * @param n temporal variable
	 * @return the non-temporal name of the variable
	 */
	static String getTemporalName(Variable n) {
		assert (isTemporal(n));

		Pattern p = Pattern.compile("_");
		String[] items = p.split(n.getName());

		return items[0];
	}

	/** Returns the temporal index of an existing temporal variable
	 * 
	 * @param n temporal variable
	 * @return the temporal index of the variable
	 */
	static int getTemporalIndex(Variable n) {
		assert (isTemporal(n));

		Pattern p = Pattern.compile("_");
		String[] items = p.split(n.getName());

		return Integer.parseInt(items[1]);
	}
	
	/** Returns the temporal index of an existing temporal node
	 * 
	 * @param n temporal node
	 * @return the temporal index of the node
	 */
	static int getTemporalIndex(ProbNode n) {
		assert (isTemporal(n));

		Pattern p = Pattern.compile("_");
		String[] items = p.split(n.getName());

		return Integer.parseInt(items[1]);
	}
	
	/**
	 * @param configPriori
	 * @param configActions
	 * @param configPosteriori
	 * @return
	 */
	public double posteriorProb (MDPVariableConfig configPriori, MDPVariableConfig configActions, MDPVariableConfig configPosteriori) {
		double dProbEstadoCond= 1.0;

		// S�lo los potenciales condicionados (su estado futuro depende del estado de otras variables)
		for (Potential p : id.getPotentialsType(NodeType.CHANCE)) {
			
			assert (p instanceof TablePotential);
			TablePotential pot= (TablePotential) p;
			Variable condicionada= pot.getVariable(0);

			//--> Jorge 2009 09 19
			assert(!isTemporal(condicionada));
			//<-- Jorge 2009 09 19
			
			if (p.getNumVariables()<2) {
				continue;
			}
			
			int []condCoords= new int[pot.getNumVariables()];

			condCoords[0]= configPosteriori.getStateValue(condicionada);

			for (int r=1; r<pot.getNumVariables(); r++) {
				Variable derecha= pot.getVariable(r);
				assert (isTemporal(derecha));
				int tempIndex= getTemporalIndex(derecha);

				if (id.getProbNode(derecha).getNodeType() == NodeType.DECISION) {
										
					// Si la accion se ha incluido en el estado, coger su valor
					if (mdpVariablePriori.potEstado.contains(derecha)) {
						assert (tempIndex==0);
						condCoords[r]= configPriori.getStateValue(derecha);
					}
					else {
						// Si no esta a�adido a la variable de estado, tomar indice
						if (tempIndex==0) {
							String sPrev= MDPEvaluation.getTemporalName(derecha) + "_1";
							try {
								derecha= id.getVariable(sPrev);
							} catch (ProbNodeNotFoundException e) {
								e.printStackTrace();
							}
						}

						condCoords[r]= configActions.getStateValue(derecha);
					}
				}
				else if (id.getProbNode(derecha).getNodeType() == NodeType.CHANCE) {

					if (tempIndex==0) {
						// Arco diacr�nico
						condCoords[r]= configPriori.getStateValue(derecha);
					}
					else {
						// Arco sincr�nico
						assert (tempIndex==1);
						condCoords[r]= configPosteriori.getStateValue(derecha);								
					}
				}
				else {
					throw new IllegalStateException();
				}
			}

			int posCond= pot.getPosition(condCoords);
			dProbEstadoCond *= pot.getValues()[posCond];
			
			if (Math.abs(dProbEstadoCond)<Double.MIN_VALUE) {
				return 0.0;
			}
		}

		return dProbEstadoCond;
	}

	
	/** Backup del estado MDP
	 *  Qn+1(s,a)= C(a) + gamma*sum_s'{ P(s'|s,a)*Jn(s') }
	 * @throws Exception 
	 */
	public double bellmanUpdate (MDPVariableConfig configPriori,
			MDPVariableConfig configActions,
			MDPParams params,
			MDPTableValueFunction valueFunction) throws Exception {
		stats.incrementNumBackups(1);
		
		double reward= getInmediateReward().eval (configPriori, configActions);
		
		MDPVariableConfig configPosteriori= mdpVariablePosteriori.config_iterator();
		double dSumatorio= 0.0;

		while (configPosteriori.hasNext()) {
			double vs= valueFunction.getValue (configPosteriori);
			double prob= posteriorProb (configPriori, configActions, configPosteriori);
			
			dSumatorio += prob * vs;
			configPosteriori.next();
		}

		return reward + params.getDiscountRate()*dSumatorio;
	}		
}
