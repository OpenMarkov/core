/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface ProbDensFunctionType {
	String name();

	//CMI To be removed
	//For Univariate
	String univariateName() default "default";
	//CMF

	//CMI 14/03/2020
	//Distributions may be used to calculate TTE in a DesNET
	boolean isValidForTTE() default false;
	//
	boolean isValidForProbabilities() default true;

	boolean isValidForNumeric() default true;

	String[] parameters();
}
