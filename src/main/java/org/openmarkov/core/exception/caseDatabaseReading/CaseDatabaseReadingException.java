package org.openmarkov.core.exception.caseDatabaseReading;

import org.openmarkov.core.exception.AutoOpenMarkovException2;

import java.io.IOException;

public abstract class CaseDatabaseReadingException extends AutoOpenMarkovException2 {
    
    public static IODatabaseReadingException of(IOException exception){
        return new IODatabaseReadingException(exception);
    }
    
    public static class IODatabaseReadingException extends CaseDatabaseReadingException {
        public final IOException exception;
        public IODatabaseReadingException(IOException exception) {
            this.exception = exception;
        }
    }
    
}