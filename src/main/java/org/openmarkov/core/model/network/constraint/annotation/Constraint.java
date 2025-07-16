/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint.annotation;

import org.openmarkov.core.annotation.Limits;
import org.openmarkov.core.annotation.RequiredConstructors;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.PNConstraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Limits(classesThatCanBeAnnotated = PNConstraint.class, requiredConstructors = @RequiredConstructors({}))
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface Constraint {
	String name();

	ConstraintBehavior defaultBehavior();
}
