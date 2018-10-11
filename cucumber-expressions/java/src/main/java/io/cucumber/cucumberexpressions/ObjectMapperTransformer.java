package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;

//This class can be replaced with a lambda once we use java 8
final class ObjectMapperTransformer implements Transformer<Object> {

    private final DefaultTransformer defaultTransformer;
    private final Type toValueType;

    ObjectMapperTransformer(DefaultTransformer defaultTransformer, Type toValueType) {
        this.defaultTransformer = defaultTransformer;
        this.toValueType = toValueType;
    }

    @Override
    public Object transform(String arg) {
        return defaultTransformer.transform(arg, toValueType);
    }
}
