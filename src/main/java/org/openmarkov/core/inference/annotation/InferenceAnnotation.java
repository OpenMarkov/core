/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference.annotation;

import org.openmarkov.core.annotation.Limits;
import org.openmarkov.core.annotation.RequiredConstructors;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.ProbNet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class sets the labels for the annotations inference
 * @author myebra
 * @author mpalacios
 */

@Limits(classesThatCanBeAnnotated = InferenceAlgorithm.class,
		requiredConstructors = @RequiredConstructors(ProbNet.class))
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface InferenceAnnotation {
	/**
	 * Gets the name of the class
	 * @return The name of the inference algorithm
	 */
	String name();
}