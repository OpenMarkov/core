/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.potential;

import java.util.ArrayList;

import org.openmarkov.core.exception.NotEnoughMemoryException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.operation.Util;


/** A generalized <code>TablePotential</code> that contains an 
 * <code>Objects</code> table of the same type: <code>Element</code>. */
public class GTablePotential<Element> extends TablePotential {

    // Attributes
    /** The array buffer into which the elements of the 
     *   <code>GeneralizedTablePotential</code> are stored. This attribute is 
     *   public for the sake of efficiency. */
    public ArrayList<Element> elementTable; 

    // Constructor
    public GTablePotential(ArrayList<Variable> variables, PotentialRole role) 
            throws NotEnoughMemoryException {
        super(variables, null); // <- Don't create a table of doubles
        if (numVariables != 0) {
            int sizeTable = dimensions[numVariables - 1] * 
                offsets[numVariables - 1];
            try {
                elementTable = new ArrayList<Element>(sizeTable);
            } catch (Exception e) {
    			long freeMemory = runtime.freeMemory();
                throw new NotEnoughMemoryException("There are only "
                		+ Util.printInteger(freeMemory) + " bytes free. "
    					+ "Not enough memory to allocate a table with "
    					+ sizeTable +" elements in GTablePotential constructor." 
    					+ " Number of variables: " + variables.size());
            }
        } else {// In this case the potential is a constant
            elementTable =  new ArrayList<Element>(1);
        }
    }
    
    public GTablePotential (Potential potential) throws NotEnoughMemoryException
    {
        this (potential.getVariables (), potential.getPotentialRole ());
    }
    
    
	@Override
	public PotentialType getPotentialType() {
		return PotentialType.GTABLE;
	}
    
	/** Overrides <code>toString</code> method. Mainly for test purposes */
	public String toString() {
		// writes the variables names
		String string = new String(numVariables + " Variables: ");
		if (numVariables > 0) {
			string = string + variables.get(0).getName();
			for (int i = 1; i < numVariables; i++) {
				string = string + ", " + variables.get(i).getName();
			}
			if (role == PotentialRole.UTILITY ) {
				string += " - Utility potential";
			}
			string = string + "\n";

			// writes each configuration and its value
			int[] configuration = null;
			int configurationsPerLine = 1;
			for (int i = 0; dimensions != null && i < dimensions.length
					&& i < 2; i++) {
				configurationsPerLine *= dimensions[i];
			}
			int numElementsTable = elementTable.size();
			for (int i = 0; i < numElementsTable; i++) {
				if (dimensions != null) {
					configuration = getConfiguration(i);
				}
				string = string + "[";
				for (int j = 0; configuration != null
						&& j < configuration.length; j++) {
					string = string + configuration[j];
				}
				string = string + "]: " + elementTable.get(i).toString();
				if (((i + 1) % configurationsPerLine == 0)
						|| (i == numElementsTable - 1)) {
					string = string + "\n";
				} else {
					string = string + ", ";
				}
			}
		}
		return string;
	}

}
