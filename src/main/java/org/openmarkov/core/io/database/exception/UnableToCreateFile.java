/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database.exception;

import org.openmarkov.core.exception.OpenMarkovException;

/**
 * Thrown when creating the file throws an exception
 *
 * @author Inigo
 */
@OpenMarkovException(name = "UnableToCreateFile")
@SuppressWarnings("serial") public class UnableToCreateFile extends Exception {
}
