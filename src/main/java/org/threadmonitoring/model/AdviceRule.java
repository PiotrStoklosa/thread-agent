package org.threadmonitoring.model;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class AdviceRule {

    private final ElementMatcher<? super TypeDescription> typeMatcher;
    private final String className;
    private final ElementMatcher<? super MethodDescription> methodMatcher;

    private AdviceRule(Builder builder) {
        this.typeMatcher = builder.typeMatcher;
        this.className = builder.className;
        this.methodMatcher = builder.methodMatcher;
    }

    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return typeMatcher;
    }

    public String getClassName() {
        return className;
    }

    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return methodMatcher;
    }

    public static class Builder {

        private ElementMatcher<? super TypeDescription> typeMatcher;
        private String className;
        private ElementMatcher<? super MethodDescription> methodMatcher;

        public Builder setTypeMatcher(ElementMatcher<? super TypeDescription> typeMatcher) {
            this.typeMatcher = typeMatcher;
            return this;
        }

        public Builder setClassName(String className) {
            this.className = className;
            return this;
        }

        public Builder setMethodMatcher(ElementMatcher<? super MethodDescription> methodMatcher) {
            this.methodMatcher = methodMatcher;
            return this;
        }

        public AdviceRule build() {
            return new AdviceRule(this);
        }
    }
}
