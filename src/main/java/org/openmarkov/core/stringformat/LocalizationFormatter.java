package org.openmarkov.core.stringformat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

/**
 * Represents information about formatting options indicated by a user. Said information is read for the 'style' field
 * when processing a String in {@link StringFormat#apply(CharSequence, Map)}.
 */
public final class LocalizationFormatter {
    
    public static final LocalizationFormatter DEFAULT = new LocalizationFormatter(
            LocalizationFormatterLength.UNSPECIFIED
    );
    public final @NotNull LocalizationFormatterLength desiredLength;
    
    /**
     * Constructs a new instance of {@code LocalizationFormatter} with the specified formatting length.
     *
     * @param desiredLength The desired length
     */
    private LocalizationFormatter(@NotNull LocalizationFormatterLength desiredLength) {
        this.desiredLength = desiredLength;
    }
    
    /**
     * Parses the {@code format} String to create an instance of {@code LocalizationFormatter}.
     * <p>
     * If the {@code format} parameter is null, then it returns {@link LocalizationFormatter#DEFAULT}.
     *
     * @param format The textual representation of the desired format.
     * @return The result of parsing the input as a {@link LocalizationFormatter}.
     */
    public static LocalizationFormatter of(@Nullable String format) {
        if (format == null || format.isBlank()) {
            return LocalizationFormatter.DEFAULT;
        }
        format = format.toLowerCase();
        String finalFormat = format;
        var desiredLength = Arrays.stream(LocalizationFormatterLength.values())
                                  .filter(length -> finalFormat.contains(length.toString().toLowerCase()))
                                  .findFirst()
                                  .orElse(LocalizationFormatter.DEFAULT.desiredLength);
        return new LocalizationFormatter(desiredLength);
    }
    
    /**
     * Represents multiples levels of formatting detail.
     */
    public enum LocalizationFormatterLength {
        UNSPECIFIED, SHORT, MEDIUM, LONG, VERBOSE
    }
    
}
