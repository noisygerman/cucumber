package io.cucumber.cucumberexpressions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import static java.util.Locale.ENGLISH;

public class SimpleDefaultTransformerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private SimpleDefaultTransformer objectMapper = new SimpleDefaultTransformer(ENGLISH);

    @Test
    public void simple_object_mapper_only_supports_class_types() {
        expectedException.expectMessage("" +
                "Can't transform something to java.util.AbstractList<E>\n" +
                "SimpleDefaultTransformer only supports a limited number of class types\n" +
                "Consider using a different object mapper or register a parameter type for java.util.AbstractList<E>");
        Type abstractListOfE = ArrayList.class.getGenericSuperclass();
        objectMapper.transform("something", abstractListOfE);
    }


    @Test
    public void simple_object_mapper_only_supports_some_class_types() {
        expectedException.expectMessage("" +
                "Can't transform something to class java.util.Date\n" +
                "SimpleDefaultTransformer only supports a limited number of class types\n" +
                "Consider using a different object mapper or register a parameter type for class java.util.Date");
        objectMapper.transform("something", Date.class);
    }


}