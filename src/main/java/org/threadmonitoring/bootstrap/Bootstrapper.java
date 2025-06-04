package org.threadmonitoring.bootstrap;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Bootstrapper {

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
                System.err.println("Error occurred during invoking bootstrap method: " + e);
                throw new RuntimeException(e);

            }
        } else{
            System.err.println("Bootstrap method not initialized");
            throw new RuntimeException("Bootstrap method not initialized");
        }
    }

    public static void setBootstrap(MethodHandle bootstrap) {
        Bootstrapper.bootstrap = bootstrap;
    }
}