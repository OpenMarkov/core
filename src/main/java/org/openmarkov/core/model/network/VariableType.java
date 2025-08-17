/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.openmarkov.java.enumUtils.EnumUtils;

import java.io.Serializable;

/**
 * @author manuel
 */
public enum VariableType implements Serializable {
    FINITE_STATES,
    NUMERIC,
    DISCRETIZED;

	public String toString() {
        return EnumUtils.toCamelCase(this);
	}
}
