package org.openmarkov.core.io.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

public class NoWriterForExtensionException extends Exception implements IBundledOpenMarkovException {
    public NoWriterForExtensionException(String extension) {
        this.extension = extension;
    }
    
    public final String extension;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
