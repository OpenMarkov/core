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
 * Codifies existing node types using this numbers:
 * <ol start="0">
 * <li>CHANCE
 * <li>DECISION
 * <li>UTILITY
 * <li>SV_SUM
 * <li>SV_PRODUCT
 * <li>COST
 * <li>EFFECTIVENESS
 * <li>CE (Cost-Effectiveness)
 * </ol>
 *
 * @author manuel
 * @author fjdiez
 */
public enum NodeType implements Serializable {
    CHANCE,
    DECISION,
    UTILITY,
    SV_SUM,
    SV_PRODUCT;
    
    
    public String toString() {
        return EnumUtils.toCamelCase(this);
	}

}
