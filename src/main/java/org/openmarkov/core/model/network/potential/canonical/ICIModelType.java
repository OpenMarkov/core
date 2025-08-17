/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import java.io.Serializable;

public enum ICIModelType implements Serializable {
    OR,
    CAUSAL_MAX,
    GENERAL_MAX,
    AND,
    CAUSAL_MIN,
    GENERAL_MIN,
    TUNING;
    
    public ICIFamily getFamily() {
        return switch (this) {
            case OR, CAUSAL_MAX, GENERAL_MAX -> ICIFamily.OR;
            case AND, CAUSAL_MIN, GENERAL_MIN -> ICIFamily.AND;
            case TUNING -> ICIFamily.TUNING;
        };
	}

}
