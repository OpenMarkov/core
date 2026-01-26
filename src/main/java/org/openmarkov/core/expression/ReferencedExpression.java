package org.openmarkov.core.expression;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The difference between a normal expression (A {@link String}) and a ReferencedExpression is that the latter
 * holds references to the variables that are used in the expression.
 */
public class ReferencedExpression<T> {
    private static final Pattern EXTRACT_VARIABLE_PATTERN = Pattern.compile("\\{([^\\{]+?)\\}");
    private final List<T> references;
    private final Function<T, String> stringifyReference;
    private final List<? extends ExpressionContent<T>> contents;
    
    public ReferencedExpression(Map<String, T> references, String expression, Function<T, String> stringifyReference) {
        this.references = new ArrayList<>();
        this.stringifyReference = stringifyReference;
        var allRanges = splitAll(EXTRACT_VARIABLE_PATTERN, expression);
        this.contents = allRanges
                .stream()
                .map(subString -> {
                    Matcher matcher = EXTRACT_VARIABLE_PATTERN.matcher(subString);
                    if (!matcher.find()) {
                        return new ExpressionContent.UnparsedExpression<T>(subString);
                    }
                    if (!references.containsKey(matcher.group(1))) {
                        return new ExpressionContent.UnparsedExpression<T>(subString);
                    }
                    return new ExpressionContent.VariableReference<T>(references.get(matcher.group(1)));
                }).toList();
    }
    
    public final String asStringExpression() {
        return contents.stream()
            .map(content -> (ExpressionContent<T>) content) // Forzamos el tipo aquí
            .map(expressionContent -> switch (expressionContent) {
                case ExpressionContent.VariableReference<T> vr -> 
                        "{" + this.stringifyReference.apply(vr.reference()) + "}";
                case ExpressionContent.UnparsedExpression<T> ue -> 
                        ue.unparsed();
            }).collect(Collectors.joining());
    }
    
    /**
     * Splits the input string into substrings where the pattern matches and where it does not.
     */
    private static @NotNull List<String> splitAll(Pattern pattern, String input) {
        var matchesRanges = pattern.matcher(input).results()
                                   .map(matchResult -> new Range(matchResult.start(), matchResult.end()))
                                   .toList();
        
        var unmatchesRanges = new ArrayList<Range>();
        if (matchesRanges.isEmpty()) {
            unmatchesRanges.add(new Range(0, input.length()));
        }
        if (!matchesRanges.isEmpty() && matchesRanges.getFirst().start() != 0) {
            unmatchesRanges.add(new Range(0, matchesRanges.getFirst().start()));
        }
        if (!matchesRanges.isEmpty() && matchesRanges.getLast().end() != input.length()) {
            unmatchesRanges.add(new Range(matchesRanges.getLast().end(), input.length()));
        }
        IntStream.range(0, matchesRanges.size() - 1).forEach(i -> {
            unmatchesRanges.add(new Range(matchesRanges.get(i).end(), matchesRanges.get(i + 1).start()));
        });
        return Stream.concat(unmatchesRanges.stream(), matchesRanges.stream())
                     .sorted(Comparator.comparing(Range::start))
                     .map(range -> input.substring(range.start(), range.end()))
                     .toList();
    }
    
    record Range(int start, int end) {
    }
    
    sealed interface ExpressionContent<T> {
        record VariableReference<T>(T reference) implements ExpressionContent<T> {
        }
        
        record UnparsedExpression<T>(String unparsed) implements ExpressionContent<T> {
        }
    }
    
    
}
