/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database;

import org.openmarkov.core.annotation.ImplementationRequirements;
import org.openmarkov.core.annotation.RequiredConstructor;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;

import java.io.IOException;

@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public interface CaseDatabaseReader {
	CaseDatabase load(String filename) throws IOException, ParsingSourceException, EmptyDatabaseException;
}
