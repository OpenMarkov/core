package org.openmarkov.core.stringformat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

/**
 * @author jrico
 */
@SuppressWarnings({"ConstantExpression", "DuplicateStringLiteralInspection"})
class StringFormatTest {
    
    private static final String TEST_PATTERN = "The {NetName} net created at {CreationDate, date, short} is not a " +
            "{DesiredNetType} network, and this functionality is only available for {DesiredNetType}";
    
    private static final String EXPECTED = "The MyNet net created at 3/6/23 is not a " +
            "Bayesian network, and this functionality is only available for Bayesian";
    
    private static final Locale MESSAGE_LOCALE = new Locale("es");
    
    /**
     * Tests the generated String is the same as {@link StringFormatTest#EXPECTED}.
     */
    @Test final void apply() {
        Map<String, Object> values = Map.of(
                "NetName", "MyNet",
                "CreationDate", java.util.Date.from(Instant.parse("2023-06-03T10:15:30.00Z")),
                "DesiredNetType", "Bayesian"
        );
        Locale.setDefault(MESSAGE_LOCALE);
        String formatedMessage = StringFormat.apply(StringFormatTest.TEST_PATTERN, values);
        Assertions.assertEquals(StringFormatTest.EXPECTED, formatedMessage);
    }
}