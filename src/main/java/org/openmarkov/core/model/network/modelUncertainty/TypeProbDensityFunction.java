/*
* Copyright 2011 CISIAD, UNED, Spain
*
* Licensed under the European Union Public Licence, version 1.1 (EUPL)
*
* Unless required by applicable law, this code is distributed
* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
*/

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.ArrayList;

public enum TypeProbDensityFunction {
	EXACT("Exact"),
	RANGE("Range"),
	TRIANGULAR("Triangular"),
	NORMAL("Normal"),
	LOGNORMAL("LogNormal"),
	GAMMA("Gamma"),
	GAMMAMV("Gamma-mv"),
	BETA("Beta"),	
	DIRICHLET("Dirichlet"),
	COMPLEMENT("Complement"),
	EXPONENTIAL("Exponential"),
	ERLANG("Erlang"),
	STANDARDNORMAL("StandardNormal");

	
	private String name;

	TypeProbDensityFunction(String name) {
		this.name = name;
	}
	
	
	public static TypeProbDensityFunction valueEnumOf(String str){
		TypeProbDensityFunction type = null;
		boolean found = false;
		
		for (int i=0;(i<values().length)&&!found;i++){
			TypeProbDensityFunction iValues = values()[i];
			if (iValues.toString().equalsIgnoreCase(str)){
				found = true;
				type = iValues;
			}
		}
		return type;
	}
    
    public String toString() {
    	return name;
    }
    
    
    public static String[] getStringsValues(){
    	TypeProbDensityFunction[] values = TypeProbDensityFunction.values();
		String[] strings = new String[values.length];
		for (int i=0;i<strings.length;i++){
			strings[i]=values[i].toString();
		}
		return strings;
    }

	public static String[] getAllowedStringsValues(boolean isChance) {
		// TODO Auto-generated method stub
		TypeProbDensityFunction[] values = TypeProbDensityFunction.values();
		ArrayList<String> strings = new ArrayList<String>();
		String[] strReturn;
		for (int i=0;i<values.length;i++){
			TypeProbDensityFunction type = values[i];
			if (ProbDensFunction.isPossibleDistribution(type,isChance)){
				strings.add(type.toString());
			}
		}
			
		strReturn = new String[strings.size()];
	    for (int i=0;i<strings.size();i++){
	    	strReturn[i]=strings.get(i);
	    }
		return strReturn;
	}
}
