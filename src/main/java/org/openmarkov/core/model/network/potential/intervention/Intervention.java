package org.openmarkov.core.model.network.potential.intervention;

public abstract class Intervention {

	// Attributes used in toString()
	protected static String defaultIndentString = "";

	protected String indent = defaultIndentString;
	
	protected int indentLevel;

	protected static int indentIncrement = 4;
	
	private static String stringIndentIncrement = null;
	
	// Methods
	/**
	 * Compares the content of this intervention with the received intervention
	 * @param object. <code>Object</code>
	 * @return boolean
	 */
	public abstract boolean sameAs(Object object);

	/**
	 * If the intervention is a decision the number of branches is 1, otherwise, 
	 * it is the number of states of the chance variable with probability greater than 0.
	 * @return <code>int</code>
	 */
	protected abstract int getNumBranches(); 
	
	/** 
	 * Recursively goes through the interventions tree adding the number of leaves.
	 * @return <code>int</code>
	 */
	protected abstract int getNumLeaves();

	/**
	 * @return <code>False</code> when this intervention is a leaf.
	 */
	public abstract boolean hasAnySubIntervention();

	/** 
	 * Change the indentation in <code>toString()</code>. Used for nested interventions.
	 * @param indentLevel. <code>int</code>
	 */
	public void setIndentLevel(int indentLevel) {
		if (indentLevel != this.indentLevel) {
			this.indentLevel = indentLevel;
			if (indentLevel % indentIncrement == 0) {
				if (stringIndentIncrement == null) {
					createStringIndentIncrement('.');
				}
				indent = "";
				for (int i = 0; i < indentLevel / indentIncrement; i++) {
					indent = indent + stringIndentIncrement;
				}
			} else {
				if (indentLevel == 0) {
					indent = "";
				} else {
					indent = " ";
					for (int i = 1; i < indentLevel; i++) indent = indent + " ";
				}
			}
		}
	}

	/**
	 * Creates a string with a character 'c' in the middle.
	 * @param c. <code>char</code>
	 *  */
	private void createStringIndentIncrement(char c) {
		stringIndentIncrement = "";
		for (int i = 0; i < indentIncrement; i++) {
			if (i == indentIncrement / 2) {
				stringIndentIncrement = stringIndentIncrement + c;
			} else {
				stringIndentIncrement = stringIndentIncrement + " ";
			}
		}
	}
	
}
