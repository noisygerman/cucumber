package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ParameterTypeRegistry {
    // Pattern.compile(...).pattern() is not necessary, but it helps us take advantage of the IntelliJ's regexp validation,
    // which detects unneeded escapes.
    private static final List<String> INTEGER_REGEXPS = asList(Pattern.compile("-?\\d+").pattern(), Pattern.compile("\\d+").pattern());
    private static final List<String> FLOAT_REGEXPS = singletonList(Pattern.compile("-?\\d*[.,]\\d+").pattern());
    private static final List<String> WORD_REGEXPS = singletonList(Pattern.compile("[^\\s]+").pattern());
    private static final List<String> STRING_REGEXPS = singletonList(Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"|'([^'\\\\]*(\\\\.[^'\\\\]*)*)'").pattern());
    private final Map<String, ParameterType<?>> parameterTypeByName = new HashMap<>();
    private final Map<Type, SortedSet<ParameterType<?>>> parameterTypesByType = new HashMap<>();
    private final Map<String, SortedSet<ParameterType<?>>> parameterTypesByRegexp = new HashMap<>();
    private final ObjectMapper objectMapper;

    public interface ObjectMapper {
        <T> T convert(String fromValue, Class<T> toValueType);

        Object convert(String fromValue, Type toValueType);
    }


    private static final class SimpleObjectMapper implements ObjectMapper {

        private final NumberParser numberParser;

        SimpleObjectMapper(Locale locale) {
            NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
            this.numberParser = new NumberParser(numberFormat);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T convert(String fromValue, Class<T> toValueType) {
            if (fromValue == null) {
                return null;
            }

            if (String.class.equals(toValueType)) {
                return (T) fromValue;
            }

            if (BigInteger.class.equals(toValueType)) {
                return (T) new BigInteger(fromValue);
            }

            if (BigDecimal.class.equals(toValueType)) {
                return (T) new BigDecimal(fromValue);
            }

            if (Byte.class.equals(toValueType)) {
                return (T) Byte.decode(fromValue);
            }

            if (Short.class.equals(toValueType)) {
                return (T) Short.decode(fromValue);
            }

            if (Integer.class.equals(toValueType)) {
                return (T) Integer.decode(fromValue);
            }

            if (Long.class.equals(toValueType)) {
                return (T) Long.decode(fromValue);
            }

            if (Float.class.equals(toValueType)) {
                return (T) (Float) numberParser.parseFloat(fromValue);
            }

            if (Double.class.equals(toValueType)) {
                return (T) (Double) numberParser.parseDouble(fromValue);
            }

            //TODO: This can happen with either the anonymous parameter type or with regular expressions
            // Resolution either:
            // 1. Register a parameter type for type.
            // 2. Register a different default object mapper.
            throw new IllegalArgumentException("Unsupported type");
        }

        @Override
        public Object convert(String fromValue, Type toValueType) {
            if (toValueType instanceof Class) {
                return convert(fromValue, (Class<?>) toValueType);
            }

            // TODO: Inline the method above here. Remove old method.
            // Above implementation was easier to write/copy/paste.
            throw new IllegalArgumentException();
        }
    }

    public ParameterTypeRegistry(Locale locale) {
        this.objectMapper = new SimpleObjectMapper(locale);

        defineParameterType(new ParameterType<>("biginteger", INTEGER_REGEXPS, BigInteger.class, new Transformer<BigInteger>() {
            @Override
            public BigInteger transform(String arg) {
                return objectMapper.convert(arg, BigInteger.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("bigdecimal", FLOAT_REGEXPS, BigDecimal.class, new Transformer<BigDecimal>() {
            @Override
            public BigDecimal transform(String arg) {
                return objectMapper.convert(arg, BigDecimal.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("byte", INTEGER_REGEXPS, Byte.class, new Transformer<Byte>() {
            @Override
            public Byte transform(String arg) {
                return objectMapper.convert(arg, Byte.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("short", INTEGER_REGEXPS, Short.class, new Transformer<Short>() {
            @Override
            public Short transform(String arg) {
                return objectMapper.convert(arg, Short.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("int", INTEGER_REGEXPS, Integer.class, new Transformer<Integer>() {
            @Override
            public Integer transform(String arg) {
                return objectMapper.convert(arg, Integer.class);
            }
        }, true, true));
        defineParameterType(new ParameterType<>("long", INTEGER_REGEXPS, Long.class, new Transformer<Long>() {
            @Override
            public Long transform(String arg) {
                return objectMapper.convert(arg, Long.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("float", FLOAT_REGEXPS, Float.class, new Transformer<Float>() {
            @Override
            public Float transform(String arg) {
                return objectMapper.convert(arg, Float.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("double", FLOAT_REGEXPS, Double.class, new Transformer<Double>() {
            @Override
            public Double transform(String arg) {
                return objectMapper.convert(arg, Double.class);
            }
        }, true, true));
        defineParameterType(new ParameterType<>("word", WORD_REGEXPS, String.class, new Transformer<String>() {
            @Override
            public String transform(String arg) {
                return objectMapper.convert(arg, String.class);
            }
        }, false, false));
        defineParameterType(new ParameterType<>("string", STRING_REGEXPS, String.class, new Transformer<String>() {
            @Override
            public String transform(String arg) {
                return arg == null ? null : objectMapper.convert(arg.replaceAll("\\\\\"", "\"").replaceAll("\\\\'", "'"), String.class);
            }
        }, true, false));
    }

    public void defineParameterType(ParameterType<?> parameterType) {
        if (parameterType.getName() != null) {
            if (parameterTypeByName.containsKey(parameterType.getName()))
                throw new DuplicateTypeNameException(String.format("There is already a parameter type with name %s", parameterType.getName()));
            parameterTypeByName.put(parameterType.getName(), parameterType);
        }

        for (String parameterTypeRegexp : parameterType.getRegexps()) {
            parameterTypesByRegexp.computeIfAbsent(parameterTypeRegexp, k -> new TreeSet<>());
            SortedSet<ParameterType<?>> parameterTypes = parameterTypesByRegexp.get(parameterTypeRegexp);
            if (!parameterTypes.isEmpty() && parameterTypes.first().preferForRegexpMatch() && parameterType.preferForRegexpMatch()) {
                throw new CucumberExpressionException(String.format(
                        "There can only be one preferential parameter type per regexp. " +
                                "The regexp /%s/ is used for two preferential parameter types, {%s} and {%s}",
                        parameterTypeRegexp, parameterTypes.first().getName(), parameterType.getName()
                ));
            }
            parameterTypes.add(parameterType);
        }

        parameterTypesByType.computeIfAbsent(parameterType.getType(), k -> new TreeSet<>());
        SortedSet<ParameterType<?>> parameterTypes = parameterTypesByType.get(parameterType.getType());
        if (!parameterTypes.isEmpty() && parameterTypes.first().preferForRegexpMatch() && parameterType.preferForRegexpMatch()) {
            throw new CucumberExpressionException(String.format(
                    "There can only be one preferential parameter type per type. " +
                            "The type %s is used for two preferential parameter types, {%s} and {%s}",
                    parameterType.getType().getTypeName(), parameterTypes.first().getType().getTypeName(), parameterType.getType().getTypeName()
            ));
        }
        parameterTypes.add(parameterType);

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public <T> ParameterType<T> lookupByTypeName(String typeName) {
        return (ParameterType<T>) parameterTypeByName.get(typeName);
    }

    public <T> ParameterType<T> lookupByType(Type type, String text) {
        SortedSet<ParameterType<?>> parameterTypes = parameterTypesByType.get(type);
        if (parameterTypes == null) return null;
        if (parameterTypes.size() > 1 && !parameterTypes.first().preferForRegexpMatch()) {
            throw new CucumberExpressionException(String.format("Multiple parameter types match type %s", type.getTypeName()));
        }
        return (ParameterType<T>) parameterTypes.first();
    }

    public <T> ParameterType<T> lookupByRegexp(String parameterTypeRegexp, Pattern expressionRegexp, String text) {
        SortedSet<ParameterType<?>> parameterTypes = parameterTypesByRegexp.get(parameterTypeRegexp);
        if (parameterTypes == null) return null;
        if (parameterTypes.size() > 1 && !parameterTypes.first().preferForRegexpMatch()) {
            // We don't do this check on insertion because we only want to restrict
            // ambiguity when we look up by Regexp. Users of CucumberExpression should
            // not be restricted.
            List<GeneratedExpression> generatedExpressions = new CucumberExpressionGenerator(this).generateExpressions(text);
            throw new AmbiguousParameterTypeException(parameterTypeRegexp, expressionRegexp, parameterTypes, generatedExpressions);
        }
        return (ParameterType<T>) parameterTypes.first();
    }

    public Collection<ParameterType<?>> getParameterTypes() {
        return parameterTypeByName.values();
    }
}
