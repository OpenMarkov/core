package org.openmarkov.core.learning.io;

import java.io.IOException;

import org.openmarkov.core.model.network.ProbNet;

public interface CaseDatabaseWriter
{
    public void save(String filename, ProbNet probnet, int[][] cases) throws IOException;
}
