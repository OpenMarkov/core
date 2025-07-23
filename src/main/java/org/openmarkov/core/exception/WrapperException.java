package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.annotation.Limits;
import org.openmarkov.core.localize.AutoLocalizable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;

@Limits(classesThatCanBeAnnotated = OpenMarkovException2.class)
public interface WrapperException extends AutoLocalizable {
    
    @Override @NotNull default String localize() {
        var openMarkovException = ((OpenMarkovException2) this);
        var cause = openMarkovException.getCause();
        if(cause instanceof Localizable localizable) {
            return localizable.localize();
        }
        if (cause instanceof OpenMarkovException2 causeOpenMarkovException) {
            return causeOpenMarkovException.getExceptionMessage();
        }
        return cause.toString();
    }
    
    @Override @NotNull default String localize(LocalizationFormatter formatter) {
        var openMarkovException = ((OpenMarkovException2) this);
        var cause = openMarkovException.getCause();
        if(cause instanceof Localizable localizable) {
            return localizable.localize(formatter);
        }
        if (cause instanceof OpenMarkovException2 causeOpenMarkovException) {
            return causeOpenMarkovException.getExceptionMessage();
        }
        return cause.toString();
    }
}
