/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.action;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

/** Creates a directed or undirected link between two nodes associated to two
 * variables in a <code>ProbNet</code> */
@SuppressWarnings("serial")
public class AddLinkEdit extends BaseLinkEdit {


	// Constructor
	/** @param probNet <code>ProbNet</code>
	 * @param variable1 <code>Variable</code>
	 * @param variable2 <code>Variable</code>
	 * @param isDirected <code>boolean</code> */
	public AddLinkEdit(ProbNet probNet, Variable variable1, Variable variable2, 
			boolean isDirected) {
		super(probNet, variable1, variable2, isDirected);
	}

	// Methods
	@Override
	/** @throws exception <code>Exception</code> */
	public void doEdit() throws DoEditException {
		try {
			probNet.addLink(variable1, variable2, isDirected);
		} catch (NodeNotFoundException e) {
			throw new DoEditException(e.getMessage() + e.getStackTrace());
		}
	}

	public void undo() {
		super.undo();
		probNet.removeLink(variable1, variable2, isDirected);
	}
   
    /** Method to compare two AddLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     * @param obj
     * @return
     */
    public int compareTo(AddLinkEdit obj){
        int result;

        if (( result = variable1.getName().compareTo(obj.getVariable1().
                getName())) != 0)
            return result;
        if (( result = variable2.getName().compareTo(obj.getVariable2().
                getName())) != 0)
            return result;
        else
            return 0;
    }

	@Override
	public String getOperationName() {
		return "Add";
	}
	
}
