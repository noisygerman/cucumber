package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;

//This class can be replaced with a lambda once we use java 8
final class ObjectMapperTransformer implements Transformer<Object> {

    private final ObjectMapper objectMapper;
    private final Type toValueType;

    ObjectMapperTransformer(ObjectMapper objectMapper, Type toValueType) {
        this.objectMapper = objectMapper;
        this.toValueType = toValueType;
    }

    @Override
    public Object transform(String arg) {
        return objectMapper.convert(arg, toValueType);
    }
}
