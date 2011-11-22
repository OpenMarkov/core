package org.openmarkov.core.learning.io;

import java.io.IOException;

import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.model.network.ProbNet;

public interface CaseDatabaseReader
{
    public int[][] load(String filename, ProbNet probnet) throws IOException, InvalidStateException;
}
