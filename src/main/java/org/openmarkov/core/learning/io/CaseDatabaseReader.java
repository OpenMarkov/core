package org.openmarkov.core.learning.io;

import java.io.IOException;

import org.openmarkov.core.exception.InvalidStateException;

public interface CaseDatabaseReader
{
    public CaseDatabase load(String filename) throws IOException, InvalidStateException;
}
