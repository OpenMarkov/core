/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database.exception;

import org.openmarkov.core.exception.OpenMarkovException;

/**
 * Thrown when opening the model net throws an exception
 *
 * @author Inigo
 */
@OpenMarkovException(name = "UnableToOpenModelNet")
@SuppressWarnings("serial") public class UnableToOpenModelNet extends Exception {
}
