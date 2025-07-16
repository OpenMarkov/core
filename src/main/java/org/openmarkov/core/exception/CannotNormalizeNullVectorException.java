/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

@SuppressWarnings("serial") public class CannotNormalizeNullVectorException extends AutoOpenMarkovException2 {
	
	private final List<Variable> variables;
	
	public CannotNormalizeNullVectorException(List<Variable> variables) {
        this.variables = variables;
    }
	
}
