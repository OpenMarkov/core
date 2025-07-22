/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.openmarkov.core.model.network.constraint.PNConstraint;

/**
 * Thrown when trying to do an edit that violates one of the
 * {@code PNConstraints} of the {@code ProbNet}
 *
 * @see org.openmarkov.core.model.graph.Link#Link(Object, Object, boolean)
 */
@SuppressWarnings("serial") public class ConstraintViolationException extends AutoOpenMarkovException2 {
	
	public final PNConstraint constraint;
	
	public ConstraintViolationException(PNConstraint constraint) {
        this.constraint = constraint;
    }
	   
}
