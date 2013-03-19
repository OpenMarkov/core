/*
 * Copyright 2011 CISIAD, UNED, Spain Licensed under the European Union Public
 * Licence, version 1.1 (EUPL) Unless required by applicable law, this code is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

public class SamplePotentialTable
{
    Variable               simulationIndexVariable;
    private TablePotential inputTablePotential;

    public SamplePotentialTable (TablePotential potential, Variable simulationIndexVariable)
    {
        this.inputTablePotential = potential;
        this.simulationIndexVariable = simulationIndexVariable;
    }

    /**
     * @param simulationIndexVariable Variable indexing the number of
     *            simulation. The number of simulations performed is the number
     *            of states of this variable
     * @return A sampled potential table
     * @throws NotEnoughMemoryException
     */
    public TablePotential getSampledTable ()
    {
        TablePotential sampledTablePotential = null;
        int inputTableSize;
        int[] indexesComplement = null;
        int[] indexesDirichlet = null;
        int[] indexesOther = null;
        ComplementFamily complementFamily = null;
        DirichletFamily dirFamily = null;
        FamilyDistribution otherFamily = null;
        List<TypeProbDensityFunction> auxTypes;
        auxTypes = new ArrayList<TypeProbDensityFunction> ();
        auxTypes.add (TypeProbDensityFunction.COMPLEMENT);
        auxTypes.add (TypeProbDensityFunction.DIRICHLET);
        List<UncertainValue> uncertainValues = null;
        double[] sampledConfigurationValues;
        int numStates;
        int numSimulations;
        numSimulations = simulationIndexVariable.getNumStates ();
        UncertainValue[] uTable = inputTablePotential.getUncertainTable ();
        double[] originalValues = inputTablePotential.getValues ();
        if (!(inputTablePotential.getUncertainTable () == null))
        {
            List<Variable> inputPotentialVariables = inputTablePotential.getVariables ();
            List<Variable> sampledPotentialVariables = (ArrayList<Variable>) inputPotentialVariables;
            sampledPotentialVariables.add (simulationIndexVariable);
            sampledTablePotential = new TablePotential (sampledPotentialVariables,
                                                        inputTablePotential.getPotentialRole ());
            double[] sampledValues = sampledTablePotential.values;
            if (!inputTablePotential.isUtility ())
            {// Probability potential
                numStates = inputPotentialVariables.get (0).getNumStates ();
            }
            else
            {// Utility potential
                numStates = 1;
            }
            sampledTablePotential.setUtilityVariable (inputTablePotential.getUtilityVariable ());
            sampledConfigurationValues = new double[numStates];
            // Number of configurations of the conditioning variables
            inputTableSize = inputTablePotential.getTableSize ();
            int numConfigurations = inputTableSize / numStates;
            boolean hasUncertainty;
            // iterates over the configurations
            for (int configurationIndex = 0; configurationIndex < numConfigurations; configurationIndex++)
            {
                int configurationBasePosition = numStates * configurationIndex;
                uncertainValues = getUncertainValuesChance (uTable, configurationBasePosition,
                                                            numStates);
                hasUncertainty = uncertainValues.get (0) != null;
                if (hasUncertainty)
                {
                    FamilyDistribution family = new FamilyDistribution (uncertainValues);
                    List<UncertainValue> familyList = family.family;
                    // calculates the indexes of the uncertain values for each
                    // group: Other, Dirichlet and Complement
                    indexesComplement = getIndexesUncertainValuesOfType (familyList,
                                                                         TypeProbDensityFunction.COMPLEMENT);
                    indexesDirichlet = getIndexesUncertainValuesOfType (familyList,
                                                                        TypeProbDensityFunction.DIRICHLET);
                    indexesOther = getIndexesUncertainValuesNotInTypes (familyList, auxTypes);
                    // Create the families of distributions
                    List<UncertainValue> complements = constructListFromIndexes (familyList,
                                                                                 indexesComplement);
                    List<UncertainValue> dirichlets = constructListFromIndexes (familyList,
                                                                                indexesDirichlet);
                    List<UncertainValue> others = constructListFromIndexes (familyList,
                                                                            indexesOther);
                    complementFamily = new ComplementFamily (complements);
                    dirFamily = new DirichletFamily (dirichlets);
                    otherFamily = new FamilyDistribution (others);
                    // Initialize the random seed and the random number
                    // generator in the Dirichlet family
                    /*
                     * setAndInitializeRandomStreamGenerator(dirFamily,
                     * arrayFamily);
                     */
                }
                else
                {
                    hasUncertainty = false;
                    // takes the values from the original potential and places
                    // them in the auxiliary vector 'sampledConfigurationValues'
                    for (int stateIndex = 0; stateIndex < numStates; stateIndex++)
                    {
                        sampledConfigurationValues[stateIndex] = originalValues[configurationBasePosition
                                                                                + stateIndex];
                    }
                }
                for (int simulationIndex = 0; simulationIndex < numSimulations; simulationIndex++)
                {
                    if (hasUncertainty)
                    {
                        // samples and places the results in the auxiliary
                        // vector 'sampledConfigurationValues'
                        sampledConfigurationValues = generateSample (otherFamily, dirFamily,
                                                                     complementFamily,
                                                                     indexesOther,
                                                                     indexesDirichlet,
                                                                     indexesComplement, numStates);
                    }
                    // copies the auxiliary them in the auxiliary vector
                    // 'sampledConfigurationValues'
                    for (int stateIndex = 0; stateIndex < numStates; stateIndex++)
                    {
                        sampledValues[inputTableSize * simulationIndex + configurationBasePosition
                                      + stateIndex] = sampledConfigurationValues[stateIndex];
                    }
                }
            }
        }
        else
        {// There is no uncertainty for the input potential
            return inputTablePotential;
        }
        return sampledTablePotential;
    }

    /*
     * private void setAndInitializeRandomStreamGenerator( DirichletFamily
     * dirFamily, ArrayList<UncertainValue> arrayFamily) { for (UncertainValue
     * aux:arrayFamily){ aux.createRandomGenerator(); aux.initializeGenerator();
     * } // dirFamily.createRandomGenerator(); //
     * dirFamily.initializeGenerator(); }
     */
    private double[] generateSample (FamilyDistribution otherFamily,
                                     DirichletFamily dirFamily,
                                     ComplementFamily complementFamily,
                                     int[] indexesOther,
                                     int[] indexesDirichlet,
                                     int[] indexesComplement,
                                     int numStates)
    {
        double[] sampleOther;
        double[] sampleDir;
        double massForComp;
        double[] sampledConfigurationValues = new double[numStates];
        // processes the uncertain values that can be sampled individually
        sampleOther = otherFamily.getSample ();
        placeInArray (sampledConfigurationValues, indexesOther, sampleOther);
        // processes Dirichlet
        sampleDir = dirFamily.getSample ();
        placeInArray (sampledConfigurationValues, indexesDirichlet, sampleDir);
        // Process complements
        massForComp = 1.0 - (Tools.sum (sampleOther));
        complementFamily.setProbMass (massForComp);
        double[] sampleComp = complementFamily.getSample ();
        placeInArray (sampledConfigurationValues, indexesComplement, sampleComp);
        return sampledConfigurationValues;
    }

    private List<UncertainValue> constructListFromIndexes (List<UncertainValue> arrayFamily,
                                                           int[] indComp)
    {
        List<UncertainValue> array = new ArrayList<UncertainValue> ();
        for (int i : indComp)
        {
            array.add (arrayFamily.get (i));
        }
        return array;
    }

    private static void placeInArray (double[] refValue, int[] indexes, double[] x)
    {
        for (int i = 0; i < indexes.length; i++)
        {
            refValue[indexes[i]] = x[i];
        }
    }

    private List<UncertainValue> getUncertainValuesChance (UncertainValue[] uTable,
                                                           int basePos,
                                                           int numStates)
    {
        List<UncertainValue> uv;
        uv = new ArrayList<UncertainValue> ();
        for (int i = 0; i < numStates; i++)
        {
            uv.add (uTable[basePos + i]);
        }
        return uv;
    }

    public boolean hasUncertainValuesUtility (UncertainValue[] uTable, int basePosition)
    {
        return uTable[basePosition] != null;
    }

    /**
     * @param arrayUncertain
     * @param types
     * @return
     */
    private static int[] getIndexesUncertainValuesOfTypes (List<UncertainValue> arrayUncertain,
                                                           List<TypeProbDensityFunction> types)
    {
        List<Integer> indexes = new ArrayList<Integer> ();
        for (int i = 0; i < arrayUncertain.size (); i++)
        {
            UncertainValue aux = arrayUncertain.get (i);
            TypeProbDensityFunction auxType = aux.getProbDensityFunction ().getType ();
            boolean isInTypes = false;
            for (int j = 0; (j < types.size ()) && !isInTypes; j++)
            {
                isInTypes = (auxType == types.get (j));
            }
            if (isInTypes)
            {
                indexes.add (i);
            }
        }
        int numIndexesOfTypes = indexes.size ();
        int[] intIndexes = new int[numIndexesOfTypes];
        for (int i = 0; i < numIndexesOfTypes; i++)
        {
            intIndexes[i] = indexes.get (i);
        }
        return intIndexes;
    }

    public static int[] getIndexesUncertainValuesNotInTypes (List<UncertainValue> arrayUncertain,
                                                             List<TypeProbDensityFunction> types)
    {
        List<Integer> indexes = new ArrayList<Integer> ();
        for (int i = 0; i < arrayUncertain.size (); i++)
        {
            UncertainValue aux = arrayUncertain.get (i);
            TypeProbDensityFunction auxType = aux.getProbDensityFunction ().getType ();
            boolean notInTypes = true;
            for (int j = 0; (j < types.size ()) && notInTypes; j++)
            {
                notInTypes = !(auxType == types.get (j));
            }
            if (notInTypes)
            {
                indexes.add (i);
            }
        }
        int numIndexesOfTypes = indexes.size ();
        int[] intIndexes = new int[numIndexesOfTypes];
        for (int i = 0; i < numIndexesOfTypes; i++)
        {
            intIndexes[i] = indexes.get (i);
        }
        return intIndexes;
    }

    public static int[] getIndexesUncertainValuesOfType (List<UncertainValue> arrayUncertain,
                                                         TypeProbDensityFunction type)
    {
        List<TypeProbDensityFunction> aux = new ArrayList<TypeProbDensityFunction> ();
        aux.add (type);
        return getIndexesUncertainValuesOfTypes (arrayUncertain, aux);
    }
}
