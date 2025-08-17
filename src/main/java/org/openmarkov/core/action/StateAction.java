/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action;

import java.io.Serializable;

/**
 * Defines the different actions on the state an PartitionedInterval objects
 *
 * @author mpalacios
 * @version 1.0
 */
public enum StateAction implements Serializable {
    ADD,
    REMOVE,
    RENAME,
    UP,
    DOWN,
    MODIFY_DELIMITER_INTERVAL,
    MODIFY_VALUE_INTERVAL;
}
