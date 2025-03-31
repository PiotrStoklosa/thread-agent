package org.threadmonitoring.model;

import java.util.List;

public class MethodTemplate {
    private final Class<?> clazz;
    private final String methodName;
    private final List<Class<?>> arguments;

    private MethodTemplate(Builder builder) {
        this.clazz = builder.clazz;
        this.methodName = builder.methodName;
        this.arguments = builder.arguments;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<Class<?>> getArguments() {
        return arguments;
    }

    public static class Builder {
        private Class<?> clazz;
        private String methodName;
        private List<Class<?>> arguments;

        public Builder setClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder setMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder setArguments(List<Class<?>> arguments) {
            this.arguments = arguments;
            return this;
        }

        public MethodTemplate build() {
            return new MethodTemplate(this);
        }
    }
}
