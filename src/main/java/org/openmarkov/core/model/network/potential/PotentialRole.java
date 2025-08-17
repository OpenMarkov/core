/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.openmarkov.java.enumUtils.EnumUtils;

/**
 * @author marias
 * @version 1.0
 */
public enum PotentialRole {
    
    CONDITIONAL_PROBABILITY,
    JOINT_PROBABILITY,
    POLICY,
    LINK_RESTRICTION,
    UNSPECIFIED,
    // DECISION,
    // UTILITY,
    // TODO Remove
    // INTERVENTION,
    // UTIL_2
	;
    
    public String toString() {
        return EnumUtils.toCamelCase(this);
	}


}
