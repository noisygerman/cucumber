package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class SimpleObjectMapper implements ObjectMapper {

    private final NumberParser numberParser;

    SimpleObjectMapper(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        this.numberParser = new NumberParser(numberFormat);
    }

    @Override
    public Object convert(String fromValue, Type toValueType) {
        if (toValueType instanceof Class) {
            return convert(fromValue, (Class<?>) requireNonNull(toValueType));
        }

        // TODO:
        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    private Object convert(String fromValue, Class<?> toValueType) {
        if (fromValue == null) {
            return null;
        }

        if (String.class.equals(toValueType) || Object.class.equals(toValueType)) {
            return fromValue;
        }

        if (BigInteger.class.equals(toValueType)) {
            return new BigInteger(fromValue);
        }

        if (BigDecimal.class.equals(toValueType)) {
            return new BigDecimal(fromValue);
        }

        if (Byte.class.equals(toValueType) || byte.class.equals(toValueType)) {
            return Byte.decode(fromValue);
        }

        if (Short.class.equals(toValueType) || short.class.equals(toValueType)) {
            return Short.decode(fromValue);
        }

        if (Integer.class.equals(toValueType) || int.class.equals(toValueType)) {
            return Integer.decode(fromValue);
        }

        if (Long.class.equals(toValueType) || long.class.equals(toValueType)) {
            return Long.decode(fromValue);
        }

        if (Float.class.equals(toValueType) || float.class.equals(toValueType)) {
            return numberParser.parseFloat(fromValue);
        }

        if (Double.class.equals(toValueType) || double.class.equals(toValueType)) {
            return numberParser.parseDouble(fromValue);
        }

        //TODO: This can happen with either the anonymous parameter type or with regular expressions
        // Resolution either:
        // 1. Register a parameter type for type.
        // 2. Register a different default object mapper.
        throw new IllegalArgumentException("Unsupported type " + toValueType);
    }

}
