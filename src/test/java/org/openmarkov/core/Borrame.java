package org.openmarkov.core;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.InvalidArgumentException;

public class Borrame {
    
    public static void main(String[] args) {
        System.out.println(new ConstraintViolatedException(null).localize());
        System.out.println(new ConstraintViolatedException.LinkAlreadyExists(null, null, null));
    }
}
