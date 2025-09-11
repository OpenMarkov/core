package org.openmarkov.core.exception;

//TODO: Catches of this class just show the exception and ignore it, leading to further bugs.
public abstract sealed class ParsingSourceException extends Exception implements IBundledOpenMarkovException {
    
    public static final class CouldNotParseSourceException extends ParsingSourceException {
        public final Exception originException;
        
        public CouldNotParseSourceException(Exception originException) {
            this.originException = originException;
        }
    }
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
    
    
}
