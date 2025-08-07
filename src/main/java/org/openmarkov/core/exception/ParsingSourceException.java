package org.openmarkov.core.exception;

public abstract sealed class ParsingSourceException extends BundledOpenMarkovException {
    
    public static final class CouldNotParseSourceException extends ParsingSourceException {
        public final Exception originException;
        
        public CouldNotParseSourceException(Exception originException) {
            this.originException = originException;
        }
    }
    
}
