package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

final class SimpleDefaultTransformer implements DefaultTransformer {

    private final NumberParser numberParser;

    SimpleDefaultTransformer(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        this.numberParser = new NumberParser(numberFormat);
    }

    @Override
    public Object transform(String fromValue, Type toValueType) {
        if (!(toValueType instanceof Class)) {
            throw createIllegalArgumentException(fromValue, toValueType);
        }

        Class<?> toValueType1 = (Class<?>) requireNonNull(toValueType);
        if (fromValue == null) {
            return null;
        }

        if (String.class.equals(toValueType1) || Object.class.equals(toValueType1)) {
            return fromValue;
        }

        if (BigInteger.class.equals(toValueType1)) {
            return new BigInteger(fromValue);
        }

        if (BigDecimal.class.equals(toValueType1)) {
            return new BigDecimal(fromValue);
        }

        if (Byte.class.equals(toValueType1) || byte.class.equals(toValueType1)) {
            return Byte.decode(fromValue);
        }

        if (Short.class.equals(toValueType1) || short.class.equals(toValueType1)) {
            return Short.decode(fromValue);
        }

        if (Integer.class.equals(toValueType1) || int.class.equals(toValueType1)) {
            return Integer.decode(fromValue);
        }

        if (Long.class.equals(toValueType1) || long.class.equals(toValueType1)) {
            return Long.decode(fromValue);
        }

        if (Float.class.equals(toValueType1) || float.class.equals(toValueType1)) {
            return numberParser.parseFloat(fromValue);
        }

        if (Double.class.equals(toValueType1) || double.class.equals(toValueType1)) {
            return numberParser.parseDouble(fromValue);
        }

        throw createIllegalArgumentException(fromValue, toValueType);
    }

    private IllegalArgumentException createIllegalArgumentException(String fromValue, Type toValueType) {
        return new IllegalArgumentException(
                "Can't transform " + fromValue + " to " + toValueType + "\n" +
                        "SimpleDefaultTransformer only supports a limited number of class types\n" +
                        "Consider using a different object mapper or register a parameter type for " + toValueType
        );
    }

}
