package org.openmarkov.core.model.network.constraint.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;

@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface Constraint
{
    String name ();
    ConstraintBehavior defaultBehavior ();
}
