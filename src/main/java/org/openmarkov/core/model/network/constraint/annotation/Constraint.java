package org.openmarkov.core.model.network.constraint.annotation;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;

public @interface Constraint
{
    String name ();
    ConstraintBehavior defaultBehavior ();
}
