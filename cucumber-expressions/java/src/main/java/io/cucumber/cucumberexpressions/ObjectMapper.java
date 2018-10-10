package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;

public interface ObjectMapper {

    Object convert(String fromValue, Type toValueType);
}
