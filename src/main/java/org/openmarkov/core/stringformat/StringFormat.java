package org.openmarkov.core.stringformat;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Allows creating messages using messages defined in a natural language way, being based on
 * {@link MessageFormat}, but instead of accessing values through indexes, they are accessed by name.
 * <p>
 * Having the following message pattern:
 * <pre>{@code "The {NetName} net created at {CreationDate, date, short} is not a {DesiredNetType} network, and this functionality is only available for {DesiredNetType}"}</pre>
 * ... you can apply the arguments of {@code NetName}, {@code CreationDate}, and {@code DesiredNetType}.
 * <br><br>
 * In this example:
 * <pre>{@code
 * String pattern = "The {NetName} net created at {CreationDate,date,short} is not a {DesiredNetType} network," +
 *         " and this functionality is only available for {DesiredNetType}";
 * Map<String, Object> values = Map.of(
 *         "NetName", "MyNet",
 *         "CreationDate", Date.UTC(2023 - 1900, 5, 3, 12, 12, 15),
 *         "DesiredNetType", "Bayesian"
 * );
 * String formatedMessage = StringFormat.apply(pattern, values);
 * System.out.println(formatedMessage);
 * }</pre>
 * <p><br>
 * ... the output will print:
 * <pre>{@code The MyNet net created at 03/06/2023 is not a Bayesian network, and this functionality is only available for Bayesian}</pre>
 *
 * @author jrico
 */
@SuppressWarnings("UnnecessaryJavaDocLink")
public class StringFormat {
    
    private static final Pattern NAMED_PARAMETER_REGEX = Pattern.compile("(?x)" +
                                                                                 "\\{" +
                                                                                 "\\s*(?<name>\\w+?)\\s*" +
                                                                                 "(,\\s*(?<format>\\w+?)\\s*)?" +
                                                                                 "(,\\s*(?<style>\\w+?)\\s*)?" +
                                                                                 "(?<unused>,\\w*?)?" +
                                                                                 "}");
    
    /**
     * Gets the {@code arguments} names of a {@code pattern} following the format of {@link StringFormat}.
     *
     * @return the {@code arguments} names of a {@code pattern}.
     */
    public static List<String> extractParameterNames(CharSequence pattern) {
        var alreadyFoundParameters = new HashSet<String>();
        return StringFormat.NAMED_PARAMETER_REGEX
                .matcher(pattern)
                .results()
                .map(match -> match.group(1))
                .filter(alreadyFoundParameters::add)
                .toList();
    }
    
    /**
     * Applies the {@code arguments} to a {@code pattern} following the rules indicated at {@link StringFormat}
     *
     * @return Result of applying {@code arguments} to a {@code pattern}.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static String apply(CharSequence pattern, Map<String, Object> arguments) {
        return StringFormat.NAMED_PARAMETER_REGEX
                .matcher(pattern)
                .replaceAll(matchResult -> {
                    var argName = matchResult.group(1);
                    var format = matchResult.group(3);
                    var style = matchResult.group(5);
                    if (!arguments.containsKey(argName)) {
                        return ">>>" + argName + "<<<";
                    }
                    var argument = arguments.get(argName);
                    if (argument == null) {
                        return ">>>" + argName + " is null<<<";
                    }
                    if (style != null) {
                        String formatting = "{0," + format + "," + style + "}";
                        try {
                            return MessageFormat.format(formatting, argument);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            new Exception("Cannot use formatting and styling options " + formatting + " to format the "
                                                  + argument.getClass().getName() + " argument " + argument, e)
                                    .printStackTrace();
                        }
                    }
                    if (format != null) {
                        String formatting = "{0," + format + "}";
                        try {
                            return MessageFormat.format(formatting, argument);
                        } catch (IllegalArgumentException | NullPointerException e) {
                            new Exception("Cannot use formatting options " + formatting + " to format the "
                                                  + argument.getClass().getName() + " argument " + argument, e)
                                    .printStackTrace();
                        }
                    }
                    return argument.toString();
                });
    }
    
}
