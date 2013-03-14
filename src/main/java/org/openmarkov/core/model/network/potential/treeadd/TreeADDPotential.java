/**
 * 
 */
package org.openmarkov.core.model.network.potential.treeadd;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.WrongCriterionException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.PotentialType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.RelationPotentialType;


/**
 * A TreeADDPotential is a type of Potential that implies several advantages
 * instead of using tables when the potential has a substructure that repeats
 * itself several times. Each TreeADDPotential is defined by a top variable and
 * its branches
 * @author myebra
 *
 */
@RelationPotentialType(name = "Tree/ADD", family = "Tree")
public class TreeADDPotential extends Potential
    implements
        Cloneable
{
    /**
     * topVariable represents the variable on the top of the tree, in other
     * words the root variable
     */
    private Variable            topVariable;
    private PotentialType       potentialType = PotentialType.TREE_ADD;
    /**
     * This ArrayList stores the branches created in the TreeADDPotential
     * constructor
     */
    private List<TreeADDBranch> branches      = new ArrayList<TreeADDBranch> ();

    /**
     * label is incompatible with reference and reference is incompatible with
     * potential This HashMap stores those potentials that have been labeled
     * within the branches in a TreeADDPotential
     */
    // private HashMap<String, Potential> potentialsLabeled;
    /**
     * For role conditional
     * @param variables
     * @param topVariable
     * @param role
     */
    public TreeADDPotential (List<Variable> variables, Variable topVariable, PotentialRole role)
    {
        super (variables, role);
        this.topVariable = topVariable;
        VariableType variableType = topVariable.getVariableType ();
        List<Variable> potentialVariables;
        // if topVariable is finite states or discretized, it creates a branch
        // for each state
        if (variableType == VariableType.FINITE_STATES || variableType == VariableType.DISCRETIZED)
        {
            State[] states = topVariable.getStates ();
           
            for (int i = states.length - 1; i >= 0; i--)
            {
                // if potential role of the treeADD is a conditional probability
                // it is assigned an uniform potential
                // to the conditioned variable which is always the first
                // variable of the arrayList of variables
                if (role == PotentialRole.CONDITIONAL_PROBABILITY)
                {
                    Variable conditionedVariable = variables.get (0);
                    potentialVariables = new ArrayList<Variable> ();
                    potentialVariables.add (conditionedVariable);
                    UniformPotential potential = new UniformPotential (potentialVariables, role);
                    List<State> branchStates = new ArrayList<State> ();
                    branchStates.add (states[i]);
                    branches.add (new TreeADDBranch (branchStates, potential, topVariable, variables));
                }
            }
        }
        // if topVariable is numeric, it creates a branch whose thresholds are
        // the
        // same as those defined for the variable
        if (variableType == VariableType.NUMERIC)
        {
            PartitionedInterval interval = topVariable.getPartitionedInterval ();
            Threshold minimum = new Threshold ((float) interval.getMin (),
                                               !interval.isLeftClosed ());
            Threshold maximum = new Threshold ((float) interval.getMax (),
                                               interval.isRightClosed ());
            potentialVariables = new ArrayList<Variable> ();
            potentialVariables.add (variables.get (0));
            UniformPotential potential = new UniformPotential (potentialVariables, role);
            branches.add (new TreeADDBranch (minimum, maximum, potential, topVariable, variables));
        }
    }

    /**
     * For role Utility
     * @param variables
     * @param topVariable
     * @param role
     * @param utilityVariable
     */
    public TreeADDPotential (List<Variable> variables,
                             Variable topVariable,
                             PotentialRole role,
                             Variable utilityVariable)
    {
        super (variables, role, utilityVariable);
        // setUtilityVariable(utilityVariable);
        this.topVariable = topVariable;
        VariableType variableType = topVariable.getVariableType ();
        List<Variable> potentialVariables;
        // if topVariable is finite states or discretized, it creates a branch
        // for each state
        if (variableType == VariableType.FINITE_STATES || variableType == VariableType.DISCRETIZED)
        {
            State[] states = topVariable.getStates ();
            for (int i = 0; i < states.length; i++)
            {
                // if the role of the treeADD is utility, it assigns a uniform
                // potential
                if (role == PotentialRole.UTILITY)
                {
                    potentialVariables = new ArrayList<Variable> ();
                    UniformPotential potential = new UniformPotential (potentialVariables, 
                                                                       role,
                                                                       utilityVariable);
                    // potential.setUtilityVariable(utilityVariable);
                    List<State> branchStates = new ArrayList<State> ();
                    branchStates.add (states[i]);
                    branches.add (new TreeADDBranch (branchStates, potential, topVariable,
                                                     variables));
                }
            }
        }
        // if topVariable is numeric, it creates a branch whose thresholds are
        // the
        // same as those defined for the variable
        if (variableType == VariableType.NUMERIC)
        {
            PartitionedInterval interval = topVariable.getPartitionedInterval ();
            Threshold minimum = new Threshold ((float) interval.getMin (),
                                               !interval.isLeftClosed ());
            Threshold maximum = new Threshold ((float) interval.getMax (),
                                               interval.isRightClosed ());
            potentialVariables = new ArrayList<Variable> ();
            // it is an utility potential for sure so it is not necessary to add
            // variable 0 to potential variables
            UniformPotential potential = new UniformPotential (potentialVariables, role,
                                                               utilityVariable);
            // potential.setUtilityVariable(utilityVariable);
            branches.add (new TreeADDBranch (minimum, maximum, potential, topVariable, variables));
        }
    }

    public TreeADDPotential (List<Variable> variables, PotentialRole role)
    {
        this (variables, variables.get (0), role);
    }

    public TreeADDPotential (List<Variable> variables, PotentialRole role, Variable utilityVariable)
    {
        this (variables, variables.get (0), role, utilityVariable);
    }

    /**
     * Constructor for the parser
     */
    public TreeADDPotential (List<Variable> variables,
                             Variable topVariable,
                             PotentialRole role,
                             List<TreeADDBranch> branches)
    {
        super (variables, role);
        this.topVariable = topVariable;
        this.role = role;
        this.branches = branches;
    }

    /**
     * Copy constructor
     * @param treeADD
     */
    public TreeADDPotential (TreeADDPotential treeADD)
    {
        super (treeADD.getVariables (), treeADD.getPotentialRole ());
        this.topVariable = treeADD.getTopVariable ();
        this.potentialType = treeADD.getPotentialType ();
        List<TreeADDBranch> treeBranches = new ArrayList<> ();
        for (int i = 0; i < treeADD.getBranches ().size (); i++)
        {
            treeBranches.add (treeADD.getBranches ().get (i).copy ());
        }
        this.branches = treeBranches;
        if (treeADD.getPotentialRole () == PotentialRole.UTILITY)
        {
            if (treeADD.getUtilityVariable () != null)
            {
                this.setUtilityVariable (treeADD.getUtilityVariable ());
            }
        }
    }

    /**
     * @param branch
     */
    public void addTreeADDBranch (TreeADDBranch branch)
    {
        branches.add (branch);
    }

    /*
     * public void setLabeledPotentials(){ for (int i = 0; i < branches.size();
     * i++) { TreeADDBranch branch = branches.get(i); String label; if ((label =
     * branch.getLabel()) != null) { potentialsLabeled.put(label,
     * branch.getPotential()); } } }
     */
    public PotentialType getPotentialType ()
    {
        return this.potentialType;
    }

    public List<TreeADDBranch> getBranches ()
    {
        return branches;
    }

    /**
     * this method return a branch potential also when it is referenced
     * @param branch
     * @return Potential or null if the reference it has not been labelled in
     *         this tree
     */
    /*
     * public Potential getAssignedPotential(TreeADDBranch branch){
     * setLabeledPotentials(); String reference; if ((reference =
     * branch.getReference()) != null) { if(potentialsLabeled.get(reference) !=
     * null) { return potentialsLabeled.get(reference); } }else{ //If reference
     * is null that means that this branch has a potential associated return
     * branch.getPotential(); } return null; }
     */
    public void setBranchAtIndex (int index, TreeADDBranch treeBranch)
    {
        this.branches.set (index, treeBranch);
    }

    public void setBranches (List<TreeADDBranch> branches)
    {
        this.branches = branches;
    }

    public Variable getTopVariable ()
    {
        return topVariable;
    }

    public Variable getConditionedVariable ()
    {
        if (role == PotentialRole.CONDITIONAL_PROBABILITY)
        {
            return variables.get (0);
        }
        else
        {
            return null;
        }
    }

    public void setTopVariable (Variable variable)
    {
        this.topVariable = variable;
    }

    /**
     * Adds variable to a treeADD potential
     * @throws NotEnoughMemoryException
     */
    public Potential addVariable (Variable variable)
    {
        // return new UniformPotential(getVariables(), getPotentialRole());
        List<Variable> variables = getVariables ();
        variables.add (variable);
        for (TreeADDBranch branch : getBranches ())
        {
            branch.setParentVariables (variables);
            branch.getPotential ().addVariable (variable);
        }
        return this;
    }

    /**
     * Removes variable from a treeADD potential
     * @throws NotEnoughMemoryException
     */
    public  Potential removeVariable(Variable variable) {
        List<Variable> newVariables = getVariables();
        newVariables.remove(variable);
        return new UniformPotential(newVariables, getPotentialRole());
    }

    @Override
    public List<TablePotential> tableProject (EvidenceCase evidenceCase,
                                              InferenceOptions inferenceOptions)
        throws NonProjectablePotentialException,
        WrongCriterionException
    {
        List<TablePotential> potentialsToSumUp = new ArrayList<TablePotential> ();
        List<TablePotential> projectedPotentials = new ArrayList<TablePotential> ();
        TablePotential projected = null;
        List<TreeADDBranch> branches = this.getBranches ();
        if (topVariable.getVariableType () != VariableType.NUMERIC)
        {
            for (TreeADDBranch branch : branches)
            {
                Potential branchPotential = branch.getPotential ();
                List<TablePotential> tablePotentials = branchPotential.tableProject (evidenceCase, inferenceOptions);
                // mask potential, only the top variable
                TablePotential maskPotential = null;
                List<Variable> variables = new ArrayList<Variable> ();
                variables.add (branch.getTopVariable ());
                maskPotential = new TablePotential (variables, role);
                List<State> branchStates = branch.getBranchStates ();
                State[] topVariableStates = branch.getTopVariable ().getStates ();
                for (int i = 0; i < topVariableStates.length; i++)
                {
                    int[] statesIndexes = new int[1];
                    statesIndexes[0] = branch.getTopVariable ().getStateIndex (topVariableStates[i]);
                    maskPotential.setValue (variables, statesIndexes, branchStates.contains (topVariableStates[i])? 1 : 0);
                }
                // multiply mask potential and the table potential of the
                // current branch
                List<TablePotential> potentialsToMultiply = new ArrayList<TablePotential> ();
                potentialsToMultiply.add (tablePotentials.get (0));
                potentialsToMultiply.add (maskPotential);
                TablePotential intermediateProduct = (TablePotential) (DiscretePotentialOperations.multiply (potentialsToMultiply, false));
				potentialsToSumUp.add (intermediateProduct);
            }
            projected = DiscretePotentialOperations.sum (potentialsToSumUp);
        }
        else
        {
            // if there is no evidence for the numerical topVariable it is not
            // possible to project the tree
            if (evidenceCase == null || evidenceCase.getFinding (topVariable) == null)
            {
                throw new NonProjectablePotentialException ("It is not possible to project this tree " + this.toShortString () + 
                                                                    " because top variable is numeric and has no evidence");
            }
            double topVariableValue = evidenceCase.getFinding (topVariable).getNumericalValue ();
            List<TreeADDBranch> numericalBranches = getBranches ();
            Potential potential = null;
            for (TreeADDBranch numericalBranch : numericalBranches)
            {
                double minLimit = numericalBranch.getMinThreshold ().getLimit ();
                double maxlimit = numericalBranch.getMaxThreshold ().getLimit ();
                if (minLimit <= topVariableValue && topVariableValue <= maxlimit)
                {
                    if (minLimit == topVariableValue)
                    {
                        if (numericalBranch.getMinThreshold ().belongsToLeft ())
                        {
                            continue;
                        }
                        else
                        {
                            potential = numericalBranch.getPotential ();
                            break;
                        }
                    }
                    else if (maxlimit == topVariableValue)
                    {
                        if (numericalBranch.getMaxThreshold ().belongsToLeft ())
                        {
                            potential = numericalBranch.getPotential ();
                            break;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    { // minLimit < topVariableValue < maxLimit
                        potential = numericalBranch.getPotential ();
                        break;
                    }
                }
            }
            // if potential still null that means finding was not within the
            // numerical variable domain so
            if (potential == null)
            {
                throw new NonProjectablePotentialException (
                                                            "It is not possible to project this tree, "
                                                                    + "top variable value was not within the topVariable domain");
            }
            projected = potential.tableProject (evidenceCase, inferenceOptions).get (0);
        }
        // Make sure variables are in the correct order after applying the mask
        // there will be variables that disappear from the potential because of
        // evidence propagation
        /*if (role == PotentialRole.CONDITIONAL_PROBABILITY || role == PotentialRole.UTILITY)
        {
            for (int i = 0; i < correctOrder.size (); i++)
            {
                if (!projected.contains (correctOrder.get (i)))
                {
                    correctOrder.remove (i);
                }
            }
            if (role == PotentialRole.UTILITY)
            {
                projected = DiscretePotentialOperations.reorder (projected, correctOrder);
            }
            else
            {
                projected.setVariables (correctOrder);
            }
        }*/
        projectedPotentials.add (projected);
        if (role == PotentialRole.UTILITY)
        {
            for (Potential auxPot : projectedPotentials)
            {
                auxPot.setUtilityVariable (utilityVariable);
            }
        }
        return projectedPotentials;
    }

    /*
     * private TablePotential getPotentialMask () { }
     */
    @Override
    public Potential shift (ProbNet probNet, int timeDifference)
        throws ProbNodeNotFoundException
    {
        TreeADDPotential copiedTree = new TreeADDPotential (this);
        List<Variable> copiedTreeVariables = new ArrayList<> ();
        for (Variable variable : copiedTree.getVariables ())
        {
            if (variable.isTemporal ())
            {
                copiedTreeVariables.add (probNet.getShiftedVariable (variable, timeDifference));
            }
            else
            {
                copiedTreeVariables.add (variable);
            }
        }
        copiedTree.setVariables (copiedTreeVariables);
        if (isUtility ())
        {
            if (getUtilityVariable ().isTemporal ())
            {
                copiedTree.setUtilityVariable (probNet.getShiftedVariable (getUtilityVariable (),
                                                                           timeDifference));
            }
        }
        if (getTopVariable ().isTemporal ())
        {
            copiedTree.setTopVariable (probNet.getShiftedVariable (getTopVariable (),
                                                                   timeDifference));
        }
        for (TreeADDBranch branch : copiedTree.getBranches ())
        {
            branch.setParentVariables (copiedTreeVariables);
            branch.setTopVariable (copiedTree.getTopVariable ());
            if (branch.getPotential () instanceof TreeADDPotential)
            {
                branch.setPotential (((TreeADDPotential) branch.getPotential ()).shift (probNet,
                                                                                        timeDifference));
            }
            else
            {
                ArrayList<Variable> branchPotentialVariables = new ArrayList<> ();
                for (Variable variable : branch.getPotential ().getVariables ())
                {
                    if (variable.isTemporal ())
                    {
                        branchPotentialVariables.add (probNet.getShiftedVariable (variable,
                                                                                  timeDifference));
                    }
                    else
                    {
                        branchPotentialVariables.add (variable);
                    }
                }
                branch.getPotential ().setVariables (branchPotentialVariables);
                if (branch.getPotential ().isUtility ())
                {
                    if (branch.getPotential ().getUtilityVariable ().isTemporal ())
                    {
                        branch.getPotential ().setUtilityVariable (probNet.getShiftedVariable (branch.getPotential ().getUtilityVariable (),
                                                                                               timeDifference));
                    }
                }
            }
        }
        return copiedTree;
    }

    public Object clone ()
        throws CloneNotSupportedException
    {
        return this.clone ();
        // TODO seguir clonando hacia abajo; hay que clonar tambien las ramas y
        // los potenciales
    }

    @Override
    public Potential copy ()
    {
        return new TreeADDPotential (this);
    }

    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role
     * @param variables
     * @param role
     */
    public static boolean validate (ProbNode probNode, List<Variable> variables, PotentialRole role)
    {
        boolean validate = false;
        // node must have at least one parent node
        if (role == PotentialRole.UTILITY)
        {
            // in variables there is not utility variable
            if (variables.size () >= 1)
            {
                validate = true;
            }
        }
        if (role == PotentialRole.CONDITIONAL_PROBABILITY)
        {
            if (variables.size () >= 2)
            {
                validate = true;
            }
        }
        return validate;
    }

    @Override
    public boolean isUncertain ()
    {
        // If at least one of the leaf potentials has uncertainty then returns
        // true
        boolean hasUncertainty = false;
        for (TreeADDBranch branch : getBranches ())
        {
            Potential branchPotential = branch.getPotential ();
            hasUncertainty = branchPotential.isUncertain ();
            if (hasUncertainty == true) break;
        }
        return hasUncertainty;
    }

    /**
     * Generates a sampled potential
     */
    public Potential sample (Variable simulationIndexVariable)
    {
        TreeADDPotential sampledTree = (TreeADDPotential) this.copy ();
        for (TreeADDBranch branch : sampledTree.getBranches ())
        {
            branch.setPotential (branch.getPotential ().sample (simulationIndexVariable));
        }
        return sampledTree;
    }
  }
