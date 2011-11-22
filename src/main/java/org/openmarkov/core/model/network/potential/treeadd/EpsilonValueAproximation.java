package org.openmarkov.core.model.network.potential.treeadd;

public class EpsilonValueAproximation {
	static public double value;
	
	static {
		value= 1.0f;
		
		do {
		   value /= 2.0f;
		}
		while ((double)(1.0 + (value/2.0)) != 1.0);
	}
}
