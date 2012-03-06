/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

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
