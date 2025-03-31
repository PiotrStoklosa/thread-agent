package org.threadmonitoring.model;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class MethodSubstitutionRule {
    private final ElementMatcher<? super TypeDescription> typeMatcher;
    private final MethodTemplate newMethod;
    private final ElementMatcher<? super MethodDescription> substituteMethod;

    private MethodSubstitutionRule(Builder builder) {
        this.typeMatcher = builder.typeMatcher;
        this.newMethod = builder.newMethod;
        this.substituteMethod = builder.substituteMethod;
    }

    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return typeMatcher;
    }

    public MethodTemplate getNewMethod() {
        return newMethod;
    }

    public ElementMatcher<? super MethodDescription> getSubstituteMethod() {
        return substituteMethod;
    }

    public static class Builder {
        private ElementMatcher<? super TypeDescription> typeMatcher;
        private MethodTemplate newMethod;
        private ElementMatcher<? super MethodDescription> substituteMethod;

        public Builder setTypeMatcher(ElementMatcher<? super TypeDescription> typeMatcher) {
            this.typeMatcher = typeMatcher;
            return this;
        }

        public Builder setNewMethod(MethodTemplate newMethod) {
            this.newMethod = newMethod;
            return this;
        }

        public Builder setSubstituteMethod(ElementMatcher<? super MethodDescription> substituteMethod) {
            this.substituteMethod = substituteMethod;
            return this;
        }

        public MethodSubstitutionRule build() {
            return new MethodSubstitutionRule(this);
        }
    }
}
