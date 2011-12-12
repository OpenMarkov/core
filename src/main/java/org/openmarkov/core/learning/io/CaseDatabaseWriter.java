package org.openmarkov.core.learning.io;

import java.io.IOException;

public interface CaseDatabaseWriter
{
    public void save(String filename, CaseDatabase database) throws IOException;
}
