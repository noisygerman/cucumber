package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.cucumber.cucumberexpressions.ParameterType.createAnonymousParameterType;

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
    public List<Argument<?>> match(String text, Type... typeHints) {
        final ObjectMapper objectMapper = parameterTypeRegistry.getObjectMapper();
        final List<ParameterType<?>> parameterTypes = new ArrayList<>();
        int typeHintIndex = 0;
        for (GroupBuilder groupBuilder : treeRegexp.getGroupBuilder().getChildren()) {
            final String parameterTypeRegexp = groupBuilder.getSource();
            final Type typeHint = typeHintIndex < typeHints.length ? typeHints[typeHintIndex++] : String.class;

            ParameterType<?> parameterType = parameterTypeRegistry.lookupByRegexp(parameterTypeRegexp, expressionRegexp, text);

            if (parameterType == null) {
                parameterType = createAnonymousParameterType(parameterTypeRegexp);
            }

            // Either from createAnonymousParameterType or lookupByRegexp
            if (parameterType.isAnonymous()) {
                Transformer<Object> transformer = new ObjectMapperTransformer(objectMapper, typeHint);
                parameterType = parameterType.deAnonymize(typeHint, transformer);
            }

            // Use hint for validation only
            // TODO: Handle types that are not classes - needs tests
            Class<?> paramClass = (Class) parameterType.getType();
            Class<?> hintClass = (Class) typeHint;
            if (!hintClass.isAssignableFrom(paramClass)) {
                throw new CucumberExpressionException(String.format(
                        // TODO: Tell users what to do if they attempt to convert \d+ into Float.
                        "Capture group with %s transforms to %s, which is incompatible with %s. " +
                                "Try changing the capture group to something that doesn't match an existing parameter type.",
                        parameterTypeRegexp,
                        paramClass.getTypeName(),
                        hintClass.getTypeName())
                );
            }

            parameterTypes.add(parameterType);
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
