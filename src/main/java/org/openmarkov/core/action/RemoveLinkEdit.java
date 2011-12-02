package org.openmarkov.core.action;

import org.apache.log4j.Logger;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class RemoveLinkEdit extends BaseLinkEdit {
	
	private Logger logger;

	// Constructor
	/** @param probNet <code>ProbNet</code>
	 * @param variable1 <code>Variable</code>
	 * @param variable2 <code>Variable</code>
	 * @param isDirected <code>boolean</code> */
	public RemoveLinkEdit(ProbNet probNet, Variable variable1, 
			Variable variable2,	boolean isDirected) {
		super(probNet, variable1, variable2, isDirected);
		this.logger = Logger.getLogger(RemoveLinkEdit.class);
	}

	@Override
	public void doEdit() {
		probNet.removeLink(variable1, variable2, isDirected);
	}
	
	@Override
	public void undo() {
		super.undo();
		try {
			probNet.addLink(variable1, variable2, isDirected);
		} catch (Exception e) {
			logger.fatal (e);
		}
	}

    /** Method to compare two RemoveLinkEdits comparing the names of
     * the source and destination variable alphabetically.
     * @param obj
     * @return
     */
    public int compareTo(RemoveLinkEdit obj){
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
		return "Remove";
	}

	public String toString() {
		if (isDirected) {
			return new String(
					"RemoveLinkEdit: " + variable1 + " --> " + variable2);
		} else {
			return new String(
					"RemoveLinkEdit: " + variable1 + " --- " + variable2);
		}
	}	

}
