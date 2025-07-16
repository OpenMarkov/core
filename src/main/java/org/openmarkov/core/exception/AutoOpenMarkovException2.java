package org.openmarkov.core.exception;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.AutoLocalizable;

import java.util.List;

public abstract class AutoOpenMarkovException2 extends OpenMarkovException2 implements AutoLocalizable {
    
    @Override protected @Nullable String getExceptionMessage() {
        return this.localize();
    }
    
    private static final List<String> UPPERCASE_LETTERS = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    
    @Override protected @Nullable String getExceptionTitle() {
        String simpleName = this.getClass().getSimpleName();
        if (simpleName.endsWith("Exception")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Exception".length());
        }
        for (var uppercaseLetter : UPPERCASE_LETTERS) {
            simpleName = simpleName.replace(uppercaseLetter, " " + uppercaseLetter.toLowerCase());
        }
        while (simpleName.startsWith(" ")) {
            simpleName = simpleName.substring(1);
        }
        if (!simpleName.isBlank()) {
            simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        }
        return simpleName.replace("\\n", System.lineSeparator());
    }
    
}
