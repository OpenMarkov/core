package org.openmarkov.core.inference;

import java.util.ArrayList;
import java.util.Hashtable;

import org.openmarkov.core.model.network.Variable;

public class Strategy {
	
	Hashtable<Variable,Policy> strategy;
	
	
	public class Policy{
		
		
		
	}

	public ArrayList<Variable> getDomainOfPolicy(Variable varDecision) {
		//return getPolicy(varDecision).getDomain();
		//TODO
		return null;
	}

	public Object getPolicy(Variable varDecision) {
		return strategy.get(varDecision);
	}

}
