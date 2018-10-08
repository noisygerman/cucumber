package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegularExpression implements Expression {
    private final Pattern expressionRegexp;
    private final ParameterTypeRegistry parameterTypeRegistry;
    private final TreeRegexp treeRegexp;

    /**
     * Creates a new instance. Use this when the transform types are not known in advance,
     * and should be determined by the regular expression's capture groups. Use this with
     * dynamically typed languages.
     *
     * @param expressionRegexp      the regular expression to use
     * @param parameterTypeRegistry used to look up parameter types
     */
    public RegularExpression(Pattern expressionRegexp, ParameterTypeRegistry parameterTypeRegistry) {
        this.expressionRegexp = expressionRegexp;
        this.parameterTypeRegistry = parameterTypeRegistry;
        this.treeRegexp = new TreeRegexp(expressionRegexp);
    }

    @Override
    public List<Argument<?>> match(String text, Type... types) {
        ParameterTypeRegistry.ObjectMapper objectMapper = parameterTypeRegistry.getObjectMapper();
        List<ParameterType<?>> parameterTypes = new ArrayList<>();
        int typeIndex = 0;
        for (GroupBuilder groupBuilder : treeRegexp.getGroupBuilder().getChildren()) {
            // TODO: Sort out the priority of the hinted parameter type vs the regular expression parameter type
            // IMO we should use the regular expression type if it exists. Otherwise we should
            // use the type hint. If the regular expression exists we should also check if the type hint
            // matches that of the regular expression and throw a clear error if it does not.
            String parameterTypeRegexp = groupBuilder.getSource();
            ParameterType<?> parameterType = null;

            if (parameterType == null) {
                parameterType = parameterTypeRegistry.lookupByRegexp(parameterTypeRegexp, expressionRegexp, text);
            }

            if (parameterType == null && typeIndex < types.length) {
                Type type = types[typeIndex];
                parameterType = new ParameterType<>(
                        "anonymous",
                        parameterTypeRegexp,
                        Object.class,
                        (Transformer<Object>) arg -> objectMapper.convert(arg, type)
                );
            }
            if (parameterType == null) {
                parameterType = new ParameterType<>(
                        null,
                        parameterTypeRegexp,
                        String.class,
                        (Transformer<String>) arg -> arg
                );
            }
            parameterTypes.add(parameterType);
            typeIndex++;
        }

        return Argument.build(treeRegexp, parameterTypes, text);
    }

    @Override
    public Pattern getRegexp() {
        return expressionRegexp;
    }

    @Override
    public String getSource() {
        return expressionRegexp.pattern();
    }
}
