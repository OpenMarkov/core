package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.core.stringformat.StringFormat;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents objects that could be represented by a String in a Localization Bundle file.
 *
 * @author jrico
 */
@FunctionalInterface
public interface Localizable {
    
    /**
     * Returns the key/path of the localization key in the Bundle file.
     *
     * @return The key/path of the localization key in the Bundle file.
     */
    @NotNull String path();
    
    /**
     * Returns the name of the Bundle file where the key should be localized.
     *
     * @return The name of the Bundle file where the key should be localized.
     */
    @Nullable default String bundle() {
        return null;
    }
    
    /**
     * Localizes the current object using a default {@link LocalizationFormatter}.
     * <p>
     * In the default implementation, this {@link LocalizationFormatter} is {@link LocalizationFormatter#DEFAULT}.
     *
     * @return a localized String representing this class.
     */
    @NotNull default String localize() {
        return this.localize(LocalizationFormatter.DEFAULT);
    }
    
    /**
     * Localizes the current object based on the given {@link LocalizationFormatter} in order to give a String
     * representing it.
     * <p>
     * The default implementation retrieves a Localized String using the {@link Localizable#bundle()} and
     * {@link Localizable#path()} over the {@link StringDatabase#getUniqueInstance()}. If the String is found, it will
     * format said String using {@link StringFormat#apply(CharSequence, Map)}, where the arguments are the fields of the
     * implementor.
     * <p>
     * If no localization String is found, it returns the name of the class surrounded by >>><<< as
     * {@link StringDatabase#surrondAsUnknown(String)} does.
     *
     * @param formatter the {@link LocalizationFormatter} used to format the object.
     * @return the localized string if available.
     */
    @NotNull default String localize(LocalizationFormatter formatter) {
        Optional<String> optionalLocalizedString = this.findLocalizedString(formatter);
        if (optionalLocalizedString.isEmpty()) {
            return StringDatabase.surrondAsUnknown(this.path());
        }
        String localizedString = optionalLocalizedString.get();
        if (!StringFormat.isStringFormatUsed(localizedString)) {
            return localizedString;
        }
        HashMap<String, Object> fields = StringFormat.extractFieldsToMap(this);
        fields.put("this", this);
        return StringFormat.apply(localizedString, fields);
    }
    
    /**
     * Searches for the first localization String in the bundles matching the {@link LocalizationFormatter}.
     *
     * @param formatter The {@link LocalizationFormatter} that specifies the desired format for the localization string.
     * @return An {@link Optional} containing the first matching localized string found.
     */
    @SuppressWarnings("SimplifyForEach")
    private @NotNull Optional<String> findLocalizedString(LocalizationFormatter formatter) {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        ArrayList<Supplier<String>> localizationAccessors = new ArrayList<>();
        String bundle = this.bundle();
        String path = this.path();
        if (bundle != null) {
            Localizable.lengthOrder(formatter.desiredLength)
                       .forEach(length -> localizationAccessors.add(
                               () -> stringDatabase.getNullableString(bundle, path + "." + length.toString()
                                                                                                 .toLowerCase())));
            localizationAccessors.add(() -> stringDatabase.getNullableString(bundle, path));
        }
        Localizable.lengthOrder(formatter.desiredLength)
                   .forEach(length -> localizationAccessors.add(
                           () -> stringDatabase.getNullableString(path + "." + length.toString()
                                                                                     .toLowerCase())));
        localizationAccessors.add(() -> stringDatabase.getNullableString(path));
        return localizationAccessors.stream().map(Supplier::get).filter(Objects::nonNull).findFirst();
    }
    
    /**
     * Gets in which order the Formatters should be discovered in the
     * {@link Localizable#findLocalizedString(LocalizationFormatter)} function.
     *
     * @param length the specified {@link LocalizationFormatter.LocalizationFormatterLength}.
     * @return a stream of {@link LocalizationFormatter.LocalizationFormatterLength} values in a specific discovery
     * order.
     */
    private static @NotNull Stream<LocalizationFormatter.LocalizationFormatterLength>
    lengthOrder(@NotNull LocalizationFormatter.LocalizationFormatterLength length) {
        return switch (length) {
            case UNSPECIFIED -> Stream.empty();
            case SHORT -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.SHORT,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE);
            case MEDIUM -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE);
            case LONG -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT);
            case VERBOSE -> Stream.of(
                    LocalizationFormatter.LocalizationFormatterLength.VERBOSE,
                    LocalizationFormatter.LocalizationFormatterLength.LONG,
                    LocalizationFormatter.LocalizationFormatterLength.MEDIUM,
                    LocalizationFormatter.LocalizationFormatterLength.SHORT);
        };
    }
    
}
