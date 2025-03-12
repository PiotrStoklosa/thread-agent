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
                e.printStackTrace();
                throw new RuntimeException(e);

            }
        } else{
            System.out.println("Bootstrap method not initialized");
            throw new RuntimeException("Bootstrap method not initialized");
        }
    }

    public static void setBootstrap(MethodHandle bootstrap) {
        BootstrapAgent.bootstrap = bootstrap;
    }
}