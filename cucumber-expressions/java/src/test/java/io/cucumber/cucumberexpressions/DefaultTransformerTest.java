package io.cucumber.cucumberexpressions;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class DefaultTransformerTest {

    private final DefaultTransformer defaultTransformer;

    @Parameterized.Parameters
    public static Collection<DefaultTransformer> objectMapperImplementations() {
        return asList(
                new SimpleDefaultTransformer(ENGLISH),
                new TestJacksonDefaultTransformer()
        );
    }

    public DefaultTransformerTest(DefaultTransformer defaultTransformer) {
        this.defaultTransformer = defaultTransformer;
    }

    @Test
    public void should_convert_null_to_null() {
        assertNull(defaultTransformer.transform(null, Object.class));
    }

    @Test
    public void should_convert_to_string() {
        assertEquals("Barbara Liskov",
                defaultTransformer.transform("Barbara Liskov", String.class));
    }


    @Test
    public void should_convert_to_object() {
        assertEquals("Barbara Liskov",
                defaultTransformer.transform("Barbara Liskov", Object.class));
    }

    @Test
    public void should_convert_to_big_integer() {
        assertEquals(new BigInteger("10000008"),
                defaultTransformer.transform("10000008", BigInteger.class));
    }


    @Test
    public void should_convert_to_big_decimal() {
        assertEquals(new BigDecimal("1.0000008"),
                defaultTransformer.transform("1.0000008", BigDecimal.class));
    }

    @Test
    public void should_convert_to_byte() {
        assertEquals(Byte.decode("42"), defaultTransformer.transform("42", Byte.class));
        assertEquals(Byte.decode("42"), defaultTransformer.transform("42", byte.class));
    }

    @Test
    public void should_convert_to_short() {
        assertEquals(Short.decode("42"), defaultTransformer.transform("42", Short.class));
        assertEquals(Short.decode("42"), defaultTransformer.transform("42", short.class));
    }

    @Test
    public void should_convert_to_integer() {
        assertEquals(Integer.decode("42"), defaultTransformer.transform("42", Integer.class));
        assertEquals(Integer.decode("42"), defaultTransformer.transform("42", int.class));
    }

    @Test
    public void should_convert_to_long() {
        assertEquals(Long.decode("42"), defaultTransformer.transform("42", Long.class));
        assertEquals(Long.decode("42"), defaultTransformer.transform("42", long.class));
    }

    @Test
    public void should_convert_to_float() {
        assertEquals(4.2f, defaultTransformer.transform("4.2", Float.class));
        assertEquals(4.2f, defaultTransformer.transform("4.2", float.class));
    }


    @Test
    public void should_convert_to_double() {
        assertEquals(4.2, defaultTransformer.transform("4.2", Double.class));
        assertEquals(4.2, defaultTransformer.transform("4.2", double.class));
    }

    private static class TestJacksonDefaultTransformer implements DefaultTransformer {
        com.fasterxml.jackson.databind.ObjectMapper delegate =
                new com.fasterxml.jackson.databind.ObjectMapper();

        @Override
        public Object transform(String fromValue, Type toValueType) {
            TypeFactory typeFactory = delegate.getTypeFactory();
            return delegate.convertValue(fromValue, typeFactory.constructType(toValueType));
        }

    }
}