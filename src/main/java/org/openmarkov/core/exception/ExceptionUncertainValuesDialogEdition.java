package org.openmarkov.core.exception;

import javax.swing.JOptionPane;

public class ExceptionUncertainValuesDialogEdition extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExceptionUncertainValuesDialogEdition(String message) {
		JOptionPane.showMessageDialog(null,message,"Error",
			    JOptionPane.ERROR_MESSAGE);

	}

}
