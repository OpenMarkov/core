package org.openmarkov.core.io.exception;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

public class NoReaderForExtension extends Exception implements IBundledOpenMarkovException {
    public NoReaderForExtension(String extension) {
        this.extension = extension;
    }
    
    public final String extension;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
