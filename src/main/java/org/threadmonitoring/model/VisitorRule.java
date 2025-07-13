package org.threadmonitoring.model;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class VisitorRule {
    private final ElementMatcher<? super TypeDescription> typeMatcher;
    private final AsmVisitorWrapper asmVisitorWrapper;

    private VisitorRule(VisitorRule.Builder builder) {
        this.typeMatcher = builder.typeMatcher;
        this.asmVisitorWrapper = builder.asmVisitorWrapper;
    }

    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return typeMatcher;
    }

    public AsmVisitorWrapper getAsmVisitorWrapper() {
        return asmVisitorWrapper;
    }

    public static class Builder {

        private ElementMatcher<? super TypeDescription> typeMatcher;
        private AsmVisitorWrapper asmVisitorWrapper;

        public VisitorRule.Builder setTypeMatcher(ElementMatcher<? super TypeDescription> typeMatcher) {
            this.typeMatcher = typeMatcher;
            return this;
        }

        public VisitorRule.Builder setAsmVisitorWrapper(AsmVisitorWrapper asmVisitorWrapper) {
            this.asmVisitorWrapper = asmVisitorWrapper;
            return this;
        }

        public VisitorRule build() {
            return new VisitorRule(this);
        }
    }
}
