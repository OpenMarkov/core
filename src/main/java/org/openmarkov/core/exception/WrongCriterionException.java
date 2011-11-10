package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.Variable;

@SuppressWarnings("serial")
public class WrongCriterionException extends Exception {

	public WrongCriterionException(Variable utilityVariable, String criterion,
			Variable decisionCriteria) {
		super("The criterion for the utility variable " + 
				utilityVariable.getName() + ", which is " + criterion +
				", does not match any of the values of the decisionCriteria" +
				"variable.");
	}

	
	
}
