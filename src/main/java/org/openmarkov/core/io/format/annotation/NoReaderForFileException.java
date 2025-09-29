package org.openmarkov.core.io.format.annotation;

import org.openmarkov.core.exception.IBundledOpenMarkovException;

import java.net.URL;

public class NoReaderForFileException extends Exception implements IBundledOpenMarkovException {
    
    public NoReaderForFileException(String fileExtension, String fileVersion, URL url) {
        this.fileExtension = fileExtension;
        this.fileVersion = fileVersion;
        this.url = url;
    }
    
    public final String fileExtension;
    public final String fileVersion;
    public final URL url;
    
    @Override public String toString() {
        return IBundledOpenMarkovException.toString(this);
    }
}
