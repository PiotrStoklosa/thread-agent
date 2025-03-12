package org.threadmonitoring.bootstrap;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class BootstrapAgent {

    public static volatile MethodHandle bootstrap;

    public static CallSite bootstrap(MethodHandles.Lookup sourceMethodLookup,
                                     String adviceMethodName,
                                     MethodType adviceMethodType,
                                     String adviceClassName,
                                     Object... args) {
        if (bootstrap != null) {
            try {
                return (CallSite) bootstrap.invoke(null, adviceMethodName, adviceMethodType, adviceClassName, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else{
            throw new RuntimeException("Bootstrap method not initialized");
        }
    }

    public static void setBootstrap(MethodHandle bootstrap) {
        BootstrapAgent.bootstrap = bootstrap;
    }
}