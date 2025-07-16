/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.plugin;

import org.openmarkov.core.annotation.Limits;
import org.openmarkov.core.annotation.RequiredConstructors;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Limits(classesThatCanBeAnnotated = Potential.class, requiredConstructors = {
		@RequiredConstructors({List.class, CycleLength.class}),
		@RequiredConstructors({List.class, PotentialRole.class}),
		@RequiredConstructors({List.class})
})
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface PotentialType {
	String name();

	String family() default "";

	String[] altNames() default "";
}
